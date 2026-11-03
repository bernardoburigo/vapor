package com.example.trabalho1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import com.example.trabalho1.dto.JogoRequestDTO;
import com.example.trabalho1.dto.JogoResponseDTO;
import com.example.trabalho1.entity.Categoria;
import com.example.trabalho1.entity.Desenvolvedora;
import com.example.trabalho1.entity.Jogo;
import com.example.trabalho1.exception.BusinessException;
import com.example.trabalho1.exception.ResourceNotFoundException;
import com.example.trabalho1.repository.AvaliacaoRepository;
import com.example.trabalho1.repository.AvaliacaoRepository.ResumoNota;
import com.example.trabalho1.repository.CategoriaRepository;
import com.example.trabalho1.repository.DesenvolvedoraRepository;
import com.example.trabalho1.repository.JogoRepository;

@ExtendWith(MockitoExtension.class)
class JogoServiceTest {

    @Mock
    private JogoRepository jogoRepository;

    @Mock
    private DesenvolvedoraRepository desenvolvedoraRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @InjectMocks
    private JogoService jogoService;

    private Desenvolvedora desenvolvedora;
    private Categoria rpg;
    private Categoria acao;
    private Jogo jogoExistente;

    @BeforeEach
    void setUp() {
        desenvolvedora = new Desenvolvedora();
        desenvolvedora.setId(1L);
        desenvolvedora.setNome("Valve Corporation");

        rpg = new Categoria();
        rpg.setId(10L);
        rpg.setNome("RPG");

        acao = new Categoria();
        acao.setId(11L);
        acao.setNome("Ação");

        jogoExistente = new Jogo();
        jogoExistente.setId(100L);
        jogoExistente.setTitulo("Half-Life 3");
        jogoExistente.setPreco(new BigDecimal("199.90"));
        jogoExistente.setDataLancamento(LocalDate.of(2026, 11, 15));
        jogoExistente.setDesenvolvedora(desenvolvedora);
        jogoExistente.setCategorias(new HashSet<>(Set.of(rpg)));
    }

    private JogoRequestDTO requisicao(Set<Long> categoriaIds) {
        return new JogoRequestDTO("  Portal 3  ", "Puzzle", new BigDecimal("99.90"),
                LocalDate.of(2027, 1, 10), 1L, categoriaIds);
    }

    private ResumoNota resumo(Long jogoId, Double media, Long total) {
        return new ResumoNota() {
            @Override
            public Long getJogoId() {
                return jogoId;
            }

            @Override
            public Double getMedia() {
                return media;
            }

            @Override
            public Long getTotal() {
                return total;
            }
        };
    }

    // -------------------------------------------------------------------------
    // criar
    // -------------------------------------------------------------------------

    @Test
    void deveCriarJogoVinculandoDesenvolvedoraECategorias() {
        when(desenvolvedoraRepository.findById(1L)).thenReturn(Optional.of(desenvolvedora));
        when(categoriaRepository.findAllById(Set.of(10L, 11L))).thenReturn(List.of(rpg, acao));
        when(jogoRepository.save(any(Jogo.class))).thenAnswer(inv -> {
            Jogo j = inv.getArgument(0);
            j.setId(200L);
            return j;
        });
        when(avaliacaoRepository.resumirNotasPorJogo(any())).thenReturn(List.of());

        JogoResponseDTO resultado = jogoService.criar(requisicao(Set.of(10L, 11L)));

        assertThat(resultado.id()).isEqualTo(200L);
        assertThat(resultado.titulo()).isEqualTo("Portal 3");
        assertThat(resultado.desenvolvedora().nome()).isEqualTo("Valve Corporation");
        assertThat(resultado.categorias()).extracting("nome").containsExactly("Ação", "RPG");
        assertThat(resultado.notaMedia()).isNull();
        assertThat(resultado.totalAvaliacoes()).isZero();
    }

