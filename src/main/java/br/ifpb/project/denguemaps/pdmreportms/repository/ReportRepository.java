package br.ifpb.project.denguemaps.pdmreportms.repository;

import br.ifpb.project.denguemaps.pdmreportms.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {
    public List<Report> findByCidadao_Id(UUID uuid);

    public void deleteAllByCidadaoId(UUID uuid);

    @Query(value = "SELECT * FROM report r WHERE " +
            // 1. Busca por ID
            "(:id IS NOT NULL AND r.report_id = :id) OR " +
            // 2. Busca por Coordenadas - FORÇAR CAST do parâmetro para jsonb
            "(:coordenadas IS NOT NULL AND r.coordenadas = CAST(:coordenadas AS jsonb)) OR " +
            // 3. Busca por Classificação de Risco
            "(:classificacaoRisco IS NOT NULL AND r.classificacao_risco = :classificacaoRisco) OR " +
            // 4. Busca por ID do Cidadão
            "(:cidadaoId IS NOT NULL AND r.fk_cidadao_id = :cidadaoId)",
            nativeQuery = true) // <-- ESSENCIAL: Indica que é SQL nativo
    List<Report> findByObjetoReport(
            @Param("id") UUID id,
            @Param("coordenadas") String coordenadas,
            @Param("classificacaoRisco") String classificacaoRisco,
            @Param("cidadaoId") UUID cidadaoId
    );

    @Query("SELECT r FROM Report r " +
            "JOIN r.cidadao c " +      // 1. Navega de Report para Cidadao
            "JOIN c.endereco e " +     // 2. Navega de Cidadao para Endereco (Assumindo que Cidadao tem o campo 'endereco')
            "WHERE e.municipio.id = :municipioId") // 3. Compara o ID do Município no Endereco
    List<Report> buscarReportsPorMunicipioId(@Param("municipioId") UUID municipioId);

}