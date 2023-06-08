package antifraud.services;


import antifraud.Enum.Region;
import antifraud.Enum.TransactionValidity;
import antifraud.dao.CardRepo;
import antifraud.dao.StolenCardRepo;
import antifraud.dao.SuspiciousIpRepo;
import antifraud.dao.TransactionRepo;
import antifraud.dto.TransactionDTO;
import antifraud.dto.TransactionDTO2;
import antifraud.model.Card;
import antifraud.model.StolenCard;
import antifraud.model.Transaction;
import antifraud.model.UserMapper;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class TransactionService {

    private HttpStatus status;
    private TransactionRepo transactionRepo;
    private SuspiciousIpRepo suspiciousIpRepo;
    private StolenCardRepo stolenCardRepo;
    private CardRepo cardRepo;
    private TransactionValidity transactionValidity;
    private LocalDateTime transactionTime;
    private LocalDateTime transactionTimeLessOneHour;
    private List<String> info;
    private Map<String, String> resultMap;
    private List<Transaction> lastHourTransactions;
    private UserMapper userMapper;

    @Autowired
    public TransactionService(TransactionRepo transactionRepo,
                              SuspiciousIpRepo suspiciousIpRepo,
                              StolenCardRepo stolenCardRepo,
                              CardRepo cardRepo,
                              UserMapper userMapper) {
        this.transactionRepo = transactionRepo;
        this.suspiciousIpRepo = suspiciousIpRepo;
        this.stolenCardRepo = stolenCardRepo;
        this.cardRepo = cardRepo;
        this.userMapper = userMapper;
    }

    public ResponseEntity<Map<String, String>> checkTransaction(Transaction transaction) {
        status = HttpStatus.OK;
        transactionValidity = transactionValidity.ALLOWED;
        info = new ArrayList<>();
        resultMap = new LinkedHashMap<>();

        if (StolenCard.passLuhnAlgorithmTest(transaction.getNumber()) &&
                InetAddressValidator.getInstance().isValidInet4Address(transaction.getIp()) &&
                transaction.getAmount() >= 0 &&
                Region.isValidRegion(transaction.getRegion())) {

            addErrorIfCardInvalid(transaction);
            addErrorIfIpInvalid(transaction);

            Card card;
            if (!cardRepo.existsByCardNumber(transaction.getNumber())) {
                card = new Card(transaction.getNumber(), 200L, 1500L);
                cardRepo.save(card);
            } else {
                card = cardRepo.findByCardNumber(transaction.getNumber());
            }

            addErrorIfAmountInvalid(transaction, card);
            addErrorIfIpOrRegionCorrelation(transaction);

            if (info.isEmpty()) {
                info.add("none");
            }
            Collections.sort(info);
            resultMap.put("result", transactionValidity.name());
            resultMap.put("info", info.toString().substring(1, info.toString().length() - 1));

            transaction.setTransactionValidity(transactionValidity.name());
            transactionRepo.save(transaction);

            return new ResponseEntity<>(resultMap, status);
        }
        return ResponseEntity.badRequest().build();
    }

    private void addErrorIfCardInvalid(Transaction transaction) {
        if (stolenCardRepo.findByNumber(transaction.getNumber()).isPresent()) {
            info.add("card-number");
        }
    }

    private void addErrorIfIpInvalid(Transaction transaction) {
        if (suspiciousIpRepo.findByIp(transaction.getIp()).isPresent()) {
            info.add("ip");
        }
    }

    private void addErrorIfAmountInvalid(Transaction transaction, Card card) {
        long amount = transaction.getAmount();
        long maxAmountForAllowed2 = card.getMaxAllowed();
        long maxAmountForManualProcessing2 = card.getMaxManual();

        if (amount > maxAmountForAllowed2 && amount <= maxAmountForManualProcessing2) {

            if (amount == 1000) {
                transactionValidity = TransactionValidity.PROHIBITED;

            } else {
                transactionValidityManager(TransactionValidity.MANUAL_PROCESSING);
                info.add("amount");
            }
        } else if (amount > maxAmountForAllowed2) {
            transactionValidityManager(TransactionValidity.PROHIBITED);
            info.add("amount");
        }

    }

    private void addErrorIfIpOrRegionCorrelation(Transaction transaction) {
        String ipCorrelation = null;
        String regionCorrelation = null;
        transactionTime = transaction.getDate();
        transactionTimeLessOneHour = transactionTime.minusHours(1);
        lastHourTransactions = transactionRepo
                .findAllByDateBetween(transactionTimeLessOneHour, transactionTime);
        long differentIps = lastHourTransactions.stream()
                .map(Transaction::getIp)
                .filter(diffTransaction -> !diffTransaction.equalsIgnoreCase(transaction.getIp()))
                .distinct()
                .count();
        long differentRegions = lastHourTransactions.stream()
                .map(Transaction::getRegion)
                .filter(diffTransaction -> !diffTransaction.equalsIgnoreCase(transaction.getRegion()))
                .distinct()
                .count();

        if (differentRegions == 2 || differentRegions > 2) {
            info.add("region-correlation");
            regionCorrelation = differentRegions > 2 ?  // transactionValidity = differentRegions > 2 ?
                    transactionValidity.PROHIBITED.name() :
                    transactionValidity.MANUAL_PROCESSING.name();
            transactionValidityManager(TransactionValidity.valueOf(regionCorrelation));

        }

        if (differentIps == 2 || differentIps > 2) {
            info.add("ip-correlation");
            ipCorrelation = differentIps > 2 ?
                    transactionValidity.PROHIBITED.name() :
                    transactionValidity.MANUAL_PROCESSING.name();
            transactionValidityManager(TransactionValidity.valueOf(ipCorrelation));

        }

    }

    public void transactionValidityManager(TransactionValidity newTransactionValidity) {
        transactionValidity = transactionValidity.getStatus() > newTransactionValidity.getStatus() ?
                transactionValidity : newTransactionValidity;
    }

    public ResponseEntity<Transaction> addFeedback(TransactionDTO transactionDTO) {
        status = HttpStatus.OK;
        String feedback = transactionDTO.getFeedback();
        Optional<Transaction> transaction = transactionRepo.findById(transactionDTO.getTransactionId());

        if (!transaction.isPresent()) {
            status = HttpStatus.NOT_FOUND;
        } else {
            boolean isTherePriorFeedbackInDB = !"".equals(transaction.get().getFeedback());
            String transactionValidity = transaction.get().getTransactionValidity();
            if (!TransactionValidity.isValidTransactionValidity(feedback)) {
                status = HttpStatus.BAD_REQUEST;
            } else {
                if (isTherePriorFeedbackInDB) {
                    status = HttpStatus.CONFLICT;
                } else {
                    Card card = cardRepo.findById(transaction.get().getNumber()).get();
                    switch (feedback) {
                        case "ALLOWED":
                            if (transactionValidity.equals(TransactionValidity.MANUAL_PROCESSING.name())) {
                                card.setMaxAllowed(increaseLimit(card.getMaxAllowed(), transaction.get().getAmount()));
                            } else if (transactionValidity.equals(TransactionValidity.PROHIBITED.name())) {
                                card.setMaxAllowed(increaseLimit(card.getMaxAllowed(), transaction.get().getAmount()));
                                card.setMaxManual(increaseLimit(card.getMaxManual(), transaction.get().getAmount()));
                            } else {
                                status = HttpStatus.UNPROCESSABLE_ENTITY;
                                break;
                            }
                            transaction.get().setFeedback(TransactionValidity.ALLOWED.name());
                            break;
                        case "MANUAL_PROCESSING":
                            if (transactionValidity.equals(TransactionValidity.ALLOWED.name())) {
                                card.setMaxAllowed(decreaseLimit(card.getMaxAllowed(), transaction.get().getAmount()));
                            } else if (transactionValidity.equals(TransactionValidity.PROHIBITED.name())) {
                                card.setMaxManual(increaseLimit(card.getMaxManual(), transaction.get().getAmount()));
                            } else {
                                status = HttpStatus.UNPROCESSABLE_ENTITY;
                                break;
                            }
                            transaction.get().setFeedback(TransactionValidity.MANUAL_PROCESSING.name());
                            break;
                        case "PROHIBITED":
                            if (transactionValidity.equals(TransactionValidity.ALLOWED.name())) {
                                card.setMaxAllowed(decreaseLimit(card.getMaxAllowed(), transaction.get().getAmount()));
                                card.setMaxManual(decreaseLimit(card.getMaxManual(), transaction.get().getAmount()));
                            } else if (transactionValidity.equals(TransactionValidity.MANUAL_PROCESSING.name())) {
                                card.setMaxManual(decreaseLimit(card.getMaxManual(), transaction.get().getAmount()));
                            } else {
                                status = HttpStatus.UNPROCESSABLE_ENTITY;
                                break;
                            }
                            transaction.get().setFeedback(TransactionValidity.PROHIBITED.name());
                            break;
                        default:

                    }
                    cardRepo.save(card);

                }
            }
        }

        transactionRepo.save(transaction.get());
        return status == HttpStatus.OK ? new ResponseEntity(
                userMapper.convertTransactionToTransactionDTO2(transaction.get()), HttpStatus.OK) :
                new ResponseEntity<>(status);
    }


    public long increaseLimit(long current_limit, long value_from_transaction) {
        return (long) Math.ceil(0.8 * current_limit + 0.2 * value_from_transaction);
    }

    public long decreaseLimit(long current_limit, long value_from_transaction) {
        return (long) Math.ceil(0.8 * current_limit - 0.2 * value_from_transaction);
    }


    public ResponseEntity<List<TransactionDTO2>> showTransactionHistoryByCard(String number) {
        status = HttpStatus.OK;
        List<Transaction> transactions = transactionRepo.findAllByNumber(number);
        List<TransactionDTO2> transactionsDTO2 = userMapper.convertTransactionsToTransactionDTO2List(transactions);
        if (transactions.isEmpty()) {
            status = HttpStatus.NOT_FOUND;
        }

        if (!StolenCard.passLuhnAlgorithmTest(number)) {
            status = HttpStatus.BAD_REQUEST;
        }
        return status == HttpStatus.OK ? new ResponseEntity(
                transactionsDTO2, HttpStatus.OK) :
                new ResponseEntity<>(status);
    }

    public ResponseEntity<List<TransactionDTO2>> showTransactionHistory() {
        List<Transaction> transactions = (List<Transaction>) transactionRepo.findAll();
        List<TransactionDTO2> transactionsDTO2 = userMapper.convertTransactionsToTransactionDTO2List(transactions);
        return new ResponseEntity(
                transactionsDTO2, HttpStatus.OK);
    }

    public ResponseEntity<List<Card>> getcards() {
        List<Card> cardList = cardRepo.findAll();
        return new ResponseEntity(cardList, HttpStatus.OK);
    }
}



