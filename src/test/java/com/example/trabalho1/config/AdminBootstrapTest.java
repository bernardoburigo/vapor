package com.example.trabalho1.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.trabalho1.entity.Role;
import com.example.trabalho1.entity.Usuario;
import com.example.trabalho1.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminBootstrap adminBootstrap;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adminBootstrap, "nome", "Administrador");
        ReflectionTestUtils.setField(adminBootstrap, "email", " Admin@Vapor.com ");
        ReflectionTestUtils.setField(adminBootstrap, "senha", "admin123");
    }

    @Test
    void deveCriarAdminComSenhaHasheadaQuandoNaoExiste() {
        when(usuarioRepository.existsByEmail("admin@vapor.com")).thenReturn(false);
        when(passwordEncoder.encode("admin123")).thenReturn("hash-admin");

        adminBootstrap.run(null);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario salvo = captor.getValue();
        assertThat(salvo.getEmail()).isEqualTo("admin@vapor.com");
        assertThat(salvo.getRole()).isEqualTo(Role.ADMIN);
        assertThat(salvo.getSenha()).isEqualTo("hash-admin");
    }

    @Test
    void naoDeveRecriarAdminQuandoJaExiste() {
        when(usuarioRepository.existsByEmail("admin@vapor.com")).thenReturn(true);

        adminBootstrap.run(null);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void naoDeveCriarAdminQuandoAConfiguracaoEstaVazia() {
        ReflectionTestUtils.setField(adminBootstrap, "senha", " ");

        adminBootstrap.run(null);

        verify(usuarioRepository, never()).save(any());
    }
}
