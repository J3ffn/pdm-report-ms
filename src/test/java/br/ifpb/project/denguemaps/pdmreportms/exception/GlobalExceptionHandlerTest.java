package br.ifpb.project.denguemaps.pdmreportms.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/reports/123");
    }

    @Test
    @DisplayName("ResourceNotFoundException deve ser convertida para HTTP 404")
    void deveRetornar404ParaResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Report não encontrado");

        ResponseEntity<ErrorResponseDTO> response = handler.handleNaoEncontrado(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Report não encontrado", response.getBody().mensagem());
        assertNotNull(response.getBody().traceId());
    }

    @Test
    @DisplayName("AccessDeniedException deve ser convertida para HTTP 403")
    void deveRetornar403ParaAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Sem permissão");

        ResponseEntity<ErrorResponseDTO> response = handler.handleAcessoNegado(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().status());
        assertEquals("Sem permissão", response.getBody().mensagem());
    }

    @Test
    @DisplayName("BusinessRuleException deve ser convertida para HTTP 422")
    void deveRetornar422ParaBusinessRule() {
        BusinessRuleException ex = new BusinessRuleException("Report já está inativo");

        ResponseEntity<ErrorResponseDTO> response = handler.handleNegocio(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(422, response.getBody().status());
        assertEquals("Report já está inativo", response.getBody().mensagem());
    }

    @Test
    @DisplayName("IllegalArgumentException deve ser convertida para HTTP 400")
    void deveRetornar400ParaIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("UUID inválido");

        ResponseEntity<ErrorResponseDTO> response = handler.handleRequisicaoInvalida(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("UUID inválido", response.getBody().mensagem());
    }

    @Test
    @DisplayName("Exception genérica deve ser convertida para HTTP 500 sem vazar stack trace")
    void deveRetornar500ParaExceptionGenerica() {
        Exception ex = new RuntimeException("Falha catastrófica de conexão SQL");

        ResponseEntity<ErrorResponseDTO> response = handler.handleErroInterno(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertFalse(response.getBody().mensagem().contains("SQL"), "Não deve expor detalhes internos no body");
    }
}
