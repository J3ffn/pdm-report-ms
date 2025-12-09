package br.ifpb.project.denguemaps.pdmreportms.repository;

import br.ifpb.project.denguemaps.pdmreportms.entity.Municipio;
import br.ifpb.project.denguemaps.pdmreportms.enums.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MunicipioRepository extends JpaRepository<Municipio, UUID> {
    Optional<Municipio> findByNomeAndEstado(String nome, Estado estado);

    @Query(value = "SELECT * FROM municipio m WHERE m.estado = :estado AND " +
            "((:id IS NOT NULL AND m.municipio_id = :id) OR (:nome IS NOT NULL AND m.nome = :nome))",
            nativeQuery = true)
    Optional<Municipio> buscarPorNomeOuIdEEstado(
            @Param("nome") String nome,
            @Param("id") UUID id,
            @Param("estado") String estado // String para ser compatível com Enum.toString() no Service
    );
}