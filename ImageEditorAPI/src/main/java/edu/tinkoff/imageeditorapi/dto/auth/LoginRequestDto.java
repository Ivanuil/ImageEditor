package edu.tinkoff.imageeditorapi.dto.auth;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoginRequestDto {

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;
}
