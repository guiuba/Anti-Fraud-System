package antifraud.services;

import antifraud.dao.StolenCardRepo;
import antifraud.dao.SuspiciousIpRepo;
import antifraud.dao.TransactionRepo;
import antifraud.dao.UserRepo;
import antifraud.dto.UserDTO;
import antifraud.dto.UserDTO2;
import antifraud.model.*;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class ServiceOne {

    private UserRepo userRepo;

    private SuspiciousIpRepo suspiciousIpRepo;
    private StolenCardRepo stolenCardRepo;
    private TransactionRepo transactionRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    public ServiceOne(
            UserRepo userRepo,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            SuspiciousIpRepo suspiciousIpRepo,
            StolenCardRepo stolenCardRepo,
            TransactionRepo transactionRepo) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.suspiciousIpRepo = suspiciousIpRepo;
        this.stolenCardRepo = stolenCardRepo;
        this.transactionRepo = transactionRepo;
    }

    public ResponseEntity<UserDTO> register(User user) {
        Optional<User> userToAdd = userRepo.findUserByUsernameIgnoreCase(user.getUsername());
        if (userToAdd.isPresent()) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        userRepo.save(user);
        Optional<User> userNew = userRepo.findUserByUsernameIgnoreCase(user.getUsername());
        if (userNew.get().getId() == 1) {
            user.setRole("ADMINISTRATOR");
            user.setOperation("unlocked");
            userRepo.save(user);
        } else {
            user.setRole("MERCHANT");
            user.setOperation("locked");
            userRepo.save(user);
        }
        UserDTO userDTO = userMapper.toUserDTO(user);
        return new ResponseEntity<>(userDTO,
                HttpStatus.CREATED);
    }

    public ResponseEntity<List<UserDTO>> findClients() {
        List<User> users = userRepo.findAll();
        List<UserDTO> usersDTO = userMapper.toUserDTOList(users);
        return new ResponseEntity(usersDTO, HttpStatus.OK);
    }

    public ResponseEntity<Map<String, String>> deleteUser(String username) {
        Optional<User> user = userRepo.findUserByUsernameIgnoreCase(username);
        if (user.isPresent()) {
            userRepo.delete(user.get());
            return ResponseEntity.ok(Map.of(
                    "username", username,
                    "status", "Deleted successfully!"));
        }
        return ResponseEntity.notFound().build();

    }

    public ResponseEntity<UserDTO> changeRole(String username, String role) {
        Optional<User> user = userRepo.findUserByUsernameIgnoreCase(username);
        if (user.isPresent()) {
            if (role.equalsIgnoreCase("SUPPORT") ||
                    role.equalsIgnoreCase("MERCHANT")) {
                if (role.equalsIgnoreCase(user.get().getRole())) {  // user.get().getRole().equalsIgnoreCase(role)
                    return new ResponseEntity(HttpStatus.CONFLICT);
                }
                user.get().setRole(role.toUpperCase());
                userRepo.save(user.get());
                return new ResponseEntity(userMapper.toUserDTO(user.get()), HttpStatus.OK);
            }
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.notFound().build();
    }


    public ResponseEntity<Map<String, String>> changeAccess(UserDTO2 userDTO2) {
        Optional<User> user = userRepo.findUserByUsernameIgnoreCase(userDTO2.getUsername());
        if (user.isPresent()) {

            if ("LOCK".equalsIgnoreCase(userDTO2.getOperation()) &&
                    user.get().getRole().equalsIgnoreCase("ADMINISTRATOR")) {
                return ResponseEntity.badRequest().build();
            }

            if ("UNLOCK".equalsIgnoreCase(userDTO2.getOperation())) {
                user.get().setOperation("unlocked");
                userRepo.save(user.get());
                return ResponseEntity.ok(Map.of(
                        "status", "User " + userDTO2.getUsername() + " unlocked!"));
            }
            if ("LOCK".equalsIgnoreCase(userDTO2.getOperation()) &&
                    !user.get().getRole().equalsIgnoreCase("ADMINISTRATOR")) {
                user.get().setOperation("locked");
                userRepo.save(user.get());
                return ResponseEntity.ok(Map.of(
                        "status", "User " + userDTO2.getUsername() + " locked!"));
            }
        }
        return ResponseEntity.notFound().build();
    }

    public ResponseEntity<SuspiciousIp> addSuspiciousIp(String ip) {  // SuspiciousIp
        if (InetAddressValidator.getInstance().isValidInet4Address(ip)) {
            Optional<SuspiciousIp> suspiciousIp = suspiciousIpRepo.findByIp(ip);
            if (!suspiciousIp.isPresent()) {
                SuspiciousIp newSuspiciousIp = new SuspiciousIp();
                newSuspiciousIp.setIp(ip);
                suspiciousIpRepo.save(newSuspiciousIp);
                return new ResponseEntity(newSuspiciousIp, HttpStatus.OK);
            } else {
                return new ResponseEntity(HttpStatus.CONFLICT);
            }
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);

    }

    public ResponseEntity<Map<String, String>> deleteSuspiciousIp(String ip) {

        if (InetAddressValidator.getInstance().isValidInet4Address(ip)) {
            Optional<SuspiciousIp> suspiciousIp = suspiciousIpRepo.findByIp(ip);
            if (suspiciousIp.isPresent()) {
                suspiciousIpRepo.delete(suspiciousIp.get());
                return new ResponseEntity(Map.of("status",
                        "IP " + ip +
                                " successfully removed!"), HttpStatus.OK);
            } else {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<List<SuspiciousIp>> findSuspiciousIps() {
        return new ResponseEntity<>(suspiciousIpRepo.findAll(), HttpStatus.OK);
    }

    public ResponseEntity<StolenCard> addStolenCard(String cardNumber) {
        if (!isCardNumberValid(cardNumber)) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        if (StolenCard.passLuhnAlgorithmTest(cardNumber)) {
            Optional<StolenCard> stolenCard = stolenCardRepo.findByNumber(cardNumber);
            if (!stolenCard.isPresent()) {
                StolenCard newStolenCard = new StolenCard();
                newStolenCard.setNumber(cardNumber);
                stolenCardRepo.save(newStolenCard);
                return new ResponseEntity(newStolenCard, HttpStatus.OK);
            } else {
                return new ResponseEntity(HttpStatus.CONFLICT);
            }
        }

        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Map<String, String>> deleteStolenCard(String cardNumber) {
        if (!isCardNumberValid(cardNumber)) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        if (StolenCard.passLuhnAlgorithmTest(cardNumber)) {
            Optional<StolenCard> stolenCard = stolenCardRepo.findByNumber(cardNumber);
            if (stolenCard.isPresent()) {
                stolenCardRepo.delete(stolenCard.get());
                return new ResponseEntity(Map.of("status",
                        "Card " + cardNumber + " successfully removed!"), HttpStatus.OK);
            } else {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<List<StolenCard>> findStolenCards() {
        return new ResponseEntity(stolenCardRepo.findAll(), HttpStatus.OK);
    }

    public boolean isCardNumberValid(String cardNumber) {
        String numberRegex = "[1-9][0-9]{15}";
        if (!cardNumber.matches(numberRegex)) {
            return false;
        }
        return true;
    }

}
