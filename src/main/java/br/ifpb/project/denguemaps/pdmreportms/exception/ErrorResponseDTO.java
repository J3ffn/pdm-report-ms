package br.ifpb.project.denguemaps.pdmreportms.exception;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Corpo padrão de resposta para todos os erros da API.
 * Inclui um traceId para correlação com logs do Prometheus/Loki
 *
 */
public record ErrorResponseDTO(
        String traceId,
        int status,
        String erro,
        String mensagem,
        OffsetDateTime timestamp,
        List<CampoErro> campos
) {

    /**
     * Detalhe de erro por campo (usado em erros de validação @Valid).
     */
    public record CampoErro(String campo, String mensagem) {}
}
