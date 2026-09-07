package br.ifpb.project.denguemaps.pdmreportms.service;

import br.ifpb.project.denguemaps.pdmreportms.dto.ReportFocusRequestDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.mapper.ReportMapper;
import br.ifpb.project.denguemaps.pdmreportms.model.ReportFocusEntidade;
import br.ifpb.project.denguemaps.pdmreportms.producer.ReportPublisher;
import br.ifpb.project.denguemaps.pdmreportms.repository.ReportFocusRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportFocusService implements ReportService<ReportFocusRequestDTO> {

    private static final Logger log = LoggerFactory.getLogger(ReportFocusService.class);

    private final GeoService geoService;
    private final ReportFocusRepository focusRepository;
    private final ReportPublisher reportPublisher;

    @Override
    @Transactional
    public ReportResponseDTO criar(ReportFocusRequestDTO dto, UUID cidadaoId) {

        log.info("Criando report de foco. cidadaoId={}, lat={}, lng={}", cidadaoId, dto.getLat(), dto.getLng());

        ReportFocusEntidade foco = new ReportFocusEntidade();
        foco.setGeo(geoService.criarGeo(dto.getLat(), dto.getLng()));
        foco.setDescription(dto.getDescription());
        foco.setLocalDescription(dto.getLocalDescription());
        foco.setIsEnabled(true);
        foco.setIsDisease(false);
        foco.setIsVisited(false);
        foco.setFkPersonId(cidadaoId);
        foco.setCpfHash(dto.getCpfHash());

        // JPA insere automaticamente em tb_reports + tb_report_focus
        ReportFocusEntidade salvo = focusRepository.save(foco);

        ReportResponseDTO resposta = ReportMapper.toResponseDTO(salvo);
        reportPublisher.publishReportEvent("report.focus.created", resposta);

        log.info("Report de foco criado. reportId={}", salvo.getId());

        return resposta;
    }
}
