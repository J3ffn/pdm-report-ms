package br.ifpb.project.denguemaps.pdmreportms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Report {

    @Id
    @Column(name = "report_id")
    @GeneratedValue
    private UUID id;

    @Column(columnDefinition = "jsonb", nullable = false)
    @Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    private String coordenadas;


    @Column(name = "classificacao_risco")
    private String classificacaoRisco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_cidadao_id", nullable = true)
    private Cidadao cidadao;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_by", nullable = false)
    private OffsetDateTime updatedBy;
}