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
                .andExpect(jsonPath("$.components.schemas.ErrorResponse.properties.message").exists());
    }
}
