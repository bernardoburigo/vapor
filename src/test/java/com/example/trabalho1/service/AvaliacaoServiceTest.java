package com.example.trabalho1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

import com.example.trabalho1.dto.AvaliacaoRequestDTO;
import com.example.trabalho1.dto.AvaliacaoResponseDTO;
import com.example.trabalho1.dto.AvaliacaoUpdateRequestDTO;
import com.example.trabalho1.entity.Avaliacao;
import com.example.trabalho1.entity.Jogo;
import com.example.trabalho1.entity.Role;
import com.example.trabalho1.entity.Usuario;
import com.example.trabalho1.exception.BusinessException;
import com.example.trabalho1.exception.ResourceNotFoundException;
import com.example.trabalho1.repository.AvaliacaoRepository;
import com.example.trabalho1.repository.JogoRepository;
import com.example.trabalho1.repository.UsuarioRepository;
import com.example.trabalho1.security.UsuarioPrincipal;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JogoRepository jogoRepository;

    @InjectMocks
    private AvaliacaoService avaliacaoService;

    private Usuario usuario;
    private Jogo jogo;
    private Avaliacao avaliacaoExistente;
    private UsuarioPrincipal autor;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("João Vitor");
        usuario.setEmail("joao@vapor.com");
        usuario.setSenha("hash");

        jogo = new Jogo();
        jogo.setId(10L);
        jogo.setTitulo("Half-Life 3");

        avaliacaoExistente = new Avaliacao();
        avaliacaoExistente.setId(100L);
        avaliacaoExistente.setUsuario(usuario);
        avaliacaoExistente.setJogo(jogo);
        avaliacaoExistente.setNota(5);
        avaliacaoExistente.setComentario("Obra-prima!");

        autor = new UsuarioPrincipal(1L, "joao@vapor.com", "hash", Role.USER);
    }

    // -------------------------------------------------------------------------
    // criar
    // -------------------------------------------------------------------------

    @Test
    void deveCriarAvaliacaoComSucesso() {
        AvaliacaoRequestDTO dto = new AvaliacaoRequestDTO(10L, 5, "Obra-prima!");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(jogoRepository.findById(10L)).thenReturn(Optional.of(jogo));
        when(avaliacaoRepository.existsByUsuarioIdAndJogoId(1L, 10L)).thenReturn(false);
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(inv -> {
            Avaliacao a = inv.getArgument(0);
            a.setId(100L);
            return a;
        });

        AvaliacaoResponseDTO resultado = avaliacaoService.criar(1L, dto);

        assertThat(resultado.id()).isEqualTo(100L);
        assertThat(resultado.nota()).isEqualTo(5);
        assertThat(resultado.jogoTitulo()).isEqualTo("Half-Life 3");
        verify(avaliacaoRepository).save(any(Avaliacao.class));
    }

    @Test
    void deveLancarExcecaoAoCriarAvaliacaoDuplicada() {
        AvaliacaoRequestDTO dto = new AvaliacaoRequestDTO(10L, 4, "Bom jogo");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(jogoRepository.findById(10L)).thenReturn(Optional.of(jogo));
        when(avaliacaoRepository.existsByUsuarioIdAndJogoId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> avaliacaoService.criar(1L, dto))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoCriarComUsuarioInexistente() {
        AvaliacaoRequestDTO dto = new AvaliacaoRequestDTO(10L, 3, null);

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avaliacaoService.criar(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoCriarComJogoInexistente() {
        AvaliacaoRequestDTO dto = new AvaliacaoRequestDTO(99L, 3, null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(jogoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avaliacaoService.criar(1L, dto))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(avaliacaoRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // buscarPorId
    // -------------------------------------------------------------------------

    @Test
    void deveBuscarAvaliacaoPorId() {
        when(avaliacaoRepository.findById(100L)).thenReturn(Optional.of(avaliacaoExistente));

        AvaliacaoResponseDTO resultado = avaliacaoService.buscarPorId(100L);

        assertThat(resultado.id()).isEqualTo(100L);
        assertThat(resultado.usuarioNome()).isEqualTo("João Vitor");
    }

    @Test
    void deveLancarExcecaoAoBuscarIdInexistente() {
        when(avaliacaoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avaliacaoService.buscarPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // listarPorJogo
    // -------------------------------------------------------------------------

    @Test
    void deveListarAvaliacoesPorJogo() {
        when(jogoRepository.findById(10L)).thenReturn(Optional.of(jogo));
        when(avaliacaoRepository.findAllByJogoId(10L)).thenReturn(List.of(avaliacaoExistente));

        List<AvaliacaoResponseDTO> resultado = avaliacaoService.listarPorJogo(10L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).jogoId()).isEqualTo(10L);
    }

    @Test
    void deveLancarExcecaoAoListarPorJogoInexistente() {
        when(jogoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avaliacaoService.listarPorJogo(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // listarPorUsuario
    // -------------------------------------------------------------------------

    @Test
    void deveListarAvaliacoesPorUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(avaliacaoRepository.findAllByUsuarioId(1L)).thenReturn(List.of(avaliacaoExistente));

        List<AvaliacaoResponseDTO> resultado = avaliacaoService.listarPorUsuario(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).usuarioId()).isEqualTo(1L);
    }

    // -------------------------------------------------------------------------
    // atualizar
    // -------------------------------------------------------------------------

    @Test
    void deveAtualizarNotaEComentario() {
        AvaliacaoUpdateRequestDTO dto = new AvaliacaoUpdateRequestDTO(3, "Ficou cansativo no final");

        when(avaliacaoRepository.findById(100L)).thenReturn(Optional.of(avaliacaoExistente));
        when(avaliacaoRepository.save(any(Avaliacao.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AvaliacaoResponseDTO resultado = avaliacaoService.atualizar(100L, dto, autor);

        assertThat(resultado.nota()).isEqualTo(3);
        assertThat(resultado.comentario()).isEqualTo("Ficou cansativo no final");
        // usuário e jogo não mudam
        assertThat(resultado.usuarioId()).isEqualTo(1L);
        assertThat(resultado.jogoId()).isEqualTo(10L);
    }

    @Test
    void deveLancarExcecaoAoAtualizarAvaliacaoInexistente() {
        AvaliacaoUpdateRequestDTO dto = new AvaliacaoUpdateRequestDTO(2, null);

        when(avaliacaoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avaliacaoService.atualizar(999L, dto, autor))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(avaliacaoRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // deletar
    // -------------------------------------------------------------------------

    @Test
    void deveDeletarAvaliacaoExistente() {
        when(avaliacaoRepository.findById(100L)).thenReturn(Optional.of(avaliacaoExistente));

        avaliacaoService.deletar(100L, autor);

        verify(avaliacaoRepository, times(1)).delete(avaliacaoExistente);
    }

    @Test
    void deveLancarExcecaoAoDeletarAvaliacaoInexistente() {
        when(avaliacaoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avaliacaoService.deletar(999L, autor))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(avaliacaoRepository, never()).delete(any());
    }

    // -------------------------------------------------------------------------
    // autorização: só o dono ou um ADMIN altera/remove
    // -------------------------------------------------------------------------

    @Test
    void deveNegarAtualizacaoDeAvaliacaoDeOutroUsuario() {
        UsuarioPrincipal outro = new UsuarioPrincipal(2L, "outro@vapor.com", "hash", Role.USER);
        when(avaliacaoRepository.findById(100L)).thenReturn(Optional.of(avaliacaoExistente));

        assertThatThrownBy(() -> avaliacaoService.atualizar(100L, new AvaliacaoUpdateRequestDTO(1, "Ruim"), outro))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(avaliacaoRepository, never()).save(any());
        assertThat(avaliacaoExistente.getNota()).isEqualTo(5);
    }

    @Test
    void deveNegarRemocaoDeAvaliacaoDeOutroUsuario() {
        UsuarioPrincipal outro = new UsuarioPrincipal(2L, "outro@vapor.com", "hash", Role.USER);
        when(avaliacaoRepository.findById(100L)).thenReturn(Optional.of(avaliacaoExistente));

        assertThatThrownBy(() -> avaliacaoService.deletar(100L, outro))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(avaliacaoRepository, never()).delete(any());
    }

    @Test
    void devePermitirQueAdminAtualizeAvaliacaoDeOutroUsuario() {
        UsuarioPrincipal admin = new UsuarioPrincipal(99L, "admin@vapor.com", "hash", Role.ADMIN);
        when(avaliacaoRepository.findById(100L)).thenReturn(Optional.of(avaliacaoExistente));
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(inv -> inv.getArgument(0));

        AvaliacaoResponseDTO resultado =
                avaliacaoService.atualizar(100L, new AvaliacaoUpdateRequestDTO(2, "Moderado"), admin);

        assertThat(resultado.nota()).isEqualTo(2);
    }

    @Test
    void devePermitirQueAdminRemovaAvaliacaoDeOutroUsuario() {
        UsuarioPrincipal admin = new UsuarioPrincipal(99L, "admin@vapor.com", "hash", Role.ADMIN);
        when(avaliacaoRepository.findById(100L)).thenReturn(Optional.of(avaliacaoExistente));

        avaliacaoService.deletar(100L, admin);

        verify(avaliacaoRepository).delete(avaliacaoExistente);
    }
}
