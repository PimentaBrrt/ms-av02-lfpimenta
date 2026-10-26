package lfpimenta.microservicos.p2.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Credenciais do usuário")
public class LoginRequest {

    @Schema(example = "maria")
    @NotBlank(message = "Username é obrigatório")
    @Size(max = 50, message = "Username deve ter no máximo 50 caracteres")
    public String username;

    @Schema(example = "maria")
    @NotBlank(message = "Senha é obrigatória")
    @Size(max = 10, message = "Senha deve ter no máximo 10 caracteres")
    public String senha;

}
