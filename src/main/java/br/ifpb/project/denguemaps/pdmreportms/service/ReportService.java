package br.ifpb.project.denguemaps.pdmreportms.service;

import br.ifpb.project.denguemaps.pdmreportms.dto.ReportResponseDTO;

import java.util.UUID;

/**
 * Contrato para criação de qualquer tipo de report.
 *
 * @param <T> DTO de entrada específico do tipo de report
 */
public interface ReportService<T> {
    ReportResponseDTO criar(T dto, UUID cidadaoId);
}
