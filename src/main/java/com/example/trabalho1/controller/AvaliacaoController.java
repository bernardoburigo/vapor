package com.example.trabalho1.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.trabalho1.dto.AvaliacaoRequestDTO;
import com.example.trabalho1.dto.AvaliacaoResponseDTO;
import com.example.trabalho1.dto.AvaliacaoUpdateRequestDTO;
import com.example.trabalho1.exception.ErrorResponse;
import com.example.trabalho1.security.UsuarioPrincipal;
import com.example.trabalho1.service.AvaliacaoService;

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

@Tag(name = "Avaliações", description = "Notas e comentários dos usuários sobre os jogos (um usuário avalia cada jogo uma única vez)")
@RestController
@RequestMapping("/api/avaliacoes")
@RequiredArgsConstructor
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    @Operation(summary = "Avaliar jogo",
            description = "Cria a avaliação do usuário autenticado para um jogo. "
                    + "Cada usuário só pode avaliar o mesmo jogo uma vez (409 se repetir).")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Avaliação criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (nota fora de 1 a 5, jogo ausente)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Usuário já avaliou este jogo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<AvaliacaoResponseDTO> criar(
            @Valid @RequestBody AvaliacaoRequestDTO dto,
            @AuthenticationPrincipal UsuarioPrincipal usuarioLogado) {
        AvaliacaoResponseDTO avaliacao = avaliacaoService.criar(usuarioLogado.getId(), dto);
        return ResponseEntity.created(URI.create("/api/avaliacoes/" + avaliacao.id())).body(avaliacao);
    }

    @Operation(summary = "Listar avaliações",
            description = "Lista todas as avaliações, ou filtra por jogo (?jogoId=) ou por usuário (?usuarioId=). "
                    + "Se os dois filtros forem enviados, vale o de jogo. Endpoint público.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de avaliações"),
            @ApiResponse(responseCode = "404", description = "Jogo ou usuário do filtro não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<AvaliacaoResponseDTO>> listarTodas(
            @Parameter(description = "Filtra pelo jogo. Ex.: 1")
            @RequestParam(required = false) Long jogoId,
            @Parameter(description = "Filtra pelo autor da avaliação. Ex.: 1")
            @RequestParam(required = false) Long usuarioId) {

        if (jogoId != null) {
            return ResponseEntity.ok(avaliacaoService.listarPorJogo(jogoId));
        }
        if (usuarioId != null) {
            return ResponseEntity.ok(avaliacaoService.listarPorUsuario(usuarioId));
        }
        return ResponseEntity.ok(avaliacaoService.listarTodas());
    }

    @Operation(summary = "Buscar avaliação por id", description = "Endpoint público.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avaliação encontrada"),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AvaliacaoResponseDTO> buscarPorId(
            @Parameter(description = "Identificador da avaliação") @PathVariable Long id) {
        return ResponseEntity.ok(avaliacaoService.buscarPorId(id));
    }

    @Operation(summary = "Alterar avaliação",
            description = "Altera nota e comentário. Permitido apenas ao autor da avaliação ou a um ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avaliação atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "A avaliação pertence a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<AvaliacaoResponseDTO> atualizar(
            @Parameter(description = "Identificador da avaliação") @PathVariable Long id,
            @Valid @RequestBody AvaliacaoUpdateRequestDTO dto,
            @AuthenticationPrincipal UsuarioPrincipal usuarioLogado) {
        return ResponseEntity.ok(avaliacaoService.atualizar(id, dto, usuarioLogado));
    }

    @Operation(summary = "Remover avaliação",
            description = "Permitido apenas ao autor da avaliação ou a um ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Avaliação removida"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "A avaliação pertence a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @Parameter(description = "Identificador da avaliação") @PathVariable Long id,
            @AuthenticationPrincipal UsuarioPrincipal usuarioLogado) {
        avaliacaoService.deletar(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }
}
