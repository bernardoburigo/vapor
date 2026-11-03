package com.example.trabalho1.config;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveExporDocumentacaoOpenApiDaApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title", is("VAPOR API")))
                .andExpect(jsonPath("$.paths['/api/usuarios']").exists())
                .andExpect(jsonPath("$.paths['/api/usuarios/{id}']").exists());
    }

    @Test
    void deveDocumentarOsCincoRecursosDaApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/usuarios']").exists())
                .andExpect(jsonPath("$.paths['/api/desenvolvedoras']").exists())
                .andExpect(jsonPath("$.paths['/api/desenvolvedoras/{id}']").exists())
                .andExpect(jsonPath("$.paths['/api/categorias']").exists())
                .andExpect(jsonPath("$.paths['/api/categorias/{id}']").exists())
                .andExpect(jsonPath("$.paths['/api/jogos']").exists())
                .andExpect(jsonPath("$.paths['/api/jogos/{id}']").exists())
                .andExpect(jsonPath("$.paths['/api/avaliacoes']").exists())
                .andExpect(jsonPath("$.paths['/api/avaliacoes/{id}']").exists());
    }

    @Test
    void todosOsEndpointsTemTagEResumo() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/jogos'].get.tags[0]", is("Jogos")))
                .andExpect(jsonPath("$.paths['/api/categorias'].post.tags[0]", is("Categorias")))
                .andExpect(jsonPath("$.paths['/api/desenvolvedoras'].get.tags[0]", is("Desenvolvedoras")))
                .andExpect(jsonPath("$.paths['/api/avaliacoes'].post.tags[0]", is("Avaliações")))
                .andExpect(jsonPath("$.paths['/api/avaliacoes'].post.summary", is("Avaliar jogo")));
    }

    @Test
    void deveDeclararEsquemaBearerJwt() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type", is("http")))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme", is("bearer")))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat", is("JWT")));
    }

    @Test
    void endpointsPublicosNaoExigemTokenEProtegidosExigem() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/usuarios'].post.security", hasSize(0)))
                .andExpect(jsonPath("$.paths['/api/usuarios/login'].post.security", hasSize(0)))
                .andExpect(jsonPath("$.paths['/api/usuarios'].get.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/usuarios/{id}'].put.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/usuarios/{id}'].delete.security[0].bearerAuth").exists());
    }

    @Test
    void escritasDoCatalogoEDasAvaliacoesExigemTokenELeiturasSaoPublicas() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                // escrita: bearerAuth
                .andExpect(jsonPath("$.paths['/api/jogos'].post.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/jogos/{id}'].put.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/jogos/{id}'].delete.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/categorias'].post.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/desenvolvedoras'].post.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/avaliacoes'].post.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/avaliacoes/{id}'].put.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/avaliacoes/{id}'].delete.security[0].bearerAuth").exists())
                // leitura: sem cadeado
                .andExpect(jsonPath("$.paths['/api/jogos'].get.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/jogos/{id}'].get.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/categorias'].get.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/desenvolvedoras'].get.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/avaliacoes'].get.security").doesNotExist());
    }

    @Test
    void respostaDeUsuarioNaDocumentacaoNaoExpoeSenha() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.UsuarioResponseDTO.properties.email").exists())
                .andExpect(jsonPath("$.components.schemas.UsuarioResponseDTO.properties.senha").doesNotExist());
    }

    @Test
    void documentaRespostasDeErroComOFormatoPadrao() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/usuarios/{id}'].get.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/api/usuarios'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/api/jogos'].post.responses['403']").exists())
                .andExpect(jsonPath("$.paths['/api/avaliacoes/{id}'].put.responses['403']").exists())
                .andExpect(jsonPath("$.paths['/api/avaliacoes'].post.responses['409']").exists())
                .andExpect(jsonPath("$.components.schemas.ErrorResponse.properties.message").exists());
    }

    @Test
    void loginDocumentaOTokenJwtRetornado() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.AuthResponseDTO.properties.token").exists())
                .andExpect(jsonPath("$.components.schemas.AuthResponseDTO.properties.usuario").exists());
    }

    @Test
    void avaliacaoNaoPedeOUsuarioNoCorpoPoisVemDoToken() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.AvaliacaoRequestDTO.properties.jogoId").exists())
                .andExpect(jsonPath("$.components.schemas.AvaliacaoRequestDTO.properties.usuarioId").doesNotExist());
    }

    @Test
    void jogoDocumentaNotaMediaETotalDeAvaliacoes() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.JogoResponseDTO.properties.notaMedia").exists())
                .andExpect(jsonPath("$.components.schemas.JogoResponseDTO.properties.totalAvaliacoes").exists());
    }

    @Test
    void listagensPaginadasExpoemPageSizeESortNoLugarDeUmObjetoPageable() throws Exception {
        for (String listagem : new String[] {"/api/jogos", "/api/usuarios"}) {
            String base = "$.paths['" + listagem + "'].get.parameters";
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(base + "[?(@.name=='page')]").isNotEmpty())
                    .andExpect(jsonPath(base + "[?(@.name=='size')]").isNotEmpty())
                    .andExpect(jsonPath(base + "[?(@.name=='sort')]").isNotEmpty())
                    .andExpect(jsonPath(base + "[?(@.name=='pageable')]").isEmpty());
        }
    }

    @Test
    void parametrosNaoVemPreenchidosComExemploParaNaoSeremEnviadosSemQuerer() throws Exception {
        // O Swagger UI copia "example" de parâmetro para o campo; um DELETE com id pré-preenchido é um risco ao vivo.
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/jogos'].get.parameters[?(@.name=='categoriaId')].example").isEmpty())
                .andExpect(jsonPath("$.paths['/api/jogos'].get.parameters[?(@.name=='titulo')].example").isEmpty())
                .andExpect(jsonPath("$.paths['/api/jogos/{id}'].delete.parameters[?(@.name=='id')].example").isEmpty())
                .andExpect(jsonPath("$.paths['/api/usuarios/{id}'].delete.parameters[?(@.name=='id')].example").isEmpty())
                .andExpect(jsonPath("$.paths['/api/avaliacoes/{id}'].delete.parameters[?(@.name=='id')].example").isEmpty());
    }

    @Test
    void loginOfereceExemplosProntosDeAdminEUser() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/usuarios/login'].post.requestBody.content['application/json']"
                        + ".examples['ADMIN (demonstração)']").exists())
                .andExpect(jsonPath("$.paths['/api/usuarios/login'].post.requestBody.content['application/json']"
                        + ".examples['USER (demonstração)']").exists());
    }
}
