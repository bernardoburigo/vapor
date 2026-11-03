package com.example.trabalho1.support;

import java.util.UUID;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.trabalho1.entity.Role;
import com.example.trabalho1.entity.Usuario;
import com.example.trabalho1.repository.UsuarioRepository;
import com.example.trabalho1.security.JwtService;

import lombok.RequiredArgsConstructor;

/**
 * Cria usuários com um perfil e devolve o header {@code Authorization} pronto, gerando o token direto pelo
 * {@link JwtService}. Os usuários são gravados na transação do teste (rollback ao final), então os testes
 * não dependem do ADMIN criado pelo bootstrap nem sujam o banco.
 * <p>Uso: {@code @Import(AuthTestHelper.class)} na classe de teste.
 */
@TestComponent
@RequiredArgsConstructor
public class AuthTestHelper {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /** Usuário criado e o header {@code Bearer <token>} dele. */
    public record Credencial(Usuario usuario, String bearer) {

        public Long id() {
            return usuario.getId();
        }
    }

    public Credencial criar(Role role) {
        Usuario usuario = new Usuario();
        usuario.setNome(role.name() + " de teste");
        usuario.setEmail(role.name().toLowerCase() + "-" + UUID.randomUUID() + "@vapor.com");
        usuario.setSenha(passwordEncoder.encode("senha123"));
        usuario.setRole(role);
        usuarioRepository.saveAndFlush(usuario);
        return new Credencial(usuario, "Bearer " + jwtService.gerarToken(usuario));
    }

    public Credencial admin() {
        return criar(Role.ADMIN);
    }

    public Credencial user() {
        return criar(Role.USER);
    }
}
