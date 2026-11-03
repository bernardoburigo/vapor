package com.example.trabalho1.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.trabalho1.dto.UsuarioRequestDTO;
import com.example.trabalho1.repository.UsuarioRepository;
import com.example.trabalho1.support.AuthTestHelper;
import com.example.trabalho1.support.AuthTestHelper.Credencial;

import tools.jackson.databind.ObjectMapper;

/**
 * Testes de integração ponta a ponta (Security -> Controller -> Service -> Repository -> Postgres real),
 * complementando os testes unitários do {@code UsuarioServiceTest}, que cobrem as regras de negócio com mocks.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(AuthTestHelper.class)
class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthTestHelper auth;

    private Credencial admin;

    @BeforeEach
    void setUp() {
        admin = auth.admin();
    }

    // -------------------------------------------------------------------------
    // cadastro (público)
    // -------------------------------------------------------------------------

    @Test
    void deveCriarUsuarioSemTokenERetornar201ComLocationESemSenha() throws Exception {
        String corpo = """
                {"nome":"Bernardo","email":"bernardo.criar@vapor.com","senha":"senha123"}
                """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/usuarios/\\d+")))
                .andExpect(jsonPath("$.email", is("bernardo.criar@vapor.com")))
                .andExpect(jsonPath("$.role", is("USER")))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void cadastroPublicoNaoPermiteEscolherOPerfil() throws Exception {
        String corpo = """
                {"nome":"Esperto","email":"esperto@vapor.com","senha":"senha123","role":"ADMIN"}
                """;

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    void deveNormalizarEmailParaMinusculas() throws Exception {
        String corpo = """
                {"nome":"Bernardo","email":"Maiusculas@Vapor.COM","senha":"senha123"}
                """;

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("maiusculas@vapor.com")));
    }

    @Test
    void deveRetornar409AoCriarComEmailDuplicado() throws Exception {
        String corpo = """
                {"nome":"Bernardo","email":"duplicado@vapor.com","senha":"senha123"}
                """;

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Email já cadastrado")));
    }

    @Test
    void deveRetornar400ParaDadosInvalidosComFieldErrors() throws Exception {
        String corpo = """
                {"nome":"","email":"nao-e-email","senha":"123"}
                """;

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nome").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.senha").exists());
    }

    // -------------------------------------------------------------------------
    // listagem (ADMIN)
    // -------------------------------------------------------------------------

    @Test
    void deveListarUsuariosComPaginacaoComoAdmin() throws Exception {
        for (int i = 0; i < 3; i++) {
            UsuarioRequestDTO dto = new UsuarioRequestDTO(
                    "Usuario " + i,
                    "paginacao" + java.util.UUID.randomUUID() + "@vapor.com",
                    "senha123");
            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/usuarios").param("page", "0").param("size", "2")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()", is(2)))
                .andExpect(jsonPath("$.page.size", is(2)))
                .andExpect(jsonPath("$.page.totalElements", is((int) usuarioRepository.count())));
    }

    @Test
    void deveRetornar401AoListarSemToken() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", containsString("token")));
    }

    @Test
    void deveRetornar403AoListarComoUser() throws Exception {
        mockMvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, auth.user().bearer()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    // -------------------------------------------------------------------------
    // consulta, atualização e remoção por id
    // -------------------------------------------------------------------------

    @Test
    void adminDeveBuscarQualquerUsuarioSemExporSenha() throws Exception {
        Long id = criarUsuarioEExtrairId("busca@vapor.com");

        mockMvc.perform(get("/api/usuarios/{id}", id).header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void userDeveBuscarOProprioPerfil() throws Exception {
        Credencial user = auth.user();

        mockMvc.perform(get("/api/usuarios/{id}", user.id()).header(HttpHeaders.AUTHORIZATION, user.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.id().intValue())));
    }

    @Test
    void userNaoPodeBuscarPerfilDeOutroUsuario() throws Exception {
        Credencial user = auth.user();
        Credencial outro = auth.user();

        mockMvc.perform(get("/api/usuarios/{id}", outro.id()).header(HttpHeaders.AUTHORIZATION, user.bearer()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar404ParaIdInexistente() throws Exception {
        mockMvc.perform(get("/api/usuarios/{id}", 999_999).header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .andExpect(status().isNotFound());
    }

    @Test
    void userDeveAtualizarOProprioPerfilETrocarASenha() throws Exception {
        Credencial user = auth.user();

        String corpoAtualizacao = """
                {"nome":"Nome Atualizado","email":"%s","senha":"novaSenha456"}
                """.formatted(user.usuario().getEmail());

        mockMvc.perform(put("/api/usuarios/{id}", user.id())
                        .header(HttpHeaders.AUTHORIZATION, user.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAtualizacao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Nome Atualizado")));

        String loginComSenhaNova = """
                {"email":"%s","senha":"novaSenha456"}
                """.formatted(user.usuario().getEmail());
        mockMvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON).content(loginComSenhaNova))
                .andExpect(status().isOk());
    }

    @Test
    void userNaoPodeAtualizarOutroUsuario() throws Exception {
        Credencial user = auth.user();
        Credencial vitima = auth.user();

        String corpo = """
                {"nome":"Invasor","email":"%s"}
                """.formatted(vitima.usuario().getEmail());

        mockMvc.perform(put("/api/usuarios/{id}", vitima.id())
                        .header(HttpHeaders.AUTHORIZATION, user.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDeveDeletarUsuarioERetornar204EDepoisNaoEncontrar() throws Exception {
        Long id = criarUsuarioEExtrairId("deletar@vapor.com");

        mockMvc.perform(delete("/api/usuarios/{id}", id).header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/usuarios/{id}", id).header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .andExpect(status().isNotFound());
    }

    @Test
    void userNaoPodeDeletarUsuario() throws Exception {
        Credencial user = auth.user();

        mockMvc.perform(delete("/api/usuarios/{id}", user.id()).header(HttpHeaders.AUTHORIZATION, user.bearer()))
                .andExpect(status().isForbidden());
    }

    @Test
    void listagemDeUsuariosComOrdenacaoInvalidaRetorna400() throws Exception {
        mockMvc.perform(get("/api/usuarios").param("sort", "naoexiste")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("ordenação")));
    }

    // -------------------------------------------------------------------------
    // login
    // -------------------------------------------------------------------------

    @Test
    void deveLogarRetornandoTokenJwtEUsuario() throws Exception {
        criarUsuarioEExtrairId("login@vapor.com");

        String corpoLogin = """
                {"email":"login@vapor.com","senha":"senha123"}
                """;

        mockMvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON).content(corpoLogin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyOrNullString())))
                .andExpect(jsonPath("$.tipo", is("Bearer")))
                .andExpect(jsonPath("$.expiraEmSegundos", is(3600)))
                .andExpect(jsonPath("$.usuario.email", is("login@vapor.com")))
                .andExpect(jsonPath("$.usuario.senha").doesNotExist());
    }

    @Test
    void tokenRetornadoNoLoginDeveAutenticarAsProximasRequisicoes() throws Exception {
        Long id = criarUsuarioEExtrairId("fluxo@vapor.com");

        String resposta = mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"fluxo@vapor.com","senha":"senha123"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(resposta).get("token").asString();

        mockMvc.perform(get("/api/usuarios/{id}", id).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("fluxo@vapor.com")));
    }

    @Test
    void deveLogarIgnorandoMaiusculasNoEmail() throws Exception {
        criarUsuarioEExtrairId("caixa@vapor.com");

        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"CAIXA@Vapor.com","senha":"senha123"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornar401ParaLoginComSenhaErrada() throws Exception {
        criarUsuarioEExtrairId("loginerrado@vapor.com");

        String corpoLogin = """
                {"email":"loginerrado@vapor.com","senha":"senhaErrada"}
                """;

        mockMvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON).content(corpoLogin))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Email ou senha inválidos")));
    }

    // -------------------------------------------------------------------------
    // erros de requisição
    // -------------------------------------------------------------------------

    @Test
    void deveRetornar400ParaCorpoJsonMalformado() throws Exception {
        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content("{nome: "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Corpo da requisição inválido ou malformado")));
    }

    @Test
    void deveRetornar400ParaIdNaoNumerico() throws Exception {
        mockMvc.perform(get("/api/usuarios/{id}", "abc").header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("id")));
    }

    private Long criarUsuarioEExtrairId(String email) throws Exception {
        String corpo = """
                {"nome":"Bernardo","email":"%s","senha":"senha123"}
                """.formatted(email);

        String resposta = mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(resposta).get("id").asLong();
    }
}
