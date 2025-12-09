package br.ifpb.project.denguemaps.pdmreportms.dto.report;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReportCriacaoDTO {
    @NotBlank
    private String coordenadas;
    @NotBlank
    private String classificacaoRisco;
    @NotBlank
    private UUID fkCidadaoID;
}
