package com.example.trabalho1.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.trabalho1.dto.AuthResponseDTO;
import com.example.trabalho1.dto.LoginRequestDTO;
import com.example.trabalho1.dto.UsuarioRequestDTO;
import com.example.trabalho1.dto.UsuarioResponseDTO;
import com.example.trabalho1.dto.UsuarioUpdateRequestDTO;
import com.example.trabalho1.exception.ErrorResponse;
import com.example.trabalho1.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Usuários", description = "Cadastro, perfil e autenticação de usuários")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private static final String EXEMPLO_USUARIO = """
            {"id": 1, "nome": "Bernardo Búrigo", "email": "bernardo@vapor.com", "role": "USER", "dataCadastro": "2026-10-06T14:30:00"}
            """;

    private static final String EXEMPLO_ERRO_VALIDACAO = """
            {"timestamp": "2026-10-06T14:30:00", "status": 400, "error": "Bad Request", "message": "Dados inválidos",
             "fieldErrors": {"email": "Email inválido", "senha": "Senha deve ter entre 6 e 100 caracteres"}}
            """;

    private static final String EXEMPLO_ERRO_NAO_ENCONTRADO = """
            {"timestamp": "2026-10-06T14:30:00", "status": 404, "error": "Not Found", "message": "Usuário não encontrado com id 99", "fieldErrors": null}
            """;

    private static final String EXEMPLO_ERRO_CONFLITO = """
            {"timestamp": "2026-10-06T14:30:00", "status": 409, "error": "Conflict", "message": "Email já cadastrado", "fieldErrors": null}
            """;

    private static final String EXEMPLO_ERRO_CREDENCIAIS = """
            {"timestamp": "2026-10-06T14:30:00", "status": 401, "error": "Unauthorized", "message": "Email ou senha inválidos", "fieldErrors": null}
            """;

    private static final String EXEMPLO_ERRO_ACESSO_NEGADO = """
            {"timestamp": "2026-10-06T14:30:00", "status": 403, "error": "Forbidden", "message": "Acesso negado: seu perfil não tem permissão para este recurso", "fieldErrors": null}
            """;

    // Credenciais de DESENVOLVIMENTO (as mesmas do README), para o login funcionar com um clique em Execute.
    // O USER de demonstração só existe depois de rodar `npm run seed` no frontend. Remova em produção.
    private static final String EXEMPLO_LOGIN_ADMIN = """
            {"email": "admin@vapor.com", "senha": "admin123"}
            """;

    private static final String EXEMPLO_LOGIN_USER = """
            {"email": "ana.demo@vapor.com", "senha": "demo1234"}
            """;

    private static final String EXEMPLO_LOGIN = """
            {"token": "eyJhbGciOiJIUzI1NiJ9...", "tipo": "Bearer", "expiraEmSegundos": 3600,
             "usuario": {"id": 1, "nome": "Bernardo Búrigo", "email": "bernardo@vapor.com", "role": "USER", "dataCadastro": "2026-10-06T14:30:00"}}
            """;

    private final UsuarioService usuarioService;

    @Operation(summary = "Cadastrar usuário",
            description = "Cria um novo usuário com perfil USER. O email deve ser único e a senha é armazenada com hash.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado",
                    headers = @Header(
                            name = "Location", description = "URI do usuário criado",
                            schema = @Schema(type = "string", example = "/api/usuarios/1")),
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponseDTO.class),
                            examples = @ExampleObject(name = "Usuário criado", value = EXEMPLO_USUARIO))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Validação", value = EXEMPLO_ERRO_VALIDACAO))),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Email duplicado", value = EXEMPLO_ERRO_CONFLITO)))
    })
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO usuario = usuarioService.criar(dto);
        return ResponseEntity.created(URI.create("/api/usuarios/" + usuario.id())).body(usuario);
    }

    @Operation(summary = "Listar usuários",
            description = "Lista usuários paginados. Parâmetros: page (padrão 0), size (padrão 20), sort (padrão nome,asc). Exige perfil ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de usuários",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Não autenticado", value = EXEMPLO_ERRO_CREDENCIAIS))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão (exige ADMIN)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Acesso negado", value = EXEMPLO_ERRO_ACESSO_NEGADO)))
    })
    @GetMapping
    public ResponseEntity<Page<UsuarioResponseDTO>> listarTodos(
            @ParameterObject @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(usuarioService.listarTodos(pageable));
    }

    @Operation(summary = "Buscar usuário por id",
            description = "Retorna os dados públicos de um usuário. Permitido ao próprio usuário ou a um ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponseDTO.class),
                            examples = @ExampleObject(name = "Usuário", value = EXEMPLO_USUARIO))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Só o próprio usuário ou um ADMIN pode consultar",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Acesso negado", value = EXEMPLO_ERRO_ACESSO_NEGADO))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Não encontrado", value = EXEMPLO_ERRO_NAO_ENCONTRADO)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(
            @Parameter(description = "Identificador do usuário") @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @Operation(summary = "Atualizar usuário",
            description = "Atualiza nome e email. A senha é trocada apenas se for informada. Permitido ao próprio usuário ou a um ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponseDTO.class),
                            examples = @ExampleObject(name = "Usuário atualizado", value = EXEMPLO_USUARIO))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Validação", value = EXEMPLO_ERRO_VALIDACAO))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Só o próprio usuário ou um ADMIN pode atualizar",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Acesso negado", value = EXEMPLO_ERRO_ACESSO_NEGADO))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Não encontrado", value = EXEMPLO_ERRO_NAO_ENCONTRADO))),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado para outro usuário",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Email duplicado", value = EXEMPLO_ERRO_CONFLITO)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(
            @Parameter(description = "Identificador do usuário") @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.atualizar(id, dto));
    }

    @Operation(summary = "Remover usuário",
            description = "Remove permanentemente um usuário. Retorna 409 se ele tiver avaliações. Exige perfil ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário removido"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão (exige ADMIN)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Acesso negado", value = EXEMPLO_ERRO_ACESSO_NEGADO))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Não encontrado", value = EXEMPLO_ERRO_NAO_ENCONTRADO))),
            @ApiResponse(responseCode = "409", description = "Usuário possui avaliações",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @Parameter(description = "Identificador do usuário") @PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Login",
            description = "Valida email e senha e retorna um token JWT (válido por 1 hora por padrão) junto com os dados do usuário. "
                    + "Envie o token nos demais endpoints no header Authorization: Bearer <token>.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciais válidas",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDTO.class),
                            examples = @ExampleObject(name = "Login", value = EXEMPLO_LOGIN))),
            @ApiResponse(responseCode = "400", description = "Campos obrigatórios ausentes",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Email ou senha inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Credenciais inválidas", value = EXEMPLO_ERRO_CREDENCIAIS)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "ADMIN (demonstração)", value = EXEMPLO_LOGIN_ADMIN),
                    @ExampleObject(name = "USER (demonstração)", value = EXEMPLO_LOGIN_USER)
            }))
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.login(dto));
    }
}
