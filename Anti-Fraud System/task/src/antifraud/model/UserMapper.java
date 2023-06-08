package antifraud.model;

import antifraud.dto.TransactionDTO2;
import antifraud.dto.UserDTO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    private static final ModelMapper MODEL_MAPPER = new ModelMapper();

    public UserDTO toUserDTO(User user) {
        return MODEL_MAPPER.map(user, UserDTO.class);
    }

    public List<UserDTO> toUserDTOList(List<User> usersList) {
        return usersList.stream().map(this::toUserDTO).collect(Collectors.toList());
    }

    public TransactionDTO2 convertTransactionToTransactionDTO2(Transaction transaction) {
     return new TransactionDTO2(transaction.getId(),
             transaction.getAmount(),
             transaction.getIp(),
             transaction.getNumber(),
             transaction.getRegion(),
             transaction.getDate(),
             transaction.getTransactionValidity(),
             transaction.getFeedback());
    }

    public List<TransactionDTO2> convertTransactionsToTransactionDTO2List(List<Transaction> transactionsList) {
        return transactionsList.stream()
                .map(this::convertTransactionToTransactionDTO2)
                .collect(Collectors.toList());
    }


}
