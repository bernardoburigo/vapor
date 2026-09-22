package com.example.trabalho1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.trabalho1.dto.LoginRequestDTO;
import com.example.trabalho1.dto.UsuarioRequestDTO;
import com.example.trabalho1.dto.UsuarioResponseDTO;
import com.example.trabalho1.dto.UsuarioUpdateRequestDTO;
import com.example.trabalho1.entity.Role;
import com.example.trabalho1.entity.Usuario;
import com.example.trabalho1.exception.BusinessException;
import com.example.trabalho1.exception.ResourceNotFoundException;
import com.example.trabalho1.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioExistente;

    @BeforeEach
    void setUp() {
        usuarioExistente = new Usuario();
        usuarioExistente.setId(1L);
        usuarioExistente.setNome("Bernardo");
        usuarioExistente.setEmail("bernardo@vapor.com");
        usuarioExistente.setSenha("hash-antigo");
        usuarioExistente.setRole(Role.USER);
    }

    @Test
    void deveCriarUsuarioComSenhaHasheada() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Bernardo", "bernardo@vapor.com", "senha123");
        when(usuarioRepository.existsByEmail(dto.email())).thenReturn(false);
        when(passwordEncoder.encode(dto.senha())).thenReturn("hash-novo");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        UsuarioResponseDTO resultado = usuarioService.criar(dto);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.email()).isEqualTo("bernardo@vapor.com");
        verify(passwordEncoder).encode("senha123");
    }

    @Test
    void deveLancarExcecaoAoCriarComEmailDuplicado() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Bernardo", "bernardo@vapor.com", "senha123");
        when(usuarioRepository.existsByEmail(dto.email())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoBuscarIdInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveAtualizarUsuarioSemTrocarSenha() {
        UsuarioUpdateRequestDTO dto = new UsuarioUpdateRequestDTO("Bernardo Búrigo", "bernardo@vapor.com", null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponseDTO resultado = usuarioService.atualizar(1L, dto);

        assertThat(resultado.nome()).isEqualTo("Bernardo Búrigo");
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void deveAtualizarUsuarioTrocandoSenha() {
        UsuarioUpdateRequestDTO dto = new UsuarioUpdateRequestDTO("Bernardo", "bernardo@vapor.com", "novaSenha123");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("hash-novo");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        usuarioService.atualizar(1L, dto);

        assertThat(usuarioExistente.getSenha()).isEqualTo("hash-novo");
    }

    @Test
    void deveDeletarUsuarioExistente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));

        usuarioService.deletar(1L);

        verify(usuarioRepository, times(1)).delete(usuarioExistente);
    }

    @Test
    void deveLancarExcecaoAoDeletarUsuarioInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.deletar(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(usuarioRepository, never()).delete(any());
    }

    @Test
    void deveLogarComCredenciaisValidas() {
        LoginRequestDTO dto = new LoginRequestDTO("bernardo@vapor.com", "senha123");
        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.of(usuarioExistente));
        when(passwordEncoder.matches(dto.senha(), usuarioExistente.getSenha())).thenReturn(true);

        UsuarioResponseDTO resultado = usuarioService.login(dto);

        assertThat(resultado.email()).isEqualTo("bernardo@vapor.com");
    }

    @Test
    void deveLancarExcecaoAoLogarComSenhaInvalida() {
        LoginRequestDTO dto = new LoginRequestDTO("bernardo@vapor.com", "senhaErrada");
        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.of(usuarioExistente));
        when(passwordEncoder.matches(dto.senha(), usuarioExistente.getSenha())).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.login(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void deveLancarExcecaoAoLogarComEmailInexistente() {
        LoginRequestDTO dto = new LoginRequestDTO("naoexiste@vapor.com", "senha123");
        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.login(dto))
                .isInstanceOf(BusinessException.class);
    }
}
