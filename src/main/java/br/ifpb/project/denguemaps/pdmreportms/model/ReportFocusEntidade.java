package br.ifpb.project.denguemaps.pdmreportms.model;

import br.ifpb.project.denguemaps.pdmreportms.model.enums.ReportType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "tb_report_focus")
@DiscriminatorValue("FOCUS")
@Getter
@Setter
@NoArgsConstructor
public class ReportFocusEntidade extends ReportEntidade {

    @Column(name = "local_description", nullable = false, columnDefinition = "TEXT")
    private String localDescription;

    @Override
    public ReportType getReportType() {
        return ReportType.FOCUS;
    }
}
