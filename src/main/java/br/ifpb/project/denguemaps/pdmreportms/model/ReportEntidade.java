package br.ifpb.project.denguemaps.pdmreportms.model;

import br.ifpb.project.denguemaps.pdmreportms.model.enums.ReportType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_reports")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "report_type", discriminatorType = DiscriminatorType.STRING, length = 20)
@Getter @Setter @NoArgsConstructor
public abstract class ReportEntidade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "report_id")
    private UUID id;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = false;

    @Column(name = "is_disease", nullable = false)
    private Boolean isDisease = false;

    @Column(name = "is_visited", nullable = false)
    private Boolean isVisited = false;

    @Column(name = "fk_person_id")
    private UUID fkPersonId;

    @Column(name = "cpf_hash")
    private String cpfHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_geo_id", nullable = false)
    private GeoEntidade geo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    /**
     * Retorna o tipo do report. Cada subclasse define o seu valor,
     * alinhado com o @DiscriminatorValue declarado na anotação JPA.
     */
    public abstract ReportType getReportType();
}
