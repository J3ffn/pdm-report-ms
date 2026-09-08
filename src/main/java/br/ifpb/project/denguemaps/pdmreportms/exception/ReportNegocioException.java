package br.ifpb.project.denguemaps.pdmreportms.exception;

/**
 * Exceção base para erros de regra de negócio do report-ms.
 * Lançada quando uma validação de domínio falha (não é erro técnico).
 * O GlobalExceptionHandler converte para HTTP 422 (Unprocessable Entity).
 */
public class ReportNegocioException extends RuntimeException {

    public ReportNegocioException(String mensagem) {
        super(mensagem);
    }
}
