package br.ifpb.project.denguemaps.pdmreportms.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    /** Descrição do local com potencial de proliferação do mosquito. */
    @NotBlank(message = "Descrição do local é obrigatória")
    private String localDescription;

    /**
     * CPF do cidadão anônimo (sem conta) — 11 dígitos numéricos, sem pontos ou traços.
     * Obrigatório quando o request não possui JWT de autenticação.
     * O CPF bruto NUNCA é persistido: o servidor aplica AES-256-GCM antes de salvar.
     */
    @Pattern(
            regexp = "\\d{11}",
            message = "CPF deve conter exatamente 11 dígitos numéricos (sem pontos ou traços)"
    )
    private String cpf;
}
