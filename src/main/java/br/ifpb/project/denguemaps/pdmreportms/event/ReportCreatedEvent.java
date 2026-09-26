package br.ifpb.project.denguemaps.pdmreportms.event;

import br.ifpb.project.denguemaps.pdmreportms.dto.ReportResponseDTO;

/**
 * Evento de domínio emitido após a criação de um report.
 *
 * @param routingKey Chave de roteamento do RabbitMQ (ex: report.focus.created)
 * @param payload    DTO serializado para envio na mensageria
 */
public record ReportCreatedEvent(String routingKey, ReportResponseDTO payload) {}
