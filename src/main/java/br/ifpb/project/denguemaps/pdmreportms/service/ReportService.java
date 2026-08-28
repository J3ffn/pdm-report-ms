package br.ifpb.project.denguemaps.pdmreportms.service;

import br.ifpb.project.denguemaps.pdmreportms.model.Report;
import br.ifpb.project.denguemaps.pdmreportms.producer.ReportPublisher;
import br.ifpb.project.denguemaps.pdmreportms.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportPublisher reportPublisher;

    public ReportService(ReportRepository reportRepository, ReportPublisher reportPublisher) {
        this.reportRepository = reportRepository;
        this.reportPublisher = reportPublisher;
    }

    @Transactional
    public Report createReport(Report report) {
        Report savedReport = reportRepository.save(report);
        // Aqui usamos o nome real do método do teu publisher
        reportPublisher.publishReportEvent("report.created", savedReport);
        return savedReport;
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Optional<Report> getReportById(Long id) {
        return reportRepository.findById(id);
    }

    @Transactional
    public Optional<Report> updateReport(Long id, Report updatedReport) {
        return reportRepository.findById(id)
                .map(existingReport -> {
                    existingReport.setDescription(updatedReport.getDescription());
                    existingReport.setLatitude(updatedReport.getLatitude());
                    existingReport.setLongitude(updatedReport.getLongitude());
                    existingReport.setDisease(updatedReport.getDisease());
                    existingReport.setStatus(updatedReport.getStatus());
                    return reportRepository.save(existingReport);
                });
    }

    @Transactional
    public boolean deleteReport(Long id) {
        return reportRepository.findById(id)
                .map(report -> {
                    reportRepository.delete(report);
                    return true;
                }).orElse(false);
    }
}