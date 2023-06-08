package antifraud.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cards")
public class Card {
    @Id
    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "maxAllowed")
    private long maxAllowed; //int = 200;

    @Column(name = "maxManual")
    private long maxManual; // int   = 1500;

}
