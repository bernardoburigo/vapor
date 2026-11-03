package com.example.trabalho1.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.example.trabalho1.exception.ErrorResponse;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

/**
 * Escreve o {@link ErrorResponse} padrão direto na resposta. Necessário porque os erros de segurança
 * acontecem nos filtros, antes de o Spring MVC (e o {@code @RestControllerAdvice}) entrar em ação.
 */
@Component
@RequiredArgsConstructor
class JsonErrorWriter {

    private final ObjectMapper objectMapper;

    void escrever(HttpServletResponse response, HttpStatus status, String mensagem) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                new ErrorResponse(status.value(), status.getReasonPhrase(), mensagem)));
    }
}
