package br.ifpb.project.denguemaps.pdmreportms.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Centraliza o tratamento de todos os erros da API.
 *
 * Cada handler:
 *   1. Gera um traceId único (UUID) para correlação no Prometheus/Loki
 *   2. Loga o erro com o traceId (o operador busca pelo traceId nos logs)
 *   3. Retorna um corpo padronizado sem vazar detalhes técnicos ao cliente
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidacao(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String traceId = UUID.randomUUID().toString();

        List<ErrorResponseDTO.CampoErro> campos = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> new ErrorResponseDTO.CampoErro(e.getField(), e.getDefaultMessage()))
                .toList();

        log.warn("[{}] Erro de validação em {} {}: {}",
                traceId, request.getMethod(), request.getRequestURI(), campos);

        ErrorResponseDTO body = new ErrorResponseDTO(
                traceId,
                HttpStatus.BAD_REQUEST.value(),
                "Dados inválidos",
                "Um ou mais campos estão incorretos. Corrija e tente novamente.",
                OffsetDateTime.now(),
                campos
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNaoEncontrado(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        String traceId = UUID.randomUUID().toString();

        log.warn("[{}] Recurso não encontrado em {} {}: {}",
                traceId, request.getMethod(), request.getRequestURI(), ex.getMessage());

        ErrorResponseDTO body = new ErrorResponseDTO(
                traceId,
                HttpStatus.NOT_FOUND.value(),
                "Não encontrado",
                ex.getMessage(),
                OffsetDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler({AccessDeniedException.class, org.springframework.security.access.AccessDeniedException.class})
    public ResponseEntity<ErrorResponseDTO> handleAcessoNegado(
            Exception ex,
            HttpServletRequest request) {

        String traceId = UUID.randomUUID().toString();

        log.warn("[{}] Acesso negado em {} {}: {}",
                traceId, request.getMethod(), request.getRequestURI(), ex.getMessage());

        ErrorResponseDTO body = new ErrorResponseDTO(
                traceId,
                HttpStatus.FORBIDDEN.value(),
                "Acesso negado",
                ex.getMessage() != null ? ex.getMessage() : "Você não possui permissão para executar esta operação.",
                OffsetDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponseDTO> handleNegocio(
            BusinessRuleException ex,
            HttpServletRequest request) {

        String traceId = UUID.randomUUID().toString();

        log.warn("[{}] Erro de negócio em {} {}: {}",
                traceId, request.getMethod(), request.getRequestURI(), ex.getMessage());

        ErrorResponseDTO body = new ErrorResponseDTO(
                traceId,
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Operação inválida",
                ex.getMessage(),
                OffsetDateTime.now(),
                null
        );

        return ResponseEntity.unprocessableEntity().body(body);
    }

    @ExceptionHandler({IllegalArgumentException.class, org.springframework.http.converter.HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponseDTO> handleRequisicaoInvalida(
            Exception ex,
            HttpServletRequest request) {

        String traceId = UUID.randomUUID().toString();

        log.warn("[{}] Requisição inválida em {} {}: {}",
                traceId, request.getMethod(), request.getRequestURI(), ex.getMessage());

        ErrorResponseDTO body = new ErrorResponseDTO(
                traceId,
                HttpStatus.BAD_REQUEST.value(),
                "Requisição inválida",
                ex.getMessage(),
                OffsetDateTime.now(),
                null
        );

        return ResponseEntity.badRequest().body(body);
    }

    // -------------------------------------------------------------------------
    // Erros inesperados (nunca vazar detalhes técnicos ao cliente)
    // -------------------------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleErroInterno(
            Exception ex,
            HttpServletRequest request) {

        String traceId = UUID.randomUUID().toString();

        // Log completo com stack trace — apenas nos logs internos, não na resposta
        log.error("[{}] Erro interno em {} {}: {}",
                traceId, request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponseDTO body = new ErrorResponseDTO(
                traceId,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno",
                "Ocorreu um erro inesperado. Informe o código ao suporte: " + traceId,
                OffsetDateTime.now(),
                null
        );

        return ResponseEntity.internalServerError().body(body);
    }
}
