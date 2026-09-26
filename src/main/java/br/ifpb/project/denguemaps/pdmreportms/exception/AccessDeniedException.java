package br.ifpb.project.denguemaps.pdmreportms.exception;

/**
 * Exceção lançada quando o usuário autenticado não possui permissão para executar a operação.
 * Mapeada para HTTP 403 (Forbidden).
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
