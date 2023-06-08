package antifraud.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity

@Table(name = "stolen_cards")
public class StolenCard {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    // @JsonIgnore
    private Long id;

    @Column(name = "number")
    @NotEmpty(message = "Number should not be empty")
    private String number;

    static int checkSumCreator(String binPlusaccountID) {

        int[] digits = new int[binPlusaccountID.length()];
        for (int i = 0; i < binPlusaccountID.length(); i++) {
            digits[i] = Character.getNumericValue(binPlusaccountID.charAt(i));
        }

        for (int i = 0; i < digits.length; i += 2) {
            digits[i] *= 2;
            if (digits[i] > 9) {
                digits[i] -= 9;
            }
        }

        int sum = 0;
        for (int i = 0; i < digits.length; i++) {
            sum += digits[i];
        }
        return (10 - sum % 10) % 10;
    }

    public static boolean passLuhnAlgorithmTest(String cardNumber) {
        return checkSumCreator(cardNumber.substring(0, 15)) == Integer.parseInt(cardNumber.substring(15));
    }

}
