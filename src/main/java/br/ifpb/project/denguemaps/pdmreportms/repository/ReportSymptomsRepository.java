package br.ifpb.project.denguemaps.pdmreportms.repository;

import br.ifpb.project.denguemaps.pdmreportms.model.ReportSymptomsEntidade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ReportSymptomsRepository extends JpaRepository<ReportSymptomsEntidade, UUID> {}
