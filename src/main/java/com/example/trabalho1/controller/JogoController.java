package com.example.trabalho1.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import com.example.trabalho1.dto.JogoRequestDTO;
import com.example.trabalho1.dto.JogoResponseDTO;
import com.example.trabalho1.exception.ErrorResponse;
import com.example.trabalho1.service.JogoService;

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

@Tag(name = "Jogos", description = "Catálogo de jogos, com filtros por categoria e desenvolvedora e nota média")
@RestController
@RequestMapping("/api/jogos")
@RequiredArgsConstructor
public class JogoController {

    private final JogoService jogoService;

    @Operation(summary = "Cadastrar jogo",
            description = "Cria um jogo vinculado a uma desenvolvedora e a uma ou mais categorias. Exige perfil ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Jogo criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão (exige ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Desenvolvedora ou categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<JogoResponseDTO> criar(@Valid @RequestBody JogoRequestDTO dto) {
        JogoResponseDTO jogo = jogoService.criar(dto);
        return ResponseEntity.created(URI.create("/api/jogos/" + jogo.id())).body(jogo);
    }

    @Operation(summary = "Listar jogos",
            description = "Lista jogos paginados. Os filtros são opcionais e combináveis: categoria, desenvolvedora "
                    + "e parte do título. Parâmetros de página: page (padrão 0), size (padrão 20), "
                    + "sort (padrão titulo,asc). Endpoint público.")
    @GetMapping
    public ResponseEntity<Page<JogoResponseDTO>> listar(
            @Parameter(description = "Filtra pela categoria", example = "1")
            @RequestParam(required = false) Long categoriaId,
            @Parameter(description = "Filtra pela desenvolvedora", example = "1")
            @RequestParam(required = false) Long desenvolvedoraId,
            @Parameter(description = "Parte do título (sem diferenciar maiúsculas)", example = "half")
            @RequestParam(required = false) String titulo,
            @PageableDefault(size = 20, sort = "titulo") Pageable pageable) {
        return ResponseEntity.ok(jogoService.listar(categoriaId, desenvolvedoraId, titulo, pageable));
    }

    @Operation(summary = "Buscar jogo por id",
            description = "Retorna o jogo com a nota média e o total de avaliações. Endpoint público.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Jogo encontrado"),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<JogoResponseDTO> buscarPorId(
            @Parameter(description = "Identificador do jogo", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(jogoService.buscarPorId(id));
    }

    @Operation(summary = "Atualizar jogo",
            description = "Substitui os dados do jogo, inclusive a lista de categorias. Exige perfil ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Jogo atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão (exige ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Jogo, desenvolvedora ou categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<JogoResponseDTO> atualizar(
            @Parameter(description = "Identificador do jogo", example = "1") @PathVariable Long id,
            @Valid @RequestBody JogoRequestDTO dto) {
        return ResponseEntity.ok(jogoService.atualizar(id, dto));
    }

    @Operation(summary = "Remover jogo",
            description = "Remove o jogo. Retorna 409 se ele já tiver avaliações. Exige perfil ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Jogo removido"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão (exige ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Jogo possui avaliações",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @Parameter(description = "Identificador do jogo", example = "1") @PathVariable Long id) {
        jogoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
