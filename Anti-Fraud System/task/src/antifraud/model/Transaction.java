package antifraud.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @JsonIgnore
    private long id;

    @Min(1)
    private long amount;

    @Column(name = "ip")
    @NotEmpty(message = "Ip should not be empty")
    private String ip;

    @Column(name = "number")
    @NotEmpty(message = "Number should not be empty")
    private  String number;

    @Column(name = "region")
    @NotEmpty(message = "Region should not be empty")
    private  String region;

    @Column(name = "date")
    @NotNull(message = "Date should not be null" )
    private LocalDateTime date;

    @Column(name = "transaction_validity")
    @JsonIgnore
    private String transactionValidity;

    @Column(name = "feedback")
    @JsonIgnore
    private String feedback = "";

}
