package br.ifpb.project.denguemaps.pdmreportms.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta padrão após criação de qualquer tipo de report.
 * Serializado automaticamente pelo Jackson para JSON.
 */
public record ReportResponseDTO(
        UUID reportId,
        String reportType,
        Double lat,
        Double lng,
        Long h3Res8,
        Long h3Res6,
        Boolean isEnabled,
        Boolean isDisease,
        OffsetDateTime createdAt
) {}
