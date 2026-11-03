package com.example.trabalho1.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
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

import com.example.trabalho1.entity.Avaliacao;
import com.example.trabalho1.entity.Categoria;
import com.example.trabalho1.entity.Desenvolvedora;
import com.example.trabalho1.entity.Usuario;
import com.example.trabalho1.repository.AvaliacaoRepository;
import com.example.trabalho1.repository.CategoriaRepository;
import com.example.trabalho1.repository.DesenvolvedoraRepository;
import com.example.trabalho1.repository.JogoRepository;
import com.example.trabalho1.repository.UsuarioRepository;
import com.example.trabalho1.support.AuthTestHelper;
import com.example.trabalho1.support.AuthTestHelper.Credencial;

import tools.jackson.databind.ObjectMapper;

/**
 * Integração Controller → Service → Repository → Postgres para o catálogo de jogos:
 * vínculos, filtros (RF08) e nota média (RF09). Cada teste roda em transação com rollback.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(AuthTestHelper.class)
class JogoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DesenvolvedoraRepository desenvolvedoraRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private AuthTestHelper auth;

    private Credencial admin;

    @BeforeEach
    void setUp() {
        admin = auth.admin();
    }

    @Test
    void deveCriarJogoRetornando201ComLocationEVinculos() throws Exception {
        Desenvolvedora dev = criarDesenvolvedora();
        Categoria cat = criarCategoria();

        mockMvc.perform(post("/api/jogos")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoJogo("Portal 3", dev.getId(), cat.getId())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/jogos/\\d+")))
                .andExpect(jsonPath("$.titulo", is("Portal 3")))
                .andExpect(jsonPath("$.desenvolvedora.id", is(dev.getId().intValue())))
                .andExpect(jsonPath("$.categorias[0].id", is(cat.getId().intValue())))
                .andExpect(jsonPath("$.notaMedia", nullValue()))
                .andExpect(jsonPath("$.totalAvaliacoes", is(0)));
    }

    @Test
    void deveRetornar400ParaDadosInvalidosComFieldErrors() throws Exception {
        String corpo = """
                {"titulo":"","preco":-1,"desenvolvedoraId":null,"categoriaIds":[]}
                """;

        mockMvc.perform(post("/api/jogos").header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.titulo").exists())
                .andExpect(jsonPath("$.fieldErrors.preco").exists())
                .andExpect(jsonPath("$.fieldErrors.desenvolvedoraId").exists())
                .andExpect(jsonPath("$.fieldErrors.categoriaIds").exists());
    }

    @Test
    void deveRetornar404AoCriarComCategoriaInexistente() throws Exception {
        Desenvolvedora dev = criarDesenvolvedora();

        mockMvc.perform(post("/api/jogos")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoJogo("Jogo Fantasma", dev.getId(), 999_999L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("999999")));
    }

    @Test
    void deveFiltrarPorCategoriaEPorDesenvolvedora() throws Exception {
        Desenvolvedora devA = criarDesenvolvedora();
        Desenvolvedora devB = criarDesenvolvedora();
        Categoria catRpg = criarCategoria();
        Categoria catAcao = criarCategoria();

        criarJogoViaApi("Jogo A", devA.getId(), catRpg.getId());
        criarJogoViaApi("Jogo B", devA.getId(), catAcao.getId());
        criarJogoViaApi("Jogo C", devB.getId(), catRpg.getId(), catAcao.getId());

        mockMvc.perform(get("/api/jogos").param("categoriaId", catRpg.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements", is(2)))
                .andExpect(jsonPath("$.content[*].titulo", containsInAnyOrder("Jogo A", "Jogo C")));

        mockMvc.perform(get("/api/jogos").param("desenvolvedoraId", devA.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].titulo", containsInAnyOrder("Jogo A", "Jogo B")));

        mockMvc.perform(get("/api/jogos")
                        .param("categoriaId", catRpg.getId().toString())
                        .param("desenvolvedoraId", devA.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].titulo", is("Jogo A")));

        // Jogo C tem as duas categorias: filtrar por uma não pode duplicá-lo.
        mockMvc.perform(get("/api/jogos").param("categoriaId", catAcao.getId().toString()))
                .andExpect(jsonPath("$.page.totalElements", is(2)));
    }

    @Test
    void deveFiltrarPorTrechoDoTituloSemDiferenciarMaiusculas() throws Exception {
        Desenvolvedora dev = criarDesenvolvedora();
        Categoria cat = criarCategoria();
        String marca = "zz" + UUID.randomUUID().toString().substring(0, 8);

        criarJogoViaApi("Super " + marca + " Bros", dev.getId(), cat.getId());
        criarJogoViaApi("Outro Jogo", dev.getId(), cat.getId());

        mockMvc.perform(get("/api/jogos").param("titulo", marca.toUpperCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].titulo", is("Super " + marca + " Bros")));
    }

    @Test
    void deveCalcularNotaMediaETotalDeAvaliacoes() throws Exception {
        Desenvolvedora dev = criarDesenvolvedora();
        Categoria cat = criarCategoria();
        Long jogoId = criarJogoViaApi("Jogo Avaliado", dev.getId(), cat.getId());

        avaliar(jogoId, 4);
        avaliar(jogoId, 5);

        mockMvc.perform(get("/api/jogos/{id}", jogoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notaMedia", is(4.5)))
                .andExpect(jsonPath("$.totalAvaliacoes", is(2)));

        mockMvc.perform(get("/api/jogos").param("categoriaId", cat.getId().toString()))
                .andExpect(jsonPath("$.content[0].notaMedia", is(4.5)));
    }

    @Test
    void deveAtualizarJogoETrocarCategorias() throws Exception {
        Desenvolvedora dev = criarDesenvolvedora();
        Categoria antiga = criarCategoria();
        Categoria nova = criarCategoria();
        Long jogoId = criarJogoViaApi("Jogo Antigo", dev.getId(), antiga.getId());

        mockMvc.perform(put("/api/jogos/{id}", jogoId)
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoJogo("Jogo Novo", dev.getId(), nova.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo", is("Jogo Novo")))
                .andExpect(jsonPath("$.categorias.length()", is(1)))
                .andExpect(jsonPath("$.categorias[0].id", is(nova.getId().intValue())));
    }

    @Test
    void deveDeletarJogoSemAvaliacoesERetornar409ComAvaliacoes() throws Exception {
        Desenvolvedora dev = criarDesenvolvedora();
        Categoria cat = criarCategoria();
        Long livre = criarJogoViaApi("Jogo Livre", dev.getId(), cat.getId());
        Long avaliado = criarJogoViaApi("Jogo Com Nota", dev.getId(), cat.getId());
        avaliar(avaliado, 3);

        mockMvc.perform(delete("/api/jogos/{id}", avaliado).header(HttpHeaders.AUTHORIZATION, admin.bearer())).andExpect(status().isConflict());

        mockMvc.perform(delete("/api/jogos/{id}", livre).header(HttpHeaders.AUTHORIZATION, admin.bearer())).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/jogos/{id}", livre)).andExpect(status().isNotFound());
    }

    @Test
    void naoDevePermitirRemoverDesenvolvedoraOuCategoriaComJogos() throws Exception {
        Desenvolvedora dev = criarDesenvolvedora();
        Categoria cat = criarCategoria();
        criarJogoViaApi("Jogo Vinculado", dev.getId(), cat.getId());

        mockMvc.perform(delete("/api/desenvolvedoras/{id}", dev.getId())
                .header(HttpHeaders.AUTHORIZATION, admin.bearer())).andExpect(status().isConflict());
        mockMvc.perform(delete("/api/categorias/{id}", cat.getId())
                .header(HttpHeaders.AUTHORIZATION, admin.bearer())).andExpect(status().isConflict());
    }

    @Test
    void escritaNoCatalogoExigeToken() throws Exception {
        mockMvc.perform(post("/api/jogos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void escritaNoCatalogoExigePerfilAdmin() throws Exception {
        Desenvolvedora dev = criarDesenvolvedora();
        Categoria cat = criarCategoria();
        Long jogoId = criarJogoViaApi("Jogo Protegido", dev.getId(), cat.getId());
        String bearerUser = auth.user().bearer();

        mockMvc.perform(post("/api/jogos")
                        .header(HttpHeaders.AUTHORIZATION, bearerUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoJogo("Intruso", dev.getId(), cat.getId())))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/jogos/{id}", jogoId)
                        .header(HttpHeaders.AUTHORIZATION, bearerUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoJogo("Intruso", dev.getId(), cat.getId())))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/jogos/{id}", jogoId).header(HttpHeaders.AUTHORIZATION, bearerUser))
                .andExpect(status().isForbidden());
    }

    @Test
    void leituraDoCatalogoEPublica() throws Exception {
        Desenvolvedora dev = criarDesenvolvedora();
        Categoria cat = criarCategoria();
        Long jogoId = criarJogoViaApi("Jogo Publico", dev.getId(), cat.getId());

        mockMvc.perform(get("/api/jogos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/jogos/{id}", jogoId)).andExpect(status().isOk());
        mockMvc.perform(get("/api/categorias")).andExpect(status().isOk());
        mockMvc.perform(get("/api/desenvolvedoras")).andExpect(status().isOk());
    }

    @Test
    void ordenacaoPorPropriedadeInexistenteRetorna400EmVezDe500() throws Exception {
        // É exatamente o que o Swagger UI mandava por padrão: sort=["string"].
        mockMvc.perform(get("/api/jogos").param("sort", "[\"string\"]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("Parâmetro de ordenação inválido")));

        mockMvc.perform(get("/api/jogos").param("sort", "naoexiste,desc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ordenacaoETamanhoDePaginaValidosFuncionam() throws Exception {
        Desenvolvedora dev = criarDesenvolvedora();
        Categoria cat = criarCategoria();
        criarJogoViaApi("Barato", dev.getId(), cat.getId());
        criarJogoViaApi("Caro", dev.getId(), cat.getId());

        mockMvc.perform(get("/api/jogos")
                        .param("desenvolvedoraId", dev.getId().toString())
                        .param("size", "1").param("sort", "titulo,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size", is(1)))
                .andExpect(jsonPath("$.page.totalElements", is(2)))
                .andExpect(jsonPath("$.content[0].titulo", is("Caro")));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Desenvolvedora criarDesenvolvedora() {
        Desenvolvedora d = new Desenvolvedora();
        d.setNome("Dev " + UUID.randomUUID());
        return desenvolvedoraRepository.save(d);
    }

    private Categoria criarCategoria() {
        Categoria c = new Categoria();
        c.setNome("Cat " + UUID.randomUUID().toString().substring(0, 18));
        return categoriaRepository.save(c);
    }

    private String corpoJogo(String titulo, Long desenvolvedoraId, Long... categoriaIds) {
        return objectMapper.writeValueAsString(java.util.Map.of(
                "titulo", titulo,
                "preco", new BigDecimal("59.90"),
                "desenvolvedoraId", desenvolvedoraId,
                "categoriaIds", categoriaIds));
    }

    private Long criarJogoViaApi(String titulo, Long desenvolvedoraId, Long... categoriaIds) throws Exception {
        String resposta = mockMvc.perform(post("/api/jogos")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoJogo(titulo, desenvolvedoraId, categoriaIds)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
    }

    private void avaliar(Long jogoId, int nota) {
        Usuario usuario = new Usuario();
        usuario.setNome("Avaliador");
        usuario.setEmail("avaliador-" + UUID.randomUUID() + "@vapor.com");
        usuario.setSenha("hash");
        usuarioRepository.save(usuario);

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUsuario(usuario);
        avaliacao.setJogo(jogoRepository.getReferenceById(jogoId));
        avaliacao.setNota(nota);
        avaliacaoRepository.saveAndFlush(avaliacao);
    }
}
