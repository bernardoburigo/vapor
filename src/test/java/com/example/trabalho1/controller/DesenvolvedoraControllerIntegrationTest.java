package com.example.trabalho1.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
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
class DesenvolvedoraControllerIntegrationTest {

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
    void adminDeveCriarDesenvolvedoraRetornando201ComLocation() throws Exception {
        mockMvc.perform(post("/api/desenvolvedoras")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(nomeUnico("Valve"), "Estados Unidos", "https://www.valvesoftware.com")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/desenvolvedoras/\\d+")))
                .andExpect(jsonPath("$.paisOrigem", is("Estados Unidos")));
    }

    @Test
    void deveRetornar409ParaNomeDuplicadoIgnorandoMaiusculas() throws Exception {
        String nome = nomeUnico("Duplicada");
        criar(nome);

        mockMvc.perform(post("/api/desenvolvedoras")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(nome.toUpperCase(), null, null)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar400ParaNomeVazioESiteSemProtocolo() throws Exception {
        mockMvc.perform(post("/api/desenvolvedoras")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("", "Brasil", "www.sem-protocolo.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nome").exists())
                .andExpect(jsonPath("$.fieldErrors.site").exists());
    }

    @Test
    void escritaExigeToken() throws Exception {
        mockMvc.perform(post("/api/desenvolvedoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Sem Token", null, null)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void escritaExigePerfilAdmin() throws Exception {
        Long id = criar(nomeUnico("Protegida"));
        String user = auth.user().bearer();

        mockMvc.perform(post("/api/desenvolvedoras")
                        .header(HttpHeaders.AUTHORIZATION, user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Intruso", null, null)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/desenvolvedoras/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Intruso", null, null)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/desenvolvedoras/{id}", id).header(HttpHeaders.AUTHORIZATION, user))
                .andExpect(status().isForbidden());
    }

    @Test
    void leituraEPublicaEFiltraPorNomeEPais() throws Exception {
        String marca = "zz" + UUID.randomUUID().toString().substring(0, 8);
        criar("Alpha " + marca, "Pais " + marca);
        criar("Beta " + marca, "Pais " + marca);
        criar("Outra " + UUID.randomUUID(), "Outro Pais " + marca);

        mockMvc.perform(get("/api/desenvolvedoras").param("nome", marca.toUpperCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)));

        mockMvc.perform(get("/api/desenvolvedoras").param("paisOrigem", "pais " + marca))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome", containsInAnyOrder("Alpha " + marca, "Beta " + marca)));
    }

    @Test
    void deveRetornar404ParaIdInexistente() throws Exception {
        mockMvc.perform(get("/api/desenvolvedoras/{id}", 999_999)).andExpect(status().isNotFound());
    }

    @Test
    void adminDeveAtualizarEDeletar() throws Exception {
        Long id = criar(nomeUnico("Antiga"));

        mockMvc.perform(put("/api/desenvolvedoras/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Nome Novo " + UUID.randomUUID(), "Japão", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paisOrigem", is("Japão")));

        mockMvc.perform(delete("/api/desenvolvedoras/{id}", id).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/desenvolvedoras/{id}", id)).andExpect(status().isNotFound());
    }

    private String nomeUnico(String base) {
        return base + " " + UUID.randomUUID();
    }

    private String corpo(String nome, String pais, String site) {
        java.util.Map<String, Object> mapa = new java.util.LinkedHashMap<>();
        mapa.put("nome", nome);
        mapa.put("paisOrigem", pais);
        mapa.put("site", site);
        return objectMapper.writeValueAsString(mapa);
    }

    private Long criar(String nome) throws Exception {
        return criar(nome, null);
    }

    private Long criar(String nome, String pais) throws Exception {
        String resposta = mockMvc.perform(post("/api/desenvolvedoras")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(nome, pais, null)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
    }
}
