package br.ifpb.project.denguemaps.pdmreportms.service;

import br.ifpb.project.denguemaps.pdmreportms.dto.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportSymptomsRequestDTO;
import br.ifpb.project.denguemaps.pdmreportms.mapper.ReportMapper;
import br.ifpb.project.denguemaps.pdmreportms.model.ReportSymptomsEntidade;
import br.ifpb.project.denguemaps.pdmreportms.producer.ReportPublisher;
import br.ifpb.project.denguemaps.pdmreportms.repository.ReportSymptomsRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportSymptomsService implements ReportService<ReportSymptomsRequestDTO> {

    private static final Logger log = LoggerFactory.getLogger(ReportSymptomsService.class);

    private static final int LIMIAR_DOENCA = 50;

    private final GeoService geoService;
    private final ReportSymptomsRepository symptomsRepository;
    private final ReportPublisher reportPublisher;

    @Override
    @Transactional
    public ReportResponseDTO criar(ReportSymptomsRequestDTO dto, UUID cidadaoId) {

        log.info("Criando report de sintomas. cidadaoId={}, score={}, templateId={}",
                cidadaoId, dto.getScoreTotal(), dto.getQuestionnaireId());

        ReportSymptomsEntidade sintomas = new ReportSymptomsEntidade();
        sintomas.setGeo(geoService.criarGeo(dto.getLat(), dto.getLng()));
        sintomas.setDescription("Questionário de sintomas respondido");
        sintomas.setIsEnabled(true);
        sintomas.setIsDisease(dto.getScoreTotal() >= LIMIAR_DOENCA);
        sintomas.setIsVisited(false);
        sintomas.setFkPersonId(cidadaoId);
        sintomas.setCpfHash(dto.getCpfHash());
        sintomas.setRespostas(dto.getRespostas());
        sintomas.setScoreTotal(dto.getScoreTotal());
        sintomas.setFkQuestionnaireId(dto.getQuestionnaireId());

        // JPA insere automaticamente em tb_reports + tb_report_symptoms
        ReportSymptomsEntidade salvo = symptomsRepository.save(sintomas);

        ReportResponseDTO resposta = ReportMapper.toResponseDTO(salvo);
        reportPublisher.publishReportEvent("report.symptom.created", resposta);

        log.info("Report de sintomas criado. reportId={}, isDisease={}", salvo.getId(), salvo.getIsDisease());

        return resposta;
    }
}
