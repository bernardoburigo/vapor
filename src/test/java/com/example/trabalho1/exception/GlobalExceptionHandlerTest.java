package com.example.trabalho1.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Cobre handlers que as rotas atuais não conseguem disparar de forma natural. */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void parametroObrigatorioAusenteDeveRetornar400() {
        ResponseEntity<ErrorResponse> resposta = handler.handleMissingParameter(
                new MissingServletRequestParameterException("jogoId", "Long"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody().message()).contains("jogoId");
    }

    @Test
    void violacaoDeIntegridadeDeveRetornar409SemVazarDetalhesDoBanco() {
        ResponseEntity<ErrorResponse> resposta = handler.handleDataIntegrity(
                new DataIntegrityViolationException("ERROR: violates foreign key constraint \"fk_secreto\""));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().message()).doesNotContain("fk_secreto");
    }

    @Test
    void metodoNaoSuportadoDeveRetornar405ComHeaderAllow() {
        ResponseEntity<ErrorResponse> resposta = handler.handleMethodNotSupported(
                new HttpRequestMethodNotSupportedException("PATCH", List.of("GET", "POST")));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(resposta.getHeaders().getAllow()).contains(HttpMethod.GET, HttpMethod.POST);
    }

    @Test
    void tipoDeConteudoNaoSuportadoDeveRetornar415() {
        ResponseEntity<ErrorResponse> resposta = handler.handleMediaTypeNotSupported(
                new HttpMediaTypeNotSupportedException(MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON)));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    void formatoNaoAceitoDeveRetornar406() {
        ResponseEntity<ErrorResponse> resposta = handler.handleMediaTypeNotAcceptable(
                new HttpMediaTypeNotAcceptableException(List.of(MediaType.APPLICATION_JSON)));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
    }

    @Test
    void rotaInexistenteDeveRetornar404() {
        ResponseEntity<ErrorResponse> resposta = handler.handleNoResource(
                new NoResourceFoundException(HttpMethod.GET, "api/nada", "api/nada"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody().message()).contains("api/nada");
    }

    @Test
    void acessoNegadoDeveRetornar403() {
        ResponseEntity<ErrorResponse> resposta = handler.handleAccessDenied(new AccessDeniedException("negado"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void falhaDeAutenticacaoDeveRetornar401() {
        ResponseEntity<ErrorResponse> resposta = handler.handleAuthentication(new BadCredentialsException("x"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void ordenacaoPorPropriedadeInexistenteDeveRetornar400() {
        ResponseEntity<ErrorResponse> resposta = handler.handleInvalidDataAccess(new InvalidDataAccessApiUsageException(
                "Sort expression '[\"string\"]: ASC' must only contain property references or aliases"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody().message()).startsWith("Parâmetro de ordenação inválido");
    }

    @Test
    void outroUsoInvalidoDoBancoContinuaSendoErroInterno() {
        ResponseEntity<ErrorResponse> resposta =
                handler.handleInvalidDataAccess(new InvalidDataAccessApiUsageException("outro problema qualquer"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
