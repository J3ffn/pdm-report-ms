package br.ifpb.project.denguemaps.pdmreportms.security.exception;

public class ExternalAuthException extends RuntimeException {

    // Construtor que recebe a mensagem de erro
    public ExternalAuthException(String message) {
        super(message);
    }

    // Opcional: construtor que recebe a causa
    public ExternalAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
