package br.ifpb.project.denguemaps.pdmreportms.repository;

import br.ifpb.project.denguemaps.pdmreportms.model.GeoEntidade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface GeoRepository extends JpaRepository<GeoEntidade, UUID> {}
