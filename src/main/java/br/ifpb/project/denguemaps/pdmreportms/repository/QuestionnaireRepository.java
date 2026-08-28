package br.ifpb.project.denguemaps.pdmreportms.repository;

import br.ifpb.project.denguemaps.pdmreportms.model.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {
    Optional<Questionnaire> findByDiseaseIgnoreCase(String disease);
}