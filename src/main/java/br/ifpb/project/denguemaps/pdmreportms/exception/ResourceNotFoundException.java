package br.ifpb.project.denguemaps.pdmreportms.exception;

/**
 * Exceção lançada quando um recurso solicitado não é encontrado no sistema.
 * Mapeada para HTTP 404 (Not Found).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
