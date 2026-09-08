package br.ifpb.project.denguemaps.pdmreportms.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportFocusRequestDTO {

    @NotNull(message = "Latitude é obrigatória")
    @DecimalMin("-90.0") @DecimalMax("90.0")
    private Double lat;

    @NotNull(message = "Longitude é obrigatória")
    @DecimalMin("-180.0") @DecimalMax("180.0")
    private Double lng;

    @NotBlank(message = "Descrição do local é obrigatória")
    private String description;

    @NotBlank(message = "Descrição detalhada do local é obrigatória")
    private String localDescription;

    private String cpfHash;
}
