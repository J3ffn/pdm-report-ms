package br.ifpb.project.denguemaps.pdmreportms.controller;

import br.ifpb.project.denguemaps.pdmreportms.dto.ReportDetailResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportFocusRequestDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportSymptomsRequestDTO;
import br.ifpb.project.denguemaps.pdmreportms.security.SecurityUtils;
import br.ifpb.project.denguemaps.pdmreportms.service.ReportFocusService;
import br.ifpb.project.denguemaps.pdmreportms.service.ReportLifecycleService;
import br.ifpb.project.denguemaps.pdmreportms.service.ReportQueryService;
import br.ifpb.project.denguemaps.pdmreportms.service.ReportSymptomsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Endpoints para registro, consulta, auditoria e desativação de reports de dengue")
public class ReportController {

    private final ReportFocusService    focusService;
    private final ReportSymptomsService symptomsService;
    private final ReportQueryService    queryService;
    private final ReportLifecycleService lifecycleService;

    // -------------------------------------------------------------------------
    // ESCRITA — Endpoints públicos (acesso sem autenticação obrigatória)
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Registrar questionário de sintomas",
            description = """
                    Aceita o questionário de sintomas preenchido pelo cidadão.
                    
                    - **Usuário logado**: envie apenas lat/lng/respostas/scoreTotal/questionnaireId (sem CPF)
                    - **Usuário anônimo**: inclua também o campo `cpf` (11 dígitos, sem pontos/traços)
                    
                    O CPF bruto **nunca** é persistido — o servidor aplica AES-256-GCM antes de salvar.
                    
                    O frontend já calculou e exibiu o resultado ao usuário, portanto **este endpoint
                    não retorna dados** (202 Accepted, sem body).
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Report aceito e em processamento"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (campos obrigatórios ausentes ou formato errado)"),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada (ex: sem JWT e sem CPF)")
    })
    @PostMapping("/symptoms")
    public ResponseEntity<Void> reportarSintomas(
            @Valid @RequestBody ReportSymptomsRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt) {

        UUID cidadaoId = SecurityUtils.extractUserId(jwt).orElse(null);
        symptomsService.criar(dto, cidadaoId);
        return ResponseEntity.accepted().build();  // 202 Accepted, sem body
    }

    @Operation(
            summary = "Registrar foco de dengue",
            description = """
                    Reporta um local com água acumulada ou potencial de proliferação do mosquito.
                    
                    - **Usuário logado**: envie lat/lng/localDescription (sem CPF)
                    - **Usuário anônimo**: inclua também o campo `cpf` (11 dígitos, sem pontos/traços)
                    
                    Retorna `201 Created` com os dados do report (incluindo índices H3) para que
                    o frontend confirme o ponto marcado no mapa.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Foco registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada (ex: sem JWT e sem CPF)")
    })
    @PostMapping("/focus")
    public ResponseEntity<ReportResponseDTO> reportarFoco(
            @Valid @RequestBody ReportFocusRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt) {

        UUID cidadaoId = SecurityUtils.extractUserId(jwt).orElse(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(focusService.criar(dto, cidadaoId));
    }

    // -------------------------------------------------------------------------
    // LEITURA — Endpoints autenticados
    // -------------------------------------------------------------------------

    @Operation(summary = "Buscar detalhes de um report", description = "Retorna detalhes completos do report ativo. Acesso autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalhes do report retornados"),
            @ApiResponse(responseCode = "404", description = "Report não encontrado ou inativo")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReportDetailResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(queryService.buscarPorId(id));
    }

    @Operation(summary = "Listar meus reports", description = "Retorna histórico paginado de reports do cidadão autenticado.")
    @GetMapping("/my")
    public ResponseEntity<Page<ReportResponseDTO>> meuReports(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        UUID cidadaoId = SecurityUtils.extractUserId(jwt).orElse(null);
        return ResponseEntity.ok(queryService.listarMeus(cidadaoId, pageable));
    }

    @Operation(summary = "Listar todos os reports ativos", description = "Lista paginada de reports ativos. Restrito a administradores e agentes de saúde.")
    @GetMapping
    public ResponseEntity<Page<ReportResponseDTO>> listarTodos(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return ResponseEntity.ok(queryService.listarTodos(pageable));
    }

    // -------------------------------------------------------------------------
    // CICLO DE VIDA — Admin / Agentes de Saúde
    // -------------------------------------------------------------------------

    @Operation(summary = "Desativar report (soft delete)", description = "Desativa um report ativo. Cidadão só desativa o seu próprio; admin desativa qualquer um.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Report desativado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para desativar"),
            @ApiResponse(responseCode = "404", description = "Report não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication) {

        UUID cidadaoId = SecurityUtils.extractUserId(jwt).orElse(null);
        boolean isAdmin = SecurityUtils.isAdmin(authentication);
        lifecycleService.desativar(id, cidadaoId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Marcar report como visitado", description = "Marca o report como visitado por agente de saúde. Restrito a agentes e admin.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report marcado como visitado"),
            @ApiResponse(responseCode = "404", description = "Report não encontrado ou inativo")
    })
    @PatchMapping("/{id}/visit")
    public ResponseEntity<ReportDetailResponseDTO> marcarVisitado(@PathVariable UUID id) {
        return ResponseEntity.ok(lifecycleService.marcarVisitado(id));
    }
}
