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
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.example.trabalho1.dto.DesenvolvedoraRequestDTO;
import com.example.trabalho1.dto.DesenvolvedoraResponseDTO;
import com.example.trabalho1.entity.Desenvolvedora;
import com.example.trabalho1.exception.BusinessException;
import com.example.trabalho1.exception.ResourceNotFoundException;
import com.example.trabalho1.repository.DesenvolvedoraRepository;
import com.example.trabalho1.repository.JogoRepository;

@ExtendWith(MockitoExtension.class)
class DesenvolvedoraServiceTest {

    @Mock
    private DesenvolvedoraRepository desenvolvedoraRepository;

    @Mock
    private JogoRepository jogoRepository;

    @InjectMocks
    private DesenvolvedoraService desenvolvedoraService;

    private Desenvolvedora valve;

    @BeforeEach
    void setUp() {
        valve = new Desenvolvedora();
        valve.setId(1L);
        valve.setNome("Valve Corporation");
        valve.setPaisOrigem("Estados Unidos");
        valve.setSite("https://www.valvesoftware.com");
    }

    // -------------------------------------------------------------------------
    // criar
    // -------------------------------------------------------------------------

    @Test
    void deveCriarDesenvolvedoraComNomeSemEspacosExtras() {
        DesenvolvedoraRequestDTO dto = new DesenvolvedoraRequestDTO("  CD Projekt  ", "Polônia", null);

        when(desenvolvedoraRepository.existsByNomeIgnoreCase("CD Projekt")).thenReturn(false);
        when(desenvolvedoraRepository.save(any(Desenvolvedora.class))).thenAnswer(inv -> {
            Desenvolvedora d = inv.getArgument(0);
            d.setId(2L);
            return d;
        });

        DesenvolvedoraResponseDTO resultado = desenvolvedoraService.criar(dto);

        assertThat(resultado.id()).isEqualTo(2L);
        assertThat(resultado.nome()).isEqualTo("CD Projekt");
        assertThat(resultado.paisOrigem()).isEqualTo("Polônia");
    }

    @Test
    void deveLancarConflitoAoCriarComNomeDuplicado() {
        DesenvolvedoraRequestDTO dto = new DesenvolvedoraRequestDTO("valve corporation", null, null);

        when(desenvolvedoraRepository.existsByNomeIgnoreCase("valve corporation")).thenReturn(true);

        assertThatThrownBy(() -> desenvolvedoraService.criar(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(desenvolvedoraRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // listar / buscar
    // -------------------------------------------------------------------------

    @Test
    void deveListarTodasSemFiltro() {
        when(desenvolvedoraRepository.findAll()).thenReturn(List.of(valve));

        assertThat(desenvolvedoraService.listar(null, null)).hasSize(1);
    }

    @Test
    void deveFiltrarPorNomeComPrioridadeSobreOPais() {
        when(desenvolvedoraRepository.findAllByNomeContainingIgnoreCase("valve")).thenReturn(List.of(valve));

        List<DesenvolvedoraResponseDTO> resultado = desenvolvedoraService.listar(" valve ", "Brasil");

        assertThat(resultado).extracting(DesenvolvedoraResponseDTO::nome).containsExactly("Valve Corporation");
        verify(desenvolvedoraRepository, never()).findAllByPaisOrigemIgnoreCase(any());
    }

    @Test
    void deveFiltrarPorPais() {
        when(desenvolvedoraRepository.findAllByPaisOrigemIgnoreCase("Estados Unidos")).thenReturn(List.of(valve));

        assertThat(desenvolvedoraService.listar(null, "Estados Unidos")).hasSize(1);
    }

    @Test
    void deveBuscarPorId() {
        when(desenvolvedoraRepository.findById(1L)).thenReturn(Optional.of(valve));

        assertThat(desenvolvedoraService.buscarPorId(1L).nome()).isEqualTo("Valve Corporation");
    }

    @Test
    void deveLancarNaoEncontradoAoBuscarIdInexistente() {
        when(desenvolvedoraRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> desenvolvedoraService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // atualizar
    // -------------------------------------------------------------------------

    @Test
    void deveAtualizarDesenvolvedora() {
        DesenvolvedoraRequestDTO dto = new DesenvolvedoraRequestDTO("Valve", "EUA", "https://valve.com");

        when(desenvolvedoraRepository.findById(1L)).thenReturn(Optional.of(valve));
        when(desenvolvedoraRepository.existsByNomeIgnoreCaseAndIdNot("Valve", 1L)).thenReturn(false);
        when(desenvolvedoraRepository.save(any(Desenvolvedora.class))).thenAnswer(inv -> inv.getArgument(0));

        DesenvolvedoraResponseDTO resultado = desenvolvedoraService.atualizar(1L, dto);

        assertThat(resultado.nome()).isEqualTo("Valve");
        assertThat(resultado.paisOrigem()).isEqualTo("EUA");
    }

    @Test
    void deveLancarConflitoAoAtualizarParaNomeDeOutraDesenvolvedora() {
        DesenvolvedoraRequestDTO dto = new DesenvolvedoraRequestDTO("CD Projekt", null, null);

        when(desenvolvedoraRepository.findById(1L)).thenReturn(Optional.of(valve));
        when(desenvolvedoraRepository.existsByNomeIgnoreCaseAndIdNot("CD Projekt", 1L)).thenReturn(true);

        assertThatThrownBy(() -> desenvolvedoraService.atualizar(1L, dto))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(desenvolvedoraRepository, never()).save(any());
    }

    @Test
    void deveLancarNaoEncontradoAoAtualizarIdInexistente() {
        when(desenvolvedoraRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> desenvolvedoraService.atualizar(99L, new DesenvolvedoraRequestDTO("X", null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // deletar
    // -------------------------------------------------------------------------

    @Test
    void deveDeletarDesenvolvedoraSemJogos() {
        when(desenvolvedoraRepository.findById(1L)).thenReturn(Optional.of(valve));
        when(jogoRepository.existsByDesenvolvedoraId(1L)).thenReturn(false);

        desenvolvedoraService.deletar(1L);

        verify(desenvolvedoraRepository).delete(valve);
    }

    @Test
    void deveLancarConflitoAoDeletarDesenvolvedoraComJogos() {
        when(desenvolvedoraRepository.findById(1L)).thenReturn(Optional.of(valve));
        when(jogoRepository.existsByDesenvolvedoraId(1L)).thenReturn(true);

        assertThatThrownBy(() -> desenvolvedoraService.deletar(1L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(desenvolvedoraRepository, never()).delete(ArgumentMatchers.<Desenvolvedora>any());
    }

    @Test
    void deveLancarNaoEncontradoAoDeletarIdInexistente() {
        when(desenvolvedoraRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> desenvolvedoraService.deletar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
