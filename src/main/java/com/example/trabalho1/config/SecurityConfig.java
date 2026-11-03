package com.example.trabalho1.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.trabalho1.security.JwtAuthenticationFilter;
import com.example.trabalho1.security.JwtService;
import com.example.trabalho1.security.RestAccessDeniedHandler;
import com.example.trabalho1.security.RestAuthenticationEntryPoint;
import com.example.trabalho1.security.UsuarioDetailsService;

/**
 * Segurança stateless com JWT.
 * <ul>
 *   <li>Público: cadastro, login, Swagger e todos os GET do catálogo e das avaliações.</li>
 *   <li>ADMIN: escrita em jogos, categorias e desenvolvedoras.</li>
 *   <li>Demais endpoints exigem token; as regras por usuário e por dono ficam em {@code @PreAuthorize}
 *       (usuários) e no {@code AvaliacaoService} (avaliações).</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] CATALOGO = {
            "/api/jogos/**", "/api/categorias/**", "/api/desenvolvedoras/**"
    };

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            UsuarioDetailsService usuarioDetailsService,
            RestAuthenticationEntryPoint entryPoint,
            RestAccessDeniedHandler accessDeniedHandler) throws Exception {

        // O filtro não é um @Bean de propósito: assim o Spring Boot não o registra também como filtro de servlet.
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, usuarioDetailsService);

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                                .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios", "/api/usuarios/login").permitAll()
                        .requestMatchers(HttpMethod.GET, CATALOGO).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/avaliacoes/**").permitAll()
                        .requestMatchers(HttpMethod.POST, CATALOGO).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, CATALOGO).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, CATALOGO).hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") String origensPermitidas) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origensPermitidas.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
