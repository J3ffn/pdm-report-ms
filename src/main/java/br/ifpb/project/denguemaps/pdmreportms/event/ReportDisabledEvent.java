package br.ifpb.project.denguemaps.pdmreportms.event;

import br.ifpb.project.denguemaps.pdmreportms.dto.ReportResponseDTO;

/**
 * Evento de domínio emitido após a desativação (soft delete) de um report.
 *
 * @param payload DTO do report desativado para remoção no heatmap do pdm-geo-ms
 */
public record ReportDisabledEvent(ReportResponseDTO payload) {

    public String routingKey() {
        return "report.disabled";
    }
}
