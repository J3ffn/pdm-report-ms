package br.ifpb.project.denguemaps.pdmreportms.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Resposta detalhada de um único report, incluindo campos específicos do subtipo.
 * Campos do subtipo não aplicável serão null (ex: localDescription para SYMPTOM).
 */
public record ReportDetailResponseDTO(
        UUID reportId,
        String reportType,
        Double lat,
        Double lng,
        Long h3Res8,
        Long h3Res6,
        Boolean isEnabled,
        Boolean isDisease,
        Boolean isVisited,
        UUID personId,
        OffsetDateTime createdAt,

        // FOCUS
        String localDescription,

        // SYMPTOM
        Map<String, String> respostas,
        Integer scoreTotal,
        UUID questionnaireId
) {}
