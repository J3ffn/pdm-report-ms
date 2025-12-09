package br.ifpb.project.denguemaps.pdmreportms.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReportObjetoDTO {
    @NotNull
    private UUID id;
    @NotNull
    private String coordenadas;
    @NotNull
    private String classificacaoRisco;
    @NotNull
    private UUID fkCidadaoID;
}
