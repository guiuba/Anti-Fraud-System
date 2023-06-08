package antifraud.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Result {
    private String result;

    @JsonIgnore
    private Long id;


}
