package br.ifpb.project.denguemaps.pdmreportms.model;

import br.ifpb.project.denguemaps.pdmreportms.model.enums.ReportType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "tb_report_symptoms")
@DiscriminatorValue("SYMPTOM")
@Getter @Setter @NoArgsConstructor
public class ReportSymptomsEntidade extends ReportEntidade {

    /**
     * Respostas como JSON: { "perguntaId": "opcaoId" }
     * Armazena o mapeamento de qual opção foi escolhida para cada pergunta.
     */
    @Type(JsonType.class)
    @Column(name = "respostas", columnDefinition = "json")
    private Map<String, String> respostas;

    /**
     * Score calculado pelo frontend com base nos pesos das opções selecionadas.
     * Valores 0-100. Acima do limiar (≥ 50) → isDisease = true.
     */
    @Column(name = "score_total", nullable = false)
    private Integer scoreTotal;


    @Column(name = "fk_questionnaire_id", nullable = false)
    private UUID fkQuestionnaireId;

    @Override
    public ReportType getReportType() {
        return ReportType.SYMPTOM;
    }
}
