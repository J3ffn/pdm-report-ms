package br.ifpb.project.denguemaps.pdmreportms.event;

import br.ifpb.project.denguemaps.pdmreportms.dto.ReportResponseDTO;

/**
 * Evento de domínio emitido após a visitação de um report por um agente de saúde.
 *
 * @param payload DTO do report visitado para atualização do status no pdm-geo-ms
 */
public record ReportVisitedEvent(ReportResponseDTO payload) {

    public String routingKey() {
        return "report.visited";
    }
}