    @Test
    void deveLancarNaoEncontradoAoCriarComDesenvolvedoraInexistente() {
        when(desenvolvedoraRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jogoService.criar(requisicao(Set.of(10L))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Desenvolvedora");

        verify(jogoRepository, never()).save(any());
    }

    @Test
    void deveLancarNaoEncontradoListandoAsCategoriasAusentes() {
        when(desenvolvedoraRepository.findById(1L)).thenReturn(Optional.of(desenvolvedora));
        when(categoriaRepository.findAllById(Set.of(10L, 99L))).thenReturn(List.of(rpg));

        assertThatThrownBy(() -> jogoService.criar(requisicao(Set.of(10L, 99L))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99")
                .hasMessageNotContaining("10");

        verify(jogoRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // listar / buscar (RF08 e RF09)
    // -------------------------------------------------------------------------

    @Test
    void deveListarJogosComNotaMediaEmUmaUnicaConsultaAgrupada() {
        Pageable pageable = PageRequest.of(0, 20);
        when(jogoRepository.findAll(ArgumentMatchers.<Specification<Jogo>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(jogoExistente), pageable, 1));
        when(avaliacaoRepository.resumirNotasPorJogo(List.of(100L)))
                .thenReturn(List.of(resumo(100L, 4.5, 2L)));

        Page<JogoResponseDTO> resultado = jogoService.listar(10L, 1L, "half", pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).notaMedia()).isEqualTo(4.5);
        assertThat(resultado.getContent().get(0).totalAvaliacoes()).isEqualTo(2L);
    }

    @Test
    void naoDeveConsultarNotasQuandoAPaginaVemVazia() {
        Pageable pageable = PageRequest.of(0, 20);
        when(jogoRepository.findAll(ArgumentMatchers.<Specification<Jogo>>any(), any(Pageable.class)))
                .thenReturn(Page.empty(pageable));

        Page<JogoResponseDTO> resultado = jogoService.listar(null, null, null, pageable);

        assertThat(resultado).isEmpty();
        verify(avaliacaoRepository, never()).resumirNotasPorJogo(any());
    }

    @Test
    void deveArredondarNotaMediaParaUmaCasaDecimal() {
        when(jogoRepository.findById(100L)).thenReturn(Optional.of(jogoExistente));
        when(avaliacaoRepository.resumirNotasPorJogo(List.of(100L)))
                .thenReturn(List.of(resumo(100L, 13.0 / 3.0, 3L)));

        JogoResponseDTO resultado = jogoService.buscarPorId(100L);

        assertThat(resultado.notaMedia()).isEqualTo(4.3);
        assertThat(resultado.totalAvaliacoes()).isEqualTo(3L);
    }

    @Test
    void deveRetornarNotaMediaNulaParaJogoSemAvaliacoes() {
        when(jogoRepository.findById(100L)).thenReturn(Optional.of(jogoExistente));
        when(avaliacaoRepository.resumirNotasPorJogo(List.of(100L))).thenReturn(List.of());

        JogoResponseDTO resultado = jogoService.buscarPorId(100L);

        assertThat(resultado.notaMedia()).isNull();
        assertThat(resultado.totalAvaliacoes()).isZero();
    }

    @Test
    void deveLancarNaoEncontradoAoBuscarIdInexistente() {
        when(jogoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jogoService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // atualizar
    // -------------------------------------------------------------------------

    @Test
    void deveAtualizarJogoSubstituindoAsCategorias() {
        when(jogoRepository.findById(100L)).thenReturn(Optional.of(jogoExistente));
        when(desenvolvedoraRepository.findById(1L)).thenReturn(Optional.of(desenvolvedora));
        when(categoriaRepository.findAllById(Set.of(11L))).thenReturn(List.of(acao));
        when(jogoRepository.save(any(Jogo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(avaliacaoRepository.resumirNotasPorJogo(any())).thenReturn(List.of());

        JogoResponseDTO resultado = jogoService.atualizar(100L, requisicao(Set.of(11L)));

        assertThat(resultado.titulo()).isEqualTo("Portal 3");
        assertThat(resultado.categorias()).extracting("nome").containsExactly("Ação");
    }

    @Test
    void deveLancarNaoEncontradoAoAtualizarIdInexistente() {
        when(jogoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jogoService.atualizar(99L, requisicao(Set.of(10L))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // deletar
    // -------------------------------------------------------------------------

    @Test
    void deveDeletarJogoSemAvaliacoes() {
        when(jogoRepository.findById(100L)).thenReturn(Optional.of(jogoExistente));
        when(avaliacaoRepository.existsByJogoId(100L)).thenReturn(false);

        jogoService.deletar(100L);

        verify(jogoRepository).delete(jogoExistente);
    }

    @Test
    void deveLancarConflitoAoDeletarJogoComAvaliacoes() {
        when(jogoRepository.findById(100L)).thenReturn(Optional.of(jogoExistente));
        when(avaliacaoRepository.existsByJogoId(100L)).thenReturn(true);

        assertThatThrownBy(() -> jogoService.deletar(100L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(jogoRepository, never()).delete(ArgumentMatchers.<Jogo>any());
    }

    @Test
    void deveLancarNaoEncontradoAoDeletarIdInexistente() {
        when(jogoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jogoService.deletar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
