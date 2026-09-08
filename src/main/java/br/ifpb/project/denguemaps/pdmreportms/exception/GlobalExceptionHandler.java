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

    @ExceptionHandler(ReportNegocioException.class)
    public ResponseEntity<ErrorResponseDTO> handleNegocio(
            ReportNegocioException ex,
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
