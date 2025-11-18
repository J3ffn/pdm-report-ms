package br.ifpb.project.denguemaps.pdmreportms.dto.questionario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionarioResponseDTO {
    private UUID id;
    private String perguntas;
    private String respostas;
    private UUID fkCidadaoId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedBy;
}
