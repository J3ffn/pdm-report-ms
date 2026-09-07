package br.ifpb.project.denguemaps.pdmreportms.service;

import br.ifpb.project.denguemaps.pdmreportms.dto.ReportDetailResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.exception.ReportNegocioException;
import br.ifpb.project.denguemaps.pdmreportms.mapper.ReportMapper;
import br.ifpb.project.denguemaps.pdmreportms.model.ReportEntidade;
import br.ifpb.project.denguemaps.pdmreportms.producer.ReportPublisher;
import br.ifpb.project.denguemaps.pdmreportms.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportQueryService {

    private static final Logger log = LoggerFactory.getLogger(ReportQueryService.class);

    private final ReportRepository reportRepository;
    private final ReportPublisher reportPublisher;

    /**
     * Busca um report por ID com todos os detalhes do subtipo.
     * Apenas reports ativos são retornados.
     */
    public ReportDetailResponseDTO buscarPorId(UUID id) {
        ReportEntidade report = reportRepository.findById(id)
                .filter(ReportEntidade::getIsEnabled)
                .orElseThrow(() -> new ReportNegocioException("Report não encontrado ou inativo."));

        return ReportMapper.toDetailDTO(report);
    }

    /**
     * Lista todos os reports ativos (paginado).
     * Restrito a agentes de saúde e administradores.
     */
    public Page<ReportResponseDTO> listarTodos(Pageable pageable) {
        return reportRepository.findAllByIsEnabledTrue(pageable)
                .map(ReportMapper::toResponseDTO);
    }

    /**
     * Lista os reports ativos do cidadão autenticado (paginado).
     * Cidadãos anônimos (sem JWT) não têm histórico.
     */
    public Page<ReportResponseDTO> listarMeus(UUID cidadaoId, Pageable pageable) {
        return reportRepository.findAllByFkPersonIdAndIsEnabledTrue(cidadaoId, pageable)
                .map(ReportMapper::toResponseDTO);
    }

    /**
     * Soft delete: desativa o report (is_enabled = false).
     *
     * Regras:
     * - Cidadão só pode desativar o próprio report.
     * - Admin pode desativar qualquer report.
     * - Publica evento "report.disabled" para que o pdm-geo-ms remova do heatmap.
     */
    @Transactional
    public void desativar(UUID id, UUID cidadaoId, boolean isAdmin) {
        ReportEntidade report = reportRepository.findById(id)
                .orElseThrow(() -> new ReportNegocioException("Report não encontrado."));

        if (!report.getIsEnabled()) {
            throw new ReportNegocioException("Report já está inativo.");
        }

        boolean ehDono = report.getFkPersonId() != null && report.getFkPersonId().equals(cidadaoId);
        if (!isAdmin && !ehDono) {
            throw new ReportNegocioException("Sem permissão para desativar este report.");
        }

        report.setIsEnabled(false);
        reportRepository.save(report);

        reportPublisher.publishReportEvent("report.disabled", ReportMapper.toResponseDTO(report));
        log.info("Report desativado. reportId={}, por={}", id, isAdmin ? "admin" : cidadaoId);
    }

    /**
     * Marca o report como visitado por um agente de saúde.
     * Publica evento "report.visited" para rastreamento.
     */
    @Transactional
    public ReportDetailResponseDTO marcarVisitado(UUID id) {
        ReportEntidade report = reportRepository.findById(id)
                .filter(ReportEntidade::getIsEnabled)
                .orElseThrow(() -> new ReportNegocioException("Report não encontrado ou inativo."));

        report.setIsVisited(true);
        reportRepository.save(report);

        reportPublisher.publishReportEvent("report.visited", ReportMapper.toResponseDTO(report));
        log.info("Report marcado como visitado. reportId={}", id);

        return ReportMapper.toDetailDTO(report);
    }
}
