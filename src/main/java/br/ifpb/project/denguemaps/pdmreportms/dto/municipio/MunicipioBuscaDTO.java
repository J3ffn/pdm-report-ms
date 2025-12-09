package br.ifpb.project.denguemaps.pdmreportms.dto.municipio;

import br.ifpb.project.denguemaps.pdmreportms.enums.Estado;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MunicipioBuscaDTO {
    private UUID id;
    private String nome;

    // O Estado é obrigatório para desambiguação
    @NotNull(message = "O Estado deve ser fornecido.")
    private Estado estado;
}
