package br.ifpb.project.denguemaps.pdmreportms.repository;

import br.ifpb.project.denguemaps.pdmreportms.model.ReportFocusEntidade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ReportFocusRepository extends JpaRepository<ReportFocusEntidade, UUID> {}
