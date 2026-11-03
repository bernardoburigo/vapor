package com.example.trabalho1.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.trabalho1.entity.Usuario;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/** Gera e valida tokens JWT assinados com HMAC-SHA256. O token é stateless: nada é guardado no servidor. */
@Slf4j
@Service
public class JwtService {

    private static final int TAMANHO_MINIMO_SEGREDO_EM_BYTES = 32;

    private final SecretKey chave;
    private final Duration validade;

    public JwtService(
            @Value("${app.jwt.secret}") String segredo,
            @Value("${app.jwt.expiration-minutes}") long validadeEmMinutos) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < TAMANHO_MINIMO_SEGREDO_EM_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret deve ter ao menos 32 bytes (256 bits) para HS256; defina JWT_SECRET");
        }
        this.chave = Keys.hmacShaKeyFor(bytes);
        this.validade = Duration.ofMinutes(validadeEmMinutos);
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("id", usuario.getId())
                .claim("role", usuario.getRole().name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(validade)))
                .signWith(chave, Jwts.SIG.HS256)
                .compact();
    }

    public long getValidadeEmSegundos() {
        return validade.toSeconds();
    }

    /** Retorna o email (subject) se o token tem assinatura válida e não expirou; vazio caso contrário. */
    public Optional<String> extrairEmail(String token) {
        try {
            String email = Jwts.parser()
                    .verifyWith(chave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Optional.ofNullable(email);
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Token JWT rejeitado: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
