package br.ifpb.project.denguemaps.pdmreportms.repository;

import br.ifpb.project.denguemaps.pdmreportms.model.ReportEntidade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportRepository extends JpaRepository<ReportEntidade, UUID> {

    /** Lista todos os reports ativos — para agentes e admins. */
    Page<ReportEntidade> findAllByIsEnabledTrue(Pageable pageable);

    /** Lista reports ativos de um cidadão específico. */
    Page<ReportEntidade> findAllByFkPersonIdAndIsEnabledTrue(UUID fkPersonId, Pageable pageable);
}
