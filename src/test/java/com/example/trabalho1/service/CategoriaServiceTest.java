package com.example.trabalho1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.example.trabalho1.dto.CategoriaRequestDTO;
import com.example.trabalho1.dto.CategoriaResponseDTO;
import com.example.trabalho1.entity.Categoria;
import com.example.trabalho1.exception.BusinessException;
import com.example.trabalho1.exception.ResourceNotFoundException;
import com.example.trabalho1.repository.CategoriaRepository;
import com.example.trabalho1.repository.JogoRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private JogoRepository jogoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoriaExistente;

    @BeforeEach
    void setUp() {
        categoriaExistente = new Categoria();
        categoriaExistente.setId(1L);
        categoriaExistente.setNome("RPG");
        categoriaExistente.setDescricao("Jogos de interpretação de papéis");
    }

    // -------------------------------------------------------------------------
    // criar
    // -------------------------------------------------------------------------

    @Test
    void deveCriarCategoriaComNomeSemEspacosExtras() {
        CategoriaRequestDTO dto = new CategoriaRequestDTO("  Indie  ", "Jogos independentes");

        when(categoriaRepository.existsByNomeIgnoreCase("Indie")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> {
            Categoria c = inv.getArgument(0);
            c.setId(2L);
            return c;
        });

        CategoriaResponseDTO resultado = categoriaService.criar(dto);

        assertThat(resultado.id()).isEqualTo(2L);
        assertThat(resultado.nome()).isEqualTo("Indie");
        assertThat(resultado.descricao()).isEqualTo("Jogos independentes");
    }

    @Test
    void deveLancarConflitoAoCriarComNomeDuplicado() {
        CategoriaRequestDTO dto = new CategoriaRequestDTO("rpg", null);

        when(categoriaRepository.existsByNomeIgnoreCase("rpg")).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.criar(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(categoriaRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // listar / buscar
    // -------------------------------------------------------------------------

    @Test
    void deveListarTodasQuandoNaoHaFiltro() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoriaExistente));

        List<CategoriaResponseDTO> resultado = categoriaService.listar(null);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nome()).isEqualTo("RPG");
        verify(categoriaRepository, never()).findAllByNomeContainingIgnoreCase(any());
    }

    @Test
    void deveFiltrarPorParteDoNome() {
        when(categoriaRepository.findAllByNomeContainingIgnoreCase("rp"))
                .thenReturn(List.of(categoriaExistente));

        List<CategoriaResponseDTO> resultado = categoriaService.listar(" rp ");

        assertThat(resultado).extracting(CategoriaResponseDTO::nome).containsExactly("RPG");
    }

    @Test
    void deveBuscarPorId() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaExistente));

        assertThat(categoriaService.buscarPorId(1L).nome()).isEqualTo("RPG");
    }

    @Test
    void deveLancarNaoEncontradoAoBuscarIdInexistente() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // atualizar
    // -------------------------------------------------------------------------

    @Test
    void deveAtualizarCategoria() {
        CategoriaRequestDTO dto = new CategoriaRequestDTO("Ação", "Muita ação");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaExistente));
        when(categoriaRepository.existsByNomeIgnoreCaseAndIdNot("Ação", 1L)).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoriaResponseDTO resultado = categoriaService.atualizar(1L, dto);

        assertThat(resultado.nome()).isEqualTo("Ação");
        assertThat(resultado.descricao()).isEqualTo("Muita ação");
    }

    @Test
    void deveLancarConflitoAoAtualizarParaNomeDeOutraCategoria() {
        CategoriaRequestDTO dto = new CategoriaRequestDTO("Ação", null);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaExistente));
        when(categoriaRepository.existsByNomeIgnoreCaseAndIdNot("Ação", 1L)).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.atualizar(1L, dto))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void deveLancarNaoEncontradoAoAtualizarIdInexistente() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.atualizar(99L, new CategoriaRequestDTO("X", null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // deletar
    // -------------------------------------------------------------------------

    @Test
    void deveDeletarCategoriaSemJogos() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaExistente));
        when(jogoRepository.existsByCategoriasId(1L)).thenReturn(false);

        categoriaService.deletar(1L);

        verify(categoriaRepository).delete(categoriaExistente);
    }

    @Test
    void deveLancarConflitoAoDeletarCategoriaComJogos() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaExistente));
        when(jogoRepository.existsByCategoriasId(1L)).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.deletar(1L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(categoriaRepository, never()).delete(any());
    }

    @Test
    void deveLancarNaoEncontradoAoDeletarIdInexistente() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.deletar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
