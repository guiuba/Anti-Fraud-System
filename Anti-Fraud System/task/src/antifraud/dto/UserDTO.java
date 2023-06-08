package antifraud.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDTO {
    private long id;
    private String name;
    @NotEmpty(message = "Username should not be empty")
    private String username;
    @NotEmpty(message = "Role should not be empty")
    private String role;
}
