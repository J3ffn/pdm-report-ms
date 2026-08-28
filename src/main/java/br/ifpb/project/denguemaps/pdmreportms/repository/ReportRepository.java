package br.ifpb.project.denguemaps.pdmreportms.repository;

import br.ifpb.project.denguemaps.pdmreportms.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
}