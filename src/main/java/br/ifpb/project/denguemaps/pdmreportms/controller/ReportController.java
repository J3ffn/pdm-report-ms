package br.ifpb.project.denguemaps.pdmreportms.controller;




import br.ifpb.project.denguemaps.pdmreportms.dto.ReportDetailResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportFocusRequestDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportSymptomsRequestDTO;
import br.ifpb.project.denguemaps.pdmreportms.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import br.ifpb.project.denguemaps.pdmreportms.service.ReportQueryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService<ReportFocusRequestDTO> focusService;
    private final ReportService<ReportSymptomsRequestDTO> symptomsService;
    private final ReportQueryService queryService;


    @PostMapping("/focus")
    public ResponseEntity<ReportResponseDTO> reportarFoco(
            @Valid @RequestBody ReportFocusRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt) {

        UUID cidadaoId = extrairId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(focusService.criar(dto, cidadaoId));
    }

    @PostMapping("/symptoms")
    public ResponseEntity<ReportResponseDTO> reportarSintomas(
            @Valid @RequestBody ReportSymptomsRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt) {

        UUID cidadaoId = extrairId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(symptomsService.criar(dto, cidadaoId));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ReportDetailResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(queryService.buscarPorId(id));
    }

    /**
     * Reports do cidadão autenticado (histórico pessoal).
     * Acesso: apenas o próprio cidadão via JWT.
     */
    @GetMapping("/my")
    public ResponseEntity<Page<ReportResponseDTO>> meuReports(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        UUID cidadaoId = extrairId(jwt);
        return ResponseEntity.ok(queryService.listarMeus(cidadaoId, pageable));
    }

    /**
     * Lista todos os reports ativos (paginada).
     * Acesso: restrito a ROLE_admin e ROLE_health_agent (configurado no SecurityConfig).
     */
    @GetMapping
    public ResponseEntity<Page<ReportResponseDTO>> listarTodos(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return ResponseEntity.ok(queryService.listarTodos(pageable));
    }


    /**
     * Desativa um report (soft delete).
     * Cidadão desativa apenas o próprio; admin desativa qualquer um.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication) {

        UUID cidadaoId = extrairId(jwt);
        boolean isAdmin = isAdmin(authentication);
        queryService.desativar(id, cidadaoId, isAdmin);
        return ResponseEntity.noContent().build();
    }


    /**
     * Marca o report como visitado por um agente de saúde.
     * Acesso: restrito a ROLE_health_agent e ROLE_admin (configurado no SecurityConfig).
     */
    @PatchMapping("/{id}/visit")
    public ResponseEntity<ReportDetailResponseDTO> marcarVisitado(@PathVariable UUID id) {
        return ResponseEntity.ok(queryService.marcarVisitado(id));
    }

    private UUID extrairId(Jwt jwt) {
        return jwt != null ? UUID.fromString(jwt.getSubject()) : null;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_admin"));
    }
}
