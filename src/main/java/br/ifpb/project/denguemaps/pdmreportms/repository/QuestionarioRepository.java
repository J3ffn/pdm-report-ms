package br.ifpb.project.denguemaps.pdmreportms.repository;

import br.ifpb.project.denguemaps.pdmreportms.entity.Questionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionarioRepository extends JpaRepository<Questionario, UUID> {
    public List<Questionario> findAllByCidadaoId(UUID uuid);

    public void deleteAllByCidadaoId(UUID uuid);
}
