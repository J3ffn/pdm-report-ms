package br.ifpb.project.denguemaps.pdmreportms.service;


import br.ifpb.project.denguemaps.pdmreportms.dto.report.ReportCriacaoDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.report.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.report.ReportAtualizarDTO;
import br.ifpb.project.denguemaps.pdmreportms.entity.Cidadao;
import br.ifpb.project.denguemaps.pdmreportms.entity.Report;
import br.ifpb.project.denguemaps.pdmreportms.repository.CidadaoRepository;
import br.ifpb.project.denguemaps.pdmreportms.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final ReportRepository reportRepository;
    private final CidadaoRepository cidadaoRepository;

    
    public ReportResponseDTO cadastrarReport(ReportCriacaoDTO reportCriacaoDTO) {
        Report report = formatarReportRetornar(reportCriacaoDTO);
        report.setCreatedAt(OffsetDateTime.now());
        report.setUpdatedBy(OffsetDateTime.now());
        return retornarResponse(
                salvarEntidadeRetornar(report)
        );
    }


    public ReportResponseDTO atualizarReport(ReportAtualizarDTO reportAtualizarDTO){
        Report report = buscarReport(reportAtualizarDTO.getId());
        atualizarReport(report, reportAtualizarDTO);
        return retornarResponse(salvarEntidadeRetornar(report));
    }

    @Transactional(readOnly = true)
    public List<ReportResponseDTO> buscarTodoReport() {
        List<Report> reports = reportRepository.findAll();
        return mapearReportsResponseDTO(reports);
    }

    @Transactional(readOnly = true)
    public List<ReportResponseDTO> buscarReportEspecifico(UUID uuid){
        List<Report> reports = reportRepository.findAllById(Collections.singleton(uuid));
        return mapearReportsResponseDTO(reports);
    }

    @Transactional(readOnly = true)
    public List<ReportResponseDTO> buscarReportCidadaoEspecifico(UUID uuid){
        List<Report> reports = reportRepository.findByCidadao_Id(uuid);
        return mapearReportsResponseDTO(reports);
    }

    public void deletarReportEspecifico(UUID uuid){
        deletarEntidadeNaoRetornar(uuid);
    }

    @Transactional
    public void deletarTodoReportCidadao(UUID uuidCidadao){
        reportRepository.deleteAllByCidadaoId(uuidCidadao);
    }
    // Metodos auxiliares:
    private Report salvarEntidadeRetornar(Report report){
        return reportRepository.save(report);
    }

    private void deletarEntidadeNaoRetornar(UUID uuid){
        reportRepository.deleteById(uuid);
    }
    private Report formatarReportRetornar(ReportCriacaoDTO reportCriacaoDTO){
        Report report = new Report();
        report.setCidadao(buscarCidadao(reportCriacaoDTO.getFkCidadaoID()));
        report.setCoordenadas(reportCriacaoDTO.getCoordenadas());
        report.setClassificacaoRisco(reportCriacaoDTO.getClassificacaoRisco());
        return report;
    }

    private void atualizarReport(Report report, ReportAtualizarDTO reportAtualizarDTO){
        report.setCoordenadas(reportAtualizarDTO.getCoordenadas());
        report.setClassificacaoRisco(reportAtualizarDTO.getClassificacaoRisco());
        report.setUpdatedBy(OffsetDateTime.now());
        report.setCidadao(buscarCidadao(reportAtualizarDTO.getFkCidadaoID()));
    }

    private ReportResponseDTO retornarResponse(Report report){
        ReportResponseDTO reportResponseDTO = new ReportResponseDTO();
        reportResponseDTO.setNomeCidadao(report.getCidadao().getNome());
        reportResponseDTO.setId(report.getId());
        reportResponseDTO.setClassificacaoRisco(report.getClassificacaoRisco());
        reportResponseDTO.setCoordenadas(report.getCoordenadas());
        reportResponseDTO.setUpdatedBy(report.getUpdatedBy());
        reportResponseDTO.setCreatedAt(report.getCreatedAt());
        return reportResponseDTO;
    }

    private Cidadao buscarCidadao(UUID uuid){
        return cidadaoRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Cidadao não encontrado"));
    }

    private Report buscarReport(UUID uuid){
        return reportRepository.findById(uuid).orElseThrow(() -> new IllegalArgumentException("Report não encontrado"));
    }

    private List<ReportResponseDTO> mapearReportsResponseDTO(List<Report> reports) {
        return reports.stream()
                .map(this::retornarResponse)
                .collect(Collectors.toList());
    }
}
