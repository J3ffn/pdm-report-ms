package br.ifpb.project.denguemaps.pdmreportms.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "tb_questionnaires")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Questionnaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String disease; // ex: "DENGUE"

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tb_questionnaire_questions", joinColumns = @JoinColumn(name = "questionnaire_id"))
    @Column(name = "question")
    private List<String> questions;
}