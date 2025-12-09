package br.ifpb.project.denguemaps.pdmreportms.service;


import br.ifpb.project.denguemaps.pdmreportms.dto.report.ReportCriacaoDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.report.ReportObjetoDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.report.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.report.ReportAtualizarDTO;
import br.ifpb.project.denguemaps.pdmreportms.entity.Cidadao;
import br.ifpb.project.denguemaps.pdmreportms.entity.Municipio;
import br.ifpb.project.denguemaps.pdmreportms.entity.Report;
import br.ifpb.project.denguemaps.pdmreportms.enums.Estado;
import br.ifpb.project.denguemaps.pdmreportms.repository.CidadaoRepository;
import br.ifpb.project.denguemaps.pdmreportms.repository.MunicipioRepository;
import br.ifpb.project.denguemaps.pdmreportms.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final ReportRepository reportRepository;
    private final MunicipioRepository municipioRepository;
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
    public List<ReportResponseDTO> buscarReportObjeto(ReportObjetoDTO reportObjetoDTO){
        List<Report> Listreport = reportRepository.findByObjetoReport(
                reportObjetoDTO.getId(),
                reportObjetoDTO.getCoordenadas(),
                reportObjetoDTO.getClassificacaoRisco(),
                reportObjetoDTO.getFkCidadaoID()
        );

        return mapearReportsResponseDTO(Listreport);

    }


    @Transactional(readOnly = true)
    public List<ReportResponseDTO> buscarReportsPorMunicipio(String nome, UUID id, Estado estado) {
        if (id == null && (nome == null || nome.isBlank())) {
            throw new IllegalArgumentException("É necessário fornecer o ID do Município ou o Nome do Município.");
        }
        Optional<Municipio> municipioOpt = Optional.empty();
        if (id != null) {
            municipioOpt = municipioRepository.buscarPorNomeOuIdEEstado(null, id, estado.name());
        }
        if (municipioOpt.isEmpty() && nome != null && !nome.isBlank()) {
            municipioOpt = municipioRepository.buscarPorNomeOuIdEEstado(nome, null, estado.name());
        }
        if (municipioOpt.isEmpty()) {
            throw new IllegalArgumentException("Município não encontrado para os critérios fornecidos (ID e/ou Nome).");
        }
        UUID municipioId = municipioOpt.get().getId();
        List<Report> reports = reportRepository.buscarReportsPorMunicipioId(municipioId);
        if (reports.isEmpty()) {
            return Collections.emptyList();
        }
        return reports.stream()
                .map(this::retornarResponse)
                .collect(Collectors.toList());
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
