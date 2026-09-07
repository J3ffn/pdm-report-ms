package br.ifpb.project.denguemaps.pdmreportms.mapper;

import br.ifpb.project.denguemaps.pdmreportms.dto.ReportDetailResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.model.ReportEntidade;
import br.ifpb.project.denguemaps.pdmreportms.model.ReportFocusEntidade;
import br.ifpb.project.denguemaps.pdmreportms.model.ReportSymptomsEntidade;

/**
 * Centraliza a conversão de ReportEntidade para os DTOs de resposta.
 */
public final class ReportMapper {

    private ReportMapper() {}

    public static ReportResponseDTO toResponseDTO(ReportEntidade report) {
        return new ReportResponseDTO(
                report.getId(),
                report.getReportType().name(),
                report.getGeo().getLat(),
                report.getGeo().getLng(),
                report.getGeo().getH3Res8(),
                report.getGeo().getH3Res6(),
                report.getIsEnabled(),
                report.getIsDisease(),
                report.getCreatedAt()
        );
    }


    public static ReportDetailResponseDTO toDetailDTO(ReportEntidade report) {
        String localDescription = null;
        java.util.Map<String, String> respostas = null;
        Integer scoreTotal = null;
        java.util.UUID questionnaireId = null;

        if (report instanceof ReportFocusEntidade foco) {
            localDescription = foco.getLocalDescription();
        } else if (report instanceof ReportSymptomsEntidade sintomas) {
            respostas = sintomas.getRespostas();
            scoreTotal = sintomas.getScoreTotal();
            questionnaireId = sintomas.getFkQuestionnaireId();
        }

        return new ReportDetailResponseDTO(
                report.getId(),
                report.getReportType().name(),
                report.getGeo().getLat(),
                report.getGeo().getLng(),
                report.getGeo().getH3Res8(),
                report.getGeo().getH3Res6(),
                report.getIsEnabled(),
                report.getIsDisease(),
                report.getIsVisited(),
                report.getFkPersonId(),
                report.getCreatedAt(),
                localDescription,
                respostas,
                scoreTotal,
                questionnaireId
        );
    }
}
