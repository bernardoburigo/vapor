package com.example.trabalho1.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

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

import com.example.trabalho1.support.AuthTestHelper;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(AuthTestHelper.class)
class CategoriaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthTestHelper auth;

    private String admin;

    @BeforeEach
    void setUp() {
        admin = auth.admin().bearer();
    }

    @Test
    void adminDeveCriarCategoriaRetornando201ComLocation() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(nomeUnico(), "Jogos de interpretação de papéis")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/categorias/\\d+")))
                .andExpect(jsonPath("$.descricao", is("Jogos de interpretação de papéis")));
    }

    @Test
    void deveRetornar409ParaNomeDuplicadoIgnorandoMaiusculas() throws Exception {
        String nome = nomeUnico();
        criar(nome);

        mockMvc.perform(post("/api/categorias")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(nome.toUpperCase(), null)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar400ParaNomeVazioOuMuitoLongo() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("", "x".repeat(300))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nome").exists())
                .andExpect(jsonPath("$.fieldErrors.descricao").exists());
    }

    @Test
    void escritaExigeTokenEPerfilAdmin() throws Exception {
        Long id = criar(nomeUnico());

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Sem Token", null)))
                .andExpect(status().isUnauthorized());

        String user = auth.user().bearer();
        mockMvc.perform(put("/api/categorias/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Intruso", null)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/categorias/{id}", id).header(HttpHeaders.AUTHORIZATION, user))
                .andExpect(status().isForbidden());
    }

    @Test
    void leituraEPublicaEFiltraPorNome() throws Exception {
        String marca = "zz" + UUID.randomUUID().toString().substring(0, 8);
        criar("Estrategia " + marca);
        criar("Corrida " + UUID.randomUUID().toString().substring(0, 10));

        mockMvc.perform(get("/api/categorias").param("nome", marca.toUpperCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].nome", is("Estrategia " + marca)));
    }

    @Test
    void deveRetornar404ParaIdInexistente() throws Exception {
        mockMvc.perform(get("/api/categorias/{id}", 999_999)).andExpect(status().isNotFound());
    }

    @Test
    void adminDeveAtualizarEDeletar() throws Exception {
        Long id = criar(nomeUnico());

        mockMvc.perform(put("/api/categorias/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(nomeUnico(), "Descrição nova")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao", is("Descrição nova")));

        mockMvc.perform(delete("/api/categorias/{id}", id).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/categorias/{id}", id)).andExpect(status().isNotFound());
    }

    private String nomeUnico() {
        return "Cat " + UUID.randomUUID().toString().substring(0, 18);
    }

    private String corpo(String nome, String descricao) {
        java.util.Map<String, Object> mapa = new java.util.LinkedHashMap<>();
        mapa.put("nome", nome);
        mapa.put("descricao", descricao);
        return objectMapper.writeValueAsString(mapa);
    }

    private Long criar(String nome) throws Exception {
        String resposta = mockMvc.perform(post("/api/categorias")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(nome, null)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
    }
}
