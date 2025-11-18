package br.ifpb.project.denguemaps.pdmreportms.dto.questionario;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionarioCriarDTO {
    @NotBlank
    private String perguntas;
    @NotBlank
    private String respostas;
    @NotBlank
    private UUID fkCidadaoId;
}
