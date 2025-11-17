package br.ifpb.project.denguemaps.pdmreportms.repository;

import br.ifpb.project.denguemaps.pdmreportms.entity.Cidadao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CidadaoRepository extends JpaRepository<Cidadao, UUID> {
}