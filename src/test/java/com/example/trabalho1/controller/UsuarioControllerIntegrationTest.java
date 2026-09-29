package com.example.trabalho1.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.trabalho1.dto.UsuarioRequestDTO;

import tools.jackson.databind.ObjectMapper;

/**
 * Testes de integração ponta a ponta (Controller -> Service -> Repository -> Postgres real),
 * complementando os testes unitários do {@code UsuarioServiceTest}, que cobrem as regras de
 * negócio isoladamente com mocks.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveCriarUsuarioERetornar201ComLocationESemSenha() throws Exception {
        String corpo = """
                {"nome":"Bernardo","email":"bernardo.criar@vapor.com","senha":"senha123"}
                """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/usuarios/\\d+")))
                .andExpect(jsonPath("$.email", is("bernardo.criar@vapor.com")))
                .andExpect(jsonPath("$.senha").doesNotExist());
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

    @Test
    void deveListarUsuariosComPaginacao() throws Exception {
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

        mockMvc.perform(get("/api/usuarios").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()", is(2)))
                .andExpect(jsonPath("$.page.size", is(2)))
                .andExpect(jsonPath("$.page.totalElements", is(3)));
    }

    @Test
    void deveBuscarPorIdRetornando200EDadosSemSenha() throws Exception {
        Long id = criarUsuarioEExtrairId("busca@vapor.com");

        mockMvc.perform(get("/api/usuarios/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void deveRetornar404ParaIdInexistente() throws Exception {
        mockMvc.perform(get("/api/usuarios/{id}", 999_999))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarUsuarioERetornar200() throws Exception {
        Long id = criarUsuarioEExtrairId("atualizar@vapor.com");

        String corpoAtualizacao = """
                {"nome":"Bernardo Atualizado","email":"atualizar@vapor.com","senha":"novaSenha456"}
                """;

        mockMvc.perform(put("/api/usuarios/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAtualizacao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Bernardo Atualizado")));

        String loginComSenhaNova = """
                {"email":"atualizar@vapor.com","senha":"novaSenha456"}
                """;
        mockMvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON).content(loginComSenhaNova))
                .andExpect(status().isOk());
    }

    @Test
    void deveDeletarUsuarioERetornar204EDepoisNaoEncontrar() throws Exception {
        Long id = criarUsuarioEExtrairId("deletar@vapor.com");

        mockMvc.perform(delete("/api/usuarios/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/usuarios/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveLogarComSucesso() throws Exception {
        criarUsuarioEExtrairId("login@vapor.com");

        String corpoLogin = """
                {"email":"login@vapor.com","senha":"senha123"}
                """;

        mockMvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON).content(corpoLogin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("login@vapor.com")));
    }

    @Test
    void deveRetornar401ParaLoginComSenhaErrada() throws Exception {
        criarUsuarioEExtrairId("loginerrado@vapor.com");

        String corpoLogin = """
                {"email":"loginerrado@vapor.com","senha":"senhaErrada"}
                """;

        mockMvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON).content(corpoLogin))
                .andExpect(status().isUnauthorized());
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
