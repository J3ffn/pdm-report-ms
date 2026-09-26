package br.ifpb.project.denguemaps.pdmreportms.model;

import br.ifpb.project.denguemaps.pdmreportms.model.enums.ReportType;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_reports")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "report_type", discriminatorType = DiscriminatorType.STRING, length = 20)
@Getter
@Setter
@NoArgsConstructor
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

    /**
     * Regra de negócio: desativa o report protegendo invariantes de domínio.
     * Cidadão desativa apenas o próprio; admin desativa qualquer um.
     */
    public void desativar(UUID executorId, boolean isAdmin) {
        if (Boolean.FALSE.equals(this.isEnabled)) {
            throw new br.ifpb.project.denguemaps.pdmreportms.exception.BusinessRuleException("Report já está inativo.");
        }
        boolean ehDono = this.fkPersonId != null && this.fkPersonId.equals(executorId);
        if (!isAdmin && !ehDono) {
            throw new br.ifpb.project.denguemaps.pdmreportms.exception.AccessDeniedException("Sem permissão para desativar este report.");
        }
        this.isEnabled = false;
    }

    /**
     * Regra de negócio: marca o report como visitado por agente de saúde.
     */
    public void marcarVisitado() {
        if (Boolean.FALSE.equals(this.isEnabled)) {
            throw new br.ifpb.project.denguemaps.pdmreportms.exception.BusinessRuleException("Report não encontrado ou inativo.");
        }
        this.isVisited = true;
    }
}
