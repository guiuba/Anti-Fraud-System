package antifraud.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.validation.constraints.NotEmpty;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDTO2 {

    @NotEmpty(message = "Username should not be empty")
    private String username;
    @NotEmpty(message = "Operation should not be empty")
    private String operation;
}