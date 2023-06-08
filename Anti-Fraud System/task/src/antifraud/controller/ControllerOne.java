package antifraud.controller;

import antifraud.dto.TransactionDTO;
import antifraud.dto.TransactionDTO2;
import antifraud.dto.UserDTO;
import antifraud.dto.UserDTO2;
import antifraud.model.*;
import antifraud.services.ServiceOne;
import antifraud.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ControllerOne {

    private ServiceOne serviceOne;
    private final PasswordEncoder passwordEncoder;
    private TransactionService transactionService;

    @Autowired
    public ControllerOne(ServiceOne serviceOne, PasswordEncoder passwordEncoder,
                         TransactionService transactionService) {
        this.serviceOne = serviceOne;
        this.passwordEncoder = passwordEncoder;
        this.transactionService = transactionService;
    }


    @PostMapping("/antifraud/transaction")
    public ResponseEntity<Map<String, String>> checkTransaction(
            @Valid @RequestBody Transaction transaction) {
        return transactionService.checkTransaction(transaction);
    }

    @PostMapping("auth/user")
    public ResponseEntity<UserDTO> register(HttpServletResponse response,
                                            @Valid @RequestBody User user) {
        response.addHeader("Content-type", "application/json");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return serviceOne.register(user);
    }

    @PostMapping(path = "/antifraud/suspicious-ip", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuspiciousIp> addSuspiciousIp(
            @RequestBody SuspiciousIp ip) {
        return serviceOne.addSuspiciousIp(ip.getIp());
    }

    @PostMapping(path = "/antifraud/stolencard", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StolenCard> addStolenCard(
            HttpServletResponse response,
            @Valid
            @RequestBody StolenCard card) {
        response.addHeader("Content-type", "application/json");
        return serviceOne.addStolenCard(card.getNumber());
    }

    @GetMapping("/auth/list")
    public ResponseEntity<List<UserDTO>> findClients() {
        return serviceOne.findClients();
    }

    @GetMapping("/antifraud/suspicious-ip")
    public ResponseEntity<List<SuspiciousIp>> findSuspiciousIps() {
        return serviceOne.findSuspiciousIps();
    }

    @GetMapping("/antifraud/stolencard")
    public ResponseEntity<List<StolenCard>> findStolenCards() {
        return serviceOne.findStolenCards();
    }

    @GetMapping("/antifraud/history")
    public ResponseEntity<List<TransactionDTO2>> showTransactionHistory() {
        return transactionService.showTransactionHistory();
    }

    @GetMapping("/antifraud/history/{number}")
    public ResponseEntity<List<TransactionDTO2>> showTransactionHistoryByCard(
            @Validated @PathVariable String number) {
        return transactionService.showTransactionHistoryByCard(number);
    }

    @DeleteMapping("/auth/user/{username}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @Validated @PathVariable("username") @Size(min = 1) String username) {
        return serviceOne.deleteUser(username);

    }

    @DeleteMapping("/antifraud/suspicious-ip/{ip}")
    public ResponseEntity<Map<String, String>> deleteSuspiciousIp(
                                                                  @Validated @Size(min = 7) @PathVariable String ip) {
        return serviceOne.deleteSuspiciousIp(ip);
    }

    @DeleteMapping("/antifraud/stolencard/{number}")
    public ResponseEntity<Map<String, String>> deleteStolenCard(
            @Validated @PathVariable String number) {
        return serviceOne.deleteStolenCard(number);
    }

    @PutMapping("/auth/role")
    public ResponseEntity<UserDTO> changeRole(
            HttpServletResponse response, @Valid @RequestBody UserDTO userDTO) {
        return serviceOne.changeRole(userDTO.getUsername(), userDTO.getRole());
    }

    @PutMapping(path = "/auth/access", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> changeAccess(
            @Valid @RequestBody UserDTO2 userDTO2
    ) {
        return serviceOne.changeAccess(userDTO2);
    }

    @PutMapping(path = "/antifraud/transaction")
    public ResponseEntity<Transaction> addFeedback(@RequestBody TransactionDTO transactionDTO) {
        return transactionService.addFeedback(transactionDTO);
    }

    @GetMapping(path = "/antifraud/getcards")
       public ResponseEntity<List<Card>> getcards() {
        return transactionService.getcards();
    }

}
