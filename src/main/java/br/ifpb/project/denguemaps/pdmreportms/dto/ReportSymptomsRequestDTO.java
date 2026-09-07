package br.ifpb.project.denguemaps.pdmreportms.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSymptomsRequestDTO {

    @NotNull(message = "Latitude é obrigatória")
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double lat;

    @NotNull(message = "Longitude é obrigatória")
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double lng;

    /**
     * Mapa de respostas: chave = perguntaId (UUID como String), valor = opcaoId (UUID como String).
     * O frontend envia as respostas já selecionadas pelo cidadão.
     * Exemplo: { "3f4a...": "9c8b..." }
     */
    @NotEmpty(message = "As respostas não podem estar vazias")
    private Map<String, String> respostas;

    /**
     * Score pré-calculado pelo frontend com base nos pesos (weight) de cada opção.
     * O frontend já buscou o template com os weights e os somou.
     * Enviado como inteiro (0-100).
     */
    @NotNull(message = "Score total é obrigatório")
    @Min(value = 0)
    @Max(value = 100)
    private Integer scoreTotal;

    /** ID do template que originou este questionário */
    @NotNull(message = "ID do questionário é obrigatório")
    private UUID questionnaireId;

    /** Hash do CPF para cidadãos sem conta (opcional se tiver JWT) */
    private String cpfHash;
}
