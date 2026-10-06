package com.example.trabalho1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para atualização de um usuário existente")
public record UsuarioUpdateRequestDTO(

        @Schema(description = "Nome de exibição do usuário", example = "Bernardo B.")
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String nome,

        @Schema(description = "Novo email (deve continuar único)", example = "bernardo@vapor.com")
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
        String email,

        @Schema(description = "Nova senha; opcional. Se omitida, a senha atual é mantida",
                example = "novaSenha456", accessMode = Schema.AccessMode.WRITE_ONLY, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
        String senha
) {
}
