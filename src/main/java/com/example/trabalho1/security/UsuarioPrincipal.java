package com.example.trabalho1.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.trabalho1.entity.Role;
import com.example.trabalho1.entity.Usuario;

/** Usuário autenticado na requisição; expõe o id e o perfil para as regras de autorização. */
public class UsuarioPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String senha;
    private final Role role;

    public UsuarioPrincipal(Long id, String email, String senha, Role role) {
        this.id = id;
        this.email = email;
        this.senha = senha;
        this.role = role;
    }

    public static UsuarioPrincipal from(Usuario usuario) {
        return new UsuarioPrincipal(usuario.getId(), usuario.getEmail(), usuario.getSenha(), usuario.getRole());
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
