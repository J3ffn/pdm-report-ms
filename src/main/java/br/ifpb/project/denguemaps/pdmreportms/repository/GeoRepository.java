package br.ifpb.project.denguemaps.pdmreportms.repository;

import br.ifpb.project.denguemaps.pdmreportms.model.GeoEntidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositório para persistência do ponto geográfico (lat/lng).
 *
 * <p>O pdm-report-ms persiste apenas a localização bruta (lat/lng).
 * Os índices H3 (h3_res8, h3_res6) são preenchidos de forma assíncrona
 * pelo pdm-geo-worker após consumir o evento do RabbitMQ.
 */
public interface GeoRepository extends JpaRepository<GeoEntidade, UUID> {}
