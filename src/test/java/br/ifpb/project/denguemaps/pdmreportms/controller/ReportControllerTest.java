package br.ifpb.project.denguemaps.pdmreportms.controller;

import br.ifpb.project.denguemaps.pdmreportms.dto.ReportDetailResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportFocusRequestDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportSymptomsRequestDTO;
import br.ifpb.project.denguemaps.pdmreportms.exception.ResourceNotFoundException;
import br.ifpb.project.denguemaps.pdmreportms.service.ReportLifecycleService;
import br.ifpb.project.denguemaps.pdmreportms.service.ReportQueryService;
import br.ifpb.project.denguemaps.pdmreportms.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private br.ifpb.project.denguemaps.pdmreportms.service.ReportFocusService focusService;

    @MockBean
    private br.ifpb.project.denguemaps.pdmreportms.service.ReportSymptomsService symptomsService;

    @MockBean
    private ReportQueryService queryService;

    @MockBean
    private ReportLifecycleService lifecycleService;

    @Test
    @DisplayName("POST /api/reports/focus com payload válido deve retornar 201 Created")
    void deveRegistrarFocoComSucesso() throws Exception {
        UUID reportId = UUID.randomUUID();
        ReportFocusRequestDTO request = ReportFocusRequestDTO.builder()
                .lat(-7.115)
                .lng(-34.861)
                .localDescription("Terreno baldio")
                .build();

        ReportResponseDTO response = new ReportResponseDTO(
                reportId, "FOCUS", -7.115, -34.861, 888L, 666L, true, false, false, OffsetDateTime.now()
        );

        when(focusService.criar(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/reports/focus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportId").value(reportId.toString()))
                .andExpect(jsonPath("$.reportType").value("FOCUS"));
    }

    @Test
    @DisplayName("POST /api/reports/symptoms com payload válido deve retornar 202 Accepted sem corpo")
    void deveRegistrarSintomasComSucesso() throws Exception {
        ReportSymptomsRequestDTO request = ReportSymptomsRequestDTO.builder()
                .lat(-7.115)
                .lng(-34.861)
                .scoreTotal(60)
                .questionnaireId(UUID.randomUUID())
                .respostas(Map.of("q1", "opt1"))
                .build();

        doNothing().when(symptomsService).criar(any(), any());

        mockMvc.perform(post("/api/reports/symptoms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("GET /api/reports/{id} deve retornar 200 OK quando encontrado")
    void deveBuscarPorIdComSucesso() throws Exception {
        UUID reportId = UUID.randomUUID();
        ReportDetailResponseDTO detail = new ReportDetailResponseDTO(
                reportId, "FOCUS", -7.115, -34.861, 888L, 666L,
                true, false, false, null, OffsetDateTime.now(),
                "Terreno", null, null, null
        );

        when(queryService.buscarPorId(reportId)).thenReturn(detail);

        mockMvc.perform(get("/api/reports/{id}", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value(reportId.toString()))
                .andExpect(jsonPath("$.localDescription").value("Terreno"));
    }

    @Test
    @DisplayName("GET /api/reports/{id} deve retornar 404 quando não encontrado")
    void deveRetornar404QuandoNaoEncontrado() throws Exception {
        UUID reportId = UUID.randomUUID();
        when(queryService.buscarPorId(reportId)).thenThrow(new ResourceNotFoundException("Não encontrado"));

        mockMvc.perform(get("/api/reports/{id}", reportId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /api/reports/{id} deve retornar 204 No Content")
    void deveDesativarReportComSucesso() throws Exception {
        UUID reportId = UUID.randomUUID();
        doNothing().when(lifecycleService).desativar(eq(reportId), any(), anyBoolean());

        mockMvc.perform(delete("/api/reports/{id}", reportId))
                .andExpect(status().isNoContent());

        verify(lifecycleService).desativar(eq(reportId), any(), anyBoolean());
    }

    @Test
    @DisplayName("PATCH /api/reports/{id}/visit deve retornar 200 OK")
    void deveMarcarVisitadoComSucesso() throws Exception {
        UUID reportId = UUID.randomUUID();
        ReportDetailResponseDTO detail = new ReportDetailResponseDTO(
                reportId, "FOCUS", -7.115, -34.861, 888L, 666L,
                true, false, true, null, OffsetDateTime.now(),
                "Terreno", null, null, null
        );

        when(lifecycleService.marcarVisitado(reportId)).thenReturn(detail);

        mockMvc.perform(patch("/api/reports/{id}/visit", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isVisited").value(true));
    }
}
