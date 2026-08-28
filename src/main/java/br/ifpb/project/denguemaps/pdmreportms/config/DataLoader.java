package br.ifpb.project.denguemaps.pdmreportms.config;

import br.ifpb.project.denguemaps.pdmreportms.model.Questionnaire;
import br.ifpb.project.denguemaps.pdmreportms.repository.QuestionnaireRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataLoader implements CommandLineRunner {

    private final QuestionnaireRepository questionnaireRepository;

    public DataLoader(QuestionnaireRepository questionnaireRepository) {
        this.questionnaireRepository = questionnaireRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Se o questionário de Dengue não existir no H2, nós criamos as perguntas padrão
        if (questionnaireRepository.findByDiseaseIgnoreCase("DENGUE").isEmpty()) {
            Questionnaire dengueForm = Questionnaire.builder()
                    .disease("DENGUE")
                    .questions(Arrays.asList(
                            "Existe água parada descoberta no local?",
                            "O foco está localizado em área residencial ou comercial?",
                            "Qual o tipo de recipiente? (Pneu, Garrafa, Caixa d'água, Prato de planta)",
                            "Há presença visível de larvas se movimentando na água?"
                    ))
                    .build();

            questionnaireRepository.save(dengueForm);
            System.out.println("[Database] Questionário inicial de DENGUE carregado com sucesso!");
        }
    }
}