package br.ifpb.project.denguemaps.pdmreportms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReportCriacaoDTO {
    private String coordenadas;
    private String classificacaoRisco;
    private UUID fkCidadaoID;
}
