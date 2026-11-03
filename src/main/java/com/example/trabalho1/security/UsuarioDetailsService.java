package com.example.trabalho1.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.trabalho1.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Carrega o usuário do banco a cada requisição autenticada: assim, um usuário removido ou com perfil alterado
 * perde o acesso imediatamente, sem esperar o token expirar.
 */
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .map(UsuarioPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }
}
