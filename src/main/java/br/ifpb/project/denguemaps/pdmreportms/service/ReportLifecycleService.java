package br.ifpb.project.denguemaps.pdmreportms.service;

import br.ifpb.project.denguemaps.pdmreportms.dto.ReportDetailResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.event.ReportDisabledEvent;
import br.ifpb.project.denguemaps.pdmreportms.event.ReportVisitedEvent;
import br.ifpb.project.denguemaps.pdmreportms.exception.ResourceNotFoundException;
import br.ifpb.project.denguemaps.pdmreportms.mapper.ReportMapper;
import br.ifpb.project.denguemaps.pdmreportms.model.ReportEntidade;
import br.ifpb.project.denguemaps.pdmreportms.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pelas operações de mutação do ciclo de vida dos reports:
 * desativação (soft delete) e confirmação de visita de campo por agentes de saúde.
 *
 * <p>Separa os comandos de escrita da camada de consulta (CQRS),
 * emitindo eventos desacoplados de domínio para a mensageria após a confirmação transacional.
 */
@Service
@RequiredArgsConstructor
public class ReportLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(ReportLifecycleService.class);

    private final ReportRepository reportRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Desativa um report existente (soft delete).
     *
     * @param id          Identificador único do report
     * @param executorId  UUID do usuário solicitante (cidadão autenticado)
     * @param isAdmin     Flag indicando se o executor possui privilégios de administrador
     */
    @Transactional
    public void desativar(UUID id, UUID executorId, boolean isAdmin) {
        ReportEntidade report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report não encontrado: " + id));

        report.desativar(executorId, isAdmin);
        reportRepository.save(report);

        eventPublisher.publishEvent(new ReportDisabledEvent(ReportMapper.toResponseDTO(report)));
        log.info("Report desativado com sucesso. reportId={}, executorId={}, isAdmin={}", id, executorId, isAdmin);
    }

    /**
     * Marca o report como visitado por um agente de saúde.
     *
     * @param id Identificador do report
     * @return DTO detalhado atualizado
     */
    @Transactional
    public ReportDetailResponseDTO marcarVisitado(UUID id) {
        ReportEntidade report = reportRepository.findByIdAndIsEnabledTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report não encontrado ou inativo: " + id));

        report.marcarVisitado();
        reportRepository.save(report);

        eventPublisher.publishEvent(new ReportVisitedEvent(ReportMapper.toResponseDTO(report)));
        log.info("Report marcado como visitado. reportId={}", id);

        return ReportMapper.toDetailDTO(report);
    }
}
