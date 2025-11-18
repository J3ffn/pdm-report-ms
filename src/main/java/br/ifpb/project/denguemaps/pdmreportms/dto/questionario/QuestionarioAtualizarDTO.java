package br.ifpb.project.denguemaps.pdmreportms.dto.questionario;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionarioAtualizarDTO {
    @NotBlank
    private UUID id;
    @NotBlank
    private String perguntas;
    @NotBlank
    private String respostas;
    @NotBlank
    private UUID fkCidadaoId;
}
