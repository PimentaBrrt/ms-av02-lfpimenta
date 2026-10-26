package lfpimenta.microservicos.p2.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Senha atual e nova senha")
public class TrocarSenhaRequest {

    @Schema(example = "maria")
    @NotBlank(message = "Senha atual é obrigatória")
    public String atual;

    @Schema(example = "nova123")
    @NotBlank(message = "Nova senha é obrigatória")
    @Size(max = 10, message = "Nova senha deve ter no máximo 10 caracteres")
    public String nova;

}
