package com.example.trabalho1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.trabalho1.entity.Role;
import com.example.trabalho1.entity.Usuario;
import com.example.trabalho1.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Cria o primeiro usuário ADMIN na inicialização, caso o email configurado ainda não exista.
 * O cadastro público sempre gera USER, então sem isto não haveria como obter um ADMIN.
 * A senha vem da configuração (e é gravada com BCrypt), sem nenhum hash fixo no repositório.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.nome}")
    private String nome;

    @Value("${app.admin.email}")
    private String email;

    @Value("${app.admin.senha}")
    private String senha;

    @Override
    public void run(ApplicationArguments args) {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            return;
        }
        String emailNormalizado = email.trim().toLowerCase();
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setNome(nome);
        admin.setEmail(emailNormalizado);
        admin.setSenha(passwordEncoder.encode(senha));
        admin.setRole(Role.ADMIN);
        usuarioRepository.save(admin);

        log.warn("Usuário ADMIN inicial criado ({}). Em produção, defina APP_ADMIN_EMAIL e APP_ADMIN_SENHA.",
                emailNormalizado);
    }
}
