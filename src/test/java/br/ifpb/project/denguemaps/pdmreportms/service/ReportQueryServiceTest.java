package br.ifpb.project.denguemaps.pdmreportms.service;

import br.ifpb.project.denguemaps.pdmreportms.dto.ReportDetailResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.exception.ResourceNotFoundException;
import br.ifpb.project.denguemaps.pdmreportms.model.GeoEntidade;
import br.ifpb.project.denguemaps.pdmreportms.model.ReportEntidade;
import br.ifpb.project.denguemaps.pdmreportms.model.ReportFocusEntidade;
import br.ifpb.project.denguemaps.pdmreportms.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportQueryServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportQueryService queryService;

    private ReportFocusEntidade report;
    private UUID reportId;
    private UUID citizenId;

    @BeforeEach
    void setUp() {
        reportId = UUID.randomUUID();
        citizenId = UUID.randomUUID();

        GeoEntidade geo = new GeoEntidade();
        geo.setId(UUID.randomUUID());
        geo.setLat(-7.115);
        geo.setLng(-34.861);
        geo.setH3Res8(123456L);
        geo.setH3Res6(654321L);

        report = new ReportFocusEntidade();
        report.setId(reportId);
        report.setFkPersonId(citizenId);
        report.setIsEnabled(true);
        report.setIsVisited(false);
        report.setIsDisease(false);
        report.setDescription("Foco em terreno baldio");
        report.setLocalDescription("Pneus acumulando água");
        report.setGeo(geo);
        report.setCreatedAt(OffsetDateTime.now());
    }

    @Test
    @DisplayName("Deve buscar report por ID quando ativo")
    void deveBuscarPorIdQuandoAtivo() {
        when(reportRepository.findByIdAndIsEnabledTrue(reportId)).thenReturn(Optional.of(report));

        ReportDetailResponseDTO dto = queryService.buscarPorId(reportId);

        assertNotNull(dto);
        assertEquals(reportId, dto.reportId());
        assertEquals("FOCUS", dto.reportType());
        assertEquals("Pneus acumulando água", dto.localDescription());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando report inativo ou inexistente")
    void deveLancarExceptionQuandoReportInativo() {
        when(reportRepository.findByIdAndIsEnabledTrue(reportId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                queryService.buscarPorId(reportId));
    }

    @Test
    @DisplayName("Deve listar todos os reports ativos de forma paginada")
    void deveListarTodosPaginado() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ReportEntidade> page = new PageImpl<>(List.of(report));
        when(reportRepository.findAllByIsEnabledTrue(pageable)).thenReturn(page);

        Page<ReportResponseDTO> resultado = queryService.listarTodos(pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(reportId, resultado.getContent().get(0).reportId());
    }

    @Test
    @DisplayName("Deve listar meus reports para cidadão autenticado")
    void deveListarMeusReports() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ReportEntidade> page = new PageImpl<>(List.of(report));
        when(reportRepository.findAllByFkPersonIdAndIsEnabledTrue(citizenId, pageable)).thenReturn(page);

        Page<ReportResponseDTO> resultado = queryService.listarMeus(citizenId, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("Deve retornar página vazia quando cidadão for anônimo (null)")
    void deveRetornarVazioParaAnonimo() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<ReportResponseDTO> resultado = queryService.listarMeus(null, pageable);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(reportRepository, never()).findAllByFkPersonIdAndIsEnabledTrue(any(), any());
    }
}
