package com.example.trabalho1.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Lê o header {@code Authorization: Bearer <token>} e, se o token for válido, autentica a requisição.
 * Token ausente ou inválido não gera erro aqui: a requisição segue anônima e o Spring Security
 * decide (401 nos endpoints protegidos, passa nos públicos).
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXO_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecalho = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (cabecalho != null && cabecalho.startsWith(PREFIXO_BEARER)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = cabecalho.substring(PREFIXO_BEARER.length()).trim();
            jwtService.extrairEmail(token).ifPresent(email -> autenticar(email, request));
        }

        chain.doFilter(request, response);
    }

    private void autenticar(String email, HttpServletRequest request) {
        try {
            UserDetails usuario = userDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken autenticacao =
                    new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
            autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(autenticacao);
        } catch (UsernameNotFoundException ex) {
            log.debug("Token válido para usuário que não existe mais: {}", email);
        }
    }
}
