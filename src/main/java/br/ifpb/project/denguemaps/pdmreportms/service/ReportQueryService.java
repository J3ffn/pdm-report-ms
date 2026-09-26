package br.ifpb.project.denguemaps.pdmreportms.service;

import br.ifpb.project.denguemaps.pdmreportms.dto.ReportDetailResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.exception.ResourceNotFoundException;
import br.ifpb.project.denguemaps.pdmreportms.mapper.ReportMapper;
import br.ifpb.project.denguemaps.pdmreportms.model.ReportEntidade;
import br.ifpb.project.denguemaps.pdmreportms.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável exclusivamente por operações de consulta (leitura) de reports.
 * Em conformidade com o princípio CQRS, não executa alterações de estado nem produz eventos de mensageria.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportQueryService {

    private final ReportRepository reportRepository;

    /**
     * Busca um report por ID com todos os detalhes do subtipo.
     * Apenas reports ativos são retornados.
     */
    public ReportDetailResponseDTO buscarPorId(UUID id) {
        ReportEntidade report = reportRepository.findByIdAndIsEnabledTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report não encontrado ou inativo: " + id));

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
        if (cidadaoId == null) {
            return Page.empty(pageable);
        }
        return reportRepository.findAllByFkPersonIdAndIsEnabledTrue(cidadaoId, pageable)
                .map(ReportMapper::toResponseDTO);
    }
}
