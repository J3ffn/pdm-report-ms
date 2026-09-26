package br.ifpb.project.denguemaps.pdmreportms.exception;

/**
 * Exceção base para erros de regras de negócio da aplicação.
 * Mapeada para HTTP 422 (Unprocessable Entity).
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
