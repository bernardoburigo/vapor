package com.example.trabalho1.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.trabalho1.dto.DesenvolvedoraRequestDTO;
import com.example.trabalho1.dto.DesenvolvedoraResponseDTO;
import com.example.trabalho1.exception.ErrorResponse;
import com.example.trabalho1.service.DesenvolvedoraService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Desenvolvedoras", description = "Cadastro de estúdios e publicadoras de jogos")
@RestController
@RequestMapping("/api/desenvolvedoras")
@RequiredArgsConstructor
public class DesenvolvedoraController {

    private final DesenvolvedoraService desenvolvedoraService;

    @Operation(summary = "Cadastrar desenvolvedora",
            description = "Cria uma desenvolvedora. O nome não pode se repetir (409 se já existir). Exige perfil ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Desenvolvedora criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão (exige ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe desenvolvedora com esse nome",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<DesenvolvedoraResponseDTO> criar(
            @Valid @RequestBody DesenvolvedoraRequestDTO dto) {
        DesenvolvedoraResponseDTO desenvolvedora = desenvolvedoraService.criar(dto);
        return ResponseEntity.created(URI.create("/api/desenvolvedoras/" + desenvolvedora.id()))
                .body(desenvolvedora);
    }

    @Operation(summary = "Listar desenvolvedoras",
            description = "Lista todas, ou filtra por parte do nome (?nome=) ou por país (?paisOrigem=). "
                    + "Se os dois forem enviados, vale o nome. Endpoint público.")
    @GetMapping
    public ResponseEntity<List<DesenvolvedoraResponseDTO>> listar(
            @Parameter(description = "Parte do nome", example = "valve")
            @RequestParam(required = false) String nome,
            @Parameter(description = "País de origem (igualdade, sem diferenciar maiúsculas)", example = "Estados Unidos")
            @RequestParam(required = false) String paisOrigem) {
        return ResponseEntity.ok(desenvolvedoraService.listar(nome, paisOrigem));
    }

    @Operation(summary = "Buscar desenvolvedora por id", description = "Endpoint público.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Desenvolvedora encontrada"),
            @ApiResponse(responseCode = "404", description = "Desenvolvedora não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<DesenvolvedoraResponseDTO> buscarPorId(
            @Parameter(description = "Identificador da desenvolvedora", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(desenvolvedoraService.buscarPorId(id));
    }

    @Operation(summary = "Atualizar desenvolvedora", description = "Exige perfil ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Desenvolvedora atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão (exige ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Desenvolvedora não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe outra desenvolvedora com esse nome",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<DesenvolvedoraResponseDTO> atualizar(
            @Parameter(description = "Identificador da desenvolvedora", example = "1") @PathVariable Long id,
            @Valid @RequestBody DesenvolvedoraRequestDTO dto) {
        return ResponseEntity.ok(desenvolvedoraService.atualizar(id, dto));
    }

    @Operation(summary = "Remover desenvolvedora",
            description = "Remove a desenvolvedora. Retorna 409 se ela ainda tiver jogos cadastrados. Exige perfil ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Desenvolvedora removida"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão (exige ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Desenvolvedora não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Desenvolvedora possui jogos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @Parameter(description = "Identificador da desenvolvedora", example = "1") @PathVariable Long id) {
        desenvolvedoraService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
