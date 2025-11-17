package br.ifpb.project.denguemaps.pdmreportms.controller;


import br.ifpb.project.denguemaps.pdmreportms.dto.report.ReportCriacaoDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.report.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.report.ReportAtualizarDTO;
import br.ifpb.project.denguemaps.pdmreportms.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponseDTO> registrarReport(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ReportCriacaoDTO request
    ) {
        ReportResponseDTO reportResponseDTO = reportService.cadastrarReport(request);
        return new ResponseEntity<>(reportResponseDTO, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<ReportResponseDTO> atualizarReport(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ReportAtualizarDTO request
    ) {
        ReportResponseDTO reportResponseDTO = reportService.atualizarReport(request);
        return new ResponseEntity<>(reportResponseDTO, HttpStatus.OK);
    }


    @DeleteMapping
    public ResponseEntity<String> deletarReport(@AuthenticationPrincipal Jwt jwt,
                                                @RequestParam UUID uuid
    ){
        reportService.deletarReportEspecifico(uuid);
        return new ResponseEntity<String>("", HttpStatus.NO_CONTENT);
    }

    @GetMapping("/esp")
    public ResponseEntity<List<ReportResponseDTO>> buscarReportEspecifico(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam UUID uuid
            ){
        List<ReportResponseDTO> listaReport = reportService.buscarReportEspecifico(uuid);
        return new ResponseEntity<>(listaReport, HttpStatus.OK);
    }

    @GetMapping("/esp-cidadao")
    public ResponseEntity<List<ReportResponseDTO>> buscarReportCidadaoEspecifico(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam UUID uuid
    ){
        List<ReportResponseDTO> listaCidadaoReport = reportService.buscarReportCidadaoEspecifico(uuid);
        return new ResponseEntity<>(listaCidadaoReport, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReportResponseDTO>> listarTodoReport(
            @AuthenticationPrincipal Jwt jwt
    ){
        List<ReportResponseDTO> listaReport = reportService.buscarTodoReport();
        return new ResponseEntity<>(listaReport, HttpStatus.OK);
    }


}
