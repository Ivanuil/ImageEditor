package edu.tinkoff.imageeditor.dto.auth;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {

    @NotEmpty
    private String username;

    @NotEmpty
    @Size(min = 5, max = 15)
    private String password;
}
