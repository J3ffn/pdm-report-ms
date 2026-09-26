package br.ifpb.project.denguemaps.pdmreportms.service;

import br.ifpb.project.denguemaps.pdmreportms.dto.ReportDetailResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.event.ReportDisabledEvent;
import br.ifpb.project.denguemaps.pdmreportms.event.ReportVisitedEvent;
import br.ifpb.project.denguemaps.pdmreportms.exception.AccessDeniedException;
import br.ifpb.project.denguemaps.pdmreportms.exception.BusinessRuleException;
import br.ifpb.project.denguemaps.pdmreportms.exception.ResourceNotFoundException;
import br.ifpb.project.denguemaps.pdmreportms.model.GeoEntidade;
import br.ifpb.project.denguemaps.pdmreportms.model.ReportFocusEntidade;
import br.ifpb.project.denguemaps.pdmreportms.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportLifecycleServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReportLifecycleService lifecycleService;

    private ReportFocusEntidade report;
    private UUID reportId;
    private UUID ownerId;

    @BeforeEach
    void setUp() {
        reportId = UUID.randomUUID();
        ownerId = UUID.randomUUID();

        GeoEntidade geo = new GeoEntidade();
        geo.setId(UUID.randomUUID());
        geo.setLat(-7.115);
        geo.setLng(-34.861);
        geo.setH3Res8(123456L);
        geo.setH3Res6(654321L);

        report = new ReportFocusEntidade();
        report.setId(reportId);
        report.setFkPersonId(ownerId);
        report.setIsEnabled(true);
        report.setIsVisited(false);
        report.setIsDisease(false);
        report.setDescription("Foco em terreno baldio");
        report.setLocalDescription("Pneus acumulando água");
        report.setGeo(geo);
        report.setCreatedAt(OffsetDateTime.now());
    }

    @Test
    @DisplayName("Cidadão dono deve conseguir desativar seu próprio report")
    void cidadaoDonoDeveDesativarComSucesso() {
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        lifecycleService.desativar(reportId, ownerId, false);

        assertFalse(report.getIsEnabled());
        verify(reportRepository).save(report);
        verify(eventPublisher).publishEvent(any(ReportDisabledEvent.class));
    }

    @Test
    @DisplayName("Admin deve conseguir desativar report de qualquer cidadão")
    void adminDeveDesativarReportDeQualquerUm() {
        UUID adminId = UUID.randomUUID();
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        lifecycleService.desativar(reportId, adminId, true);

        assertFalse(report.getIsEnabled());
        verify(reportRepository).save(report);
        verify(eventPublisher).publishEvent(any(ReportDisabledEvent.class));
    }

    @Test
    @DisplayName("Usuário que não é dono nem admin deve receber AccessDeniedException")
    void naoDonoNemAdminDeveFalharAoDesativar() {
        UUID outroId = UUID.randomUUID();
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        assertThrows(AccessDeniedException.class, () ->
                lifecycleService.desativar(reportId, outroId, false));

        verify(reportRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Desativar report inexistente deve lançar ResourceNotFoundException")
    void desativarInexistenteDeveLancarResourceNotFoundException() {
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                lifecycleService.desativar(reportId, ownerId, false));
    }

    @Test
    @DisplayName("Desativar report já inativo deve lançar BusinessRuleException")
    void desativarJaInativoDeveLancarBusinessRuleException() {
        report.setIsEnabled(false);
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        assertThrows(BusinessRuleException.class, () ->
                lifecycleService.desativar(reportId, ownerId, false));
    }

    @Test
    @DisplayName("Agente deve conseguir marcar report como visitado com sucesso")
    void marcarVisitadoComSucesso() {
        when(reportRepository.findByIdAndIsEnabledTrue(reportId)).thenReturn(Optional.of(report));

        ReportDetailResponseDTO resultado = lifecycleService.marcarVisitado(reportId);

        assertNotNull(resultado);
        assertTrue(report.getIsVisited());
        verify(reportRepository).save(report);
        verify(eventPublisher).publishEvent(any(ReportVisitedEvent.class));
    }

    @Test
    @DisplayName("Marcar visitado em report inexistente ou inativo deve lançar ResourceNotFoundException")
    void marcarVisitadoInexistenteDeveLancarResourceNotFoundException() {
        when(reportRepository.findByIdAndIsEnabledTrue(reportId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                lifecycleService.marcarVisitado(reportId));
    }
}
