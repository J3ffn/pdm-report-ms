package br.ifpb.project.denguemaps.pdmreportms.model;

import br.ifpb.project.denguemaps.pdmreportms.model.enums.ReportType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_report_focus")
@DiscriminatorValue("FOCUS")
@Getter @Setter @NoArgsConstructor
public class ReportFocusEntidade extends ReportEntidade {

    @Column(name = "local_description", nullable = false, columnDefinition = "TEXT")
    private String localDescription;

    @Override
    public ReportType getReportType() {
        return ReportType.FOCUS;
    }
}
