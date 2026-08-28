package br.ifpb.project.denguemaps.pdmreportms.controller;

import br.ifpb.project.denguemaps.pdmreportms.model.Questionnaire;
import br.ifpb.project.denguemaps.pdmreportms.repository.QuestionnaireRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/questionnaire")
public class QuestionnaireController {

    private final QuestionnaireRepository questionnaireRepository;

    public QuestionnaireController(QuestionnaireRepository questionnaireRepository) {
        this.questionnaireRepository = questionnaireRepository;
    }

    @GetMapping("/{disease}")
    public ResponseEntity<Questionnaire> getQuestionnaireByDisease(@PathVariable String disease) {
        return questionnaireRepository.findByDiseaseIgnoreCase(disease)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}