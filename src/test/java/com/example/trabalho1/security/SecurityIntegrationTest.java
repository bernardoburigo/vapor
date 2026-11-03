package com.example.trabalho1.security;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.trabalho1.repository.UsuarioRepository;
import com.example.trabalho1.support.AuthTestHelper;
import com.example.trabalho1.support.AuthTestHelper.Credencial;

/** Comportamento da camada de segurança vista de fora: filtro JWT, 401/403 em JSON e rotas públicas. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(AuthTestHelper.class)
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthTestHelper auth;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${app.jwt.secret}")
    private String segredo;

    @Test
    void swaggerEDocumentacaoOpenApiSaoPublicos() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    @Test
    void endpointProtegidoSemTokenRetorna401ComCorpoPadrao() throws Exception {
        mockMvc.perform(get("/api/usuarios/{id}", 1))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", containsString("token")));
    }

    @Test
    void tokenAdulteradoRetorna401() throws Exception {
        Credencial admin = auth.admin();
        String adulterado = admin.bearer().substring(0, admin.bearer().length() - 3) + "abc";

        mockMvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, adulterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenExpiradoRetorna401() throws Exception {
        Credencial admin = auth.admin();
        String expirado = "Bearer " + new JwtService(segredo, -1).gerarToken(admin.usuario());

        mockMvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, expirado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenDeUsuarioRemovidoRetorna401() throws Exception {
        Credencial admin = auth.admin();
        Credencial removido = auth.user();
        usuarioRepository.delete(removido.usuario());
        usuarioRepository.flush();

        mockMvc.perform(get("/api/usuarios/{id}", removido.id())
                        .header(HttpHeaders.AUTHORIZATION, removido.bearer()))
                .andExpect(status().isUnauthorized());

        // o admin segue funcionando: só o token do usuário removido deixou de valer
        mockMvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .andExpect(status().isOk());
    }

    @Test
    void esquemaDiferenteDeBearerNaoAutentica() throws Exception {
        mockMvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, "Basic YWRtaW46YWRtaW4="))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidoEmEndpointPublicoNaoImpedeOAcesso() throws Exception {
        mockMvc.perform(get("/api/jogos").header(HttpHeaders.AUTHORIZATION, "Bearer lixo"))
                .andExpect(status().isOk());
    }

    @Test
    void userAutenticadoEmEndpointDeAdminRetorna403ComCorpoPadrao() throws Exception {
        mockMvc.perform(delete("/api/categorias/{id}", 1)
                        .header(HttpHeaders.AUTHORIZATION, auth.user().bearer()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    @Test
    void rotaInexistenteAutenticadaRetorna404EmJson() throws Exception {
        mockMvc.perform(get("/api/nao-existe").header(HttpHeaders.AUTHORIZATION, auth.user().bearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("api/nao-existe")));
    }

    @Test
    void metodoNaoSuportadoRetorna405ComHeaderAllow() throws Exception {
        mockMvc.perform(patch("/api/categorias/{id}", 1)
                        .header(HttpHeaders.AUTHORIZATION, auth.admin().bearer()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists(HttpHeaders.ALLOW))
                .andExpect(jsonPath("$.status", is(405)));
    }

    @Test
    void tipoDeConteudoNaoSuportadoRetorna415() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .header(HttpHeaders.AUTHORIZATION, auth.admin().bearer())
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("nome=RPG"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status", is(415)));
    }

    @Test
    void corsLiberaAsOrigensDoFrontendConfiguradas() throws Exception {
        mockMvc.perform(options("/api/jogos")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
    }

    @Test
    void corsBloqueiaOrigensNaoConfiguradas() throws Exception {
        mockMvc.perform(options("/api/jogos")
                        .header(HttpHeaders.ORIGIN, "http://site-malicioso.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }
}
