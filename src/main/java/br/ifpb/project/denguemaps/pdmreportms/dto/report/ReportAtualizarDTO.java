package br.ifpb.project.denguemaps.pdmreportms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportAtualizarDTO extends ReportCriacaoDTO{

    private UUID id;
}
