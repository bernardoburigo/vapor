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

import java.math.BigDecimal;
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

import com.example.trabalho1.entity.Desenvolvedora;
import com.example.trabalho1.entity.Jogo;
import com.example.trabalho1.repository.DesenvolvedoraRepository;
import com.example.trabalho1.repository.JogoRepository;
import com.example.trabalho1.support.AuthTestHelper;
import com.example.trabalho1.support.AuthTestHelper.Credencial;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(AuthTestHelper.class)
class AvaliacaoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DesenvolvedoraRepository desenvolvedoraRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private AuthTestHelper auth;

    private Jogo jogo;
    private Credencial autor;

    @BeforeEach
    void setUp() {
        Desenvolvedora dev = new Desenvolvedora();
        dev.setNome("Dev " + UUID.randomUUID());
        desenvolvedoraRepository.save(dev);

        jogo = new Jogo();
        jogo.setTitulo("Jogo Avaliavel");
        jogo.setPreco(new BigDecimal("10.00"));
        jogo.setDesenvolvedora(dev);
        jogoRepository.save(jogo);

        autor = auth.user();
    }

    // -------------------------------------------------------------------------
    // criar
    // -------------------------------------------------------------------------

    @Test
    void deveCriarAvaliacaoDoUsuarioAutenticadoRetornando201ComLocation() throws Exception {
        mockMvc.perform(criar(autor, jogo.getId(), 5, "Obra-prima!"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/avaliacoes/\\d+")))
                .andExpect(jsonPath("$.usuarioId", is(autor.id().intValue())))
                .andExpect(jsonPath("$.jogoId", is(jogo.getId().intValue())))
                .andExpect(jsonPath("$.nota", is(5)));
    }

    @Test
    void usuarioIdEnviadoNoCorpoDeveSerIgnorado() throws Exception {
        Credencial outro = auth.user();
        String corpo = """
                {"usuarioId":%d,"jogoId":%d,"nota":4}
                """.formatted(outro.id(), jogo.getId());

        mockMvc.perform(post("/api/avaliacoes")
                        .header(HttpHeaders.AUTHORIZATION, autor.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId", is(autor.id().intValue())));
    }

    @Test
    void deveRetornar401AoCriarSemToken() throws Exception {
        mockMvc.perform(post("/api/avaliacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jogoId\":1,\"nota\":5}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar409AoAvaliarOMesmoJogoDuasVezes() throws Exception {
        mockMvc.perform(criar(autor, jogo.getId(), 5, null)).andExpect(status().isCreated());

        mockMvc.perform(criar(autor, jogo.getId(), 3, "Mudei de ideia"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Usuário já avaliou este jogo")));
    }

    @Test
    void doisUsuariosPodemAvaliarOMesmoJogo() throws Exception {
        mockMvc.perform(criar(autor, jogo.getId(), 5, null)).andExpect(status().isCreated());
        mockMvc.perform(criar(auth.user(), jogo.getId(), 2, null)).andExpect(status().isCreated());
    }

    @Test
    void deveRetornar400ParaNotaForaDoIntervalo() throws Exception {
        mockMvc.perform(criar(autor, jogo.getId(), 0, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nota").exists());

        mockMvc.perform(criar(autor, jogo.getId(), 6, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nota").exists());
    }

    @Test
    void deveRetornar404AoAvaliarJogoInexistente() throws Exception {
        mockMvc.perform(criar(autor, 999_999L, 5, null))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // leitura pública
    // -------------------------------------------------------------------------

    @Test
    void listagemPorJogoEPorUsuarioEPublica() throws Exception {
        mockMvc.perform(criar(autor, jogo.getId(), 4, "Bom")).andExpect(status().isCreated());

        mockMvc.perform(get("/api/avaliacoes").param("jogoId", jogo.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].usuarioNome", is(autor.usuario().getNome())));

        mockMvc.perform(get("/api/avaliacoes").param("usuarioId", autor.id().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    // -------------------------------------------------------------------------
    // alterar / remover: só o dono ou ADMIN
    // -------------------------------------------------------------------------

    @Test
    void donoDeveAlterarAPropriaAvaliacao() throws Exception {
        Long id = criarAvaliacao(autor, 5);

        mockMvc.perform(put("/api/avaliacoes/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, autor.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":3,\"comentario\":\"Cansou\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nota", is(3)))
                .andExpect(jsonPath("$.comentario", is("Cansou")));
    }

    @Test
    void outroUsuarioNaoPodeAlterarNemRemoverAvaliacaoAlheia() throws Exception {
        Long id = criarAvaliacao(autor, 5);
        Credencial intruso = auth.user();

        mockMvc.perform(put("/api/avaliacoes/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, intruso.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":1}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Você só pode alterar as suas próprias avaliações")));

        mockMvc.perform(delete("/api/avaliacoes/{id}", id).header(HttpHeaders.AUTHORIZATION, intruso.bearer()))
                .andExpect(status().isForbidden());

        // a avaliação original permanece intacta
        mockMvc.perform(get("/api/avaliacoes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nota", is(5)));
    }

    @Test
    void adminPodeAlterarERemoverAvaliacaoAlheia() throws Exception {
        Long id = criarAvaliacao(autor, 5);
        Credencial admin = auth.admin();

        mockMvc.perform(put("/api/avaliacoes/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":2}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/avaliacoes/{id}", id).header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .andExpect(status().isNoContent());
    }

    @Test
    void donoDeveRemoverAPropriaAvaliacao() throws Exception {
        Long id = criarAvaliacao(autor, 5);

        mockMvc.perform(delete("/api/avaliacoes/{id}", id).header(HttpHeaders.AUTHORIZATION, autor.bearer()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/avaliacoes/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void alterarOuRemoverSemTokenRetorna401() throws Exception {
        Long id = criarAvaliacao(autor, 5);

        mockMvc.perform(put("/api/avaliacoes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":1}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/avaliacoes/{id}", id)).andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // integridade: usuário com avaliações não pode ser removido
    // -------------------------------------------------------------------------

    @Test
    void adminNaoPodeRemoverUsuarioQueTemAvaliacoes() throws Exception {
        criarAvaliacao(autor, 5);

        mockMvc.perform(delete("/api/usuarios/{id}", autor.id())
                        .header(HttpHeaders.AUTHORIZATION, auth.admin().bearer()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Não é possível remover: o usuário possui avaliações")));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder criar(
            Credencial quem, Long jogoId, int nota, String comentario) {
        String corpo = objectMapper.writeValueAsString(java.util.Map.of(
                "jogoId", jogoId,
                "nota", nota,
                "comentario", comentario == null ? "" : comentario));
        return post("/api/avaliacoes")
                .header(HttpHeaders.AUTHORIZATION, quem.bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo);
    }

    private Long criarAvaliacao(Credencial quem, int nota) throws Exception {
        String resposta = mockMvc.perform(criar(quem, jogo.getId(), nota, "ok"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
    }
}
