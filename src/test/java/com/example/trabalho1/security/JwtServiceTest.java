package com.example.trabalho1.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.trabalho1.entity.Role;
import com.example.trabalho1.entity.Usuario;

class JwtServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-pelo-menos-32-bytes-ok";

    private JwtService jwtService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SEGREDO, 60);

        usuario = new Usuario();
        usuario.setId(7L);
        usuario.setEmail("bernardo@vapor.com");
        usuario.setRole(Role.ADMIN);
    }

    @Test
    void deveGerarTokenQueDevolveOEmailDoUsuario() {
        String token = jwtService.gerarToken(usuario);

        assertThat(jwtService.extrairEmail(token)).contains("bernardo@vapor.com");
    }

    @Test
    void tokenDeveConterIdEPerfilNasClaims() {
        String token = jwtService.gerarToken(usuario);
        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);

        assertThat(payload).contains("\"id\":7").contains("\"role\":\"ADMIN\"").contains("\"sub\":\"bernardo@vapor.com\"");
    }

    @Test
    void deveInformarAValidadeEmSegundos() {
        assertThat(jwtService.getValidadeEmSegundos()).isEqualTo(3600L);
    }

    @Test
    void deveRejeitarTokenExpirado() {
        JwtService jaExpirado = new JwtService(SEGREDO, -1);

        assertThat(jwtService.extrairEmail(jaExpirado.gerarToken(usuario))).isEmpty();
    }

    @Test
    void deveRejeitarTokenAssinadoComOutraChave() {
        JwtService outraChave = new JwtService("outro-segredo-totalmente-diferente-32-bytes!", 60);

        assertThat(jwtService.extrairEmail(outraChave.gerarToken(usuario))).isEmpty();
    }

    @Test
    void deveRejeitarTokenAdulterado() {
        String token = jwtService.gerarToken(usuario);
        String[] partes = token.split("\\.");
        // troca o perfil no payload mantendo a assinatura original
        String payloadAdulterado = Base64.getUrlEncoder().withoutPadding().encodeToString(
                new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8)
                        .replace("bernardo@vapor.com", "invasor@vapor.com")
                        .getBytes(StandardCharsets.UTF_8));

        assertThat(jwtService.extrairEmail(partes[0] + "." + payloadAdulterado + "." + partes[2])).isEmpty();
    }

    @Test
    void deveRejeitarTextoQueNaoEUmToken() {
        assertThat(jwtService.extrairEmail("isto-nao-e-um-jwt")).isEmpty();
        assertThat(jwtService.extrairEmail("")).isEmpty();
    }

    @Test
    void deveFalharNaInicializacaoComSegredoCurto() {
        assertThatThrownBy(() -> new JwtService("curto", 60))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }
}
