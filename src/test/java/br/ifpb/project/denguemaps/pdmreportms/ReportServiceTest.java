package br.ifpb.project.denguemaps.pdmreportms;

import br.ifpb.project.denguemaps.pdmreportms.dto.report.ReportAtualizarDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.report.ReportCriacaoDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.report.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.entity.Cidadao;
import br.ifpb.project.denguemaps.pdmreportms.entity.Report;
import br.ifpb.project.denguemaps.pdmreportms.repository.CidadaoRepository;
import br.ifpb.project.denguemaps.pdmreportms.repository.ReportRepository;
import br.ifpb.project.denguemaps.pdmreportms.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;
    @Mock
    private CidadaoRepository cidadaoRepository;

    @InjectMocks
    private ReportService reportService;

    // Dados de teste
    private final UUID CIDADAO_ID = UUID.randomUUID();
    private final UUID REPORT_ID = UUID.randomUUID();
    private Cidadao mockCidadao;
    private Report mockReport;
    private ReportCriacaoDTO mockCriacaoDTO;
    private ReportAtualizarDTO mockAtualizarDTO;

    @BeforeEach
    void setup() {
        // Inicialização dos mocks de Entidades
        mockCidadao = new Cidadao();
        mockCidadao.setId(CIDADAO_ID);
        mockCidadao.setNome("Carlos Teste");
        mockCidadao.setCpf("123.456.789-00");

        mockReport = new Report(
                REPORT_ID,
                "{\"lat\": 1.0, \"lon\": 2.0}",
                "ALTO",
                mockCidadao,
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().minusDays(1)
        );

        // Inicialização dos DTOs de teste usando .set()
        mockCriacaoDTO = new ReportCriacaoDTO();
        mockCriacaoDTO.setCoordenadas("{\"lat\": 3.0, \"lon\": 4.0}");
        mockCriacaoDTO.setClassificacaoRisco("BAIXO");
        mockCriacaoDTO.setFkCidadaoID(CIDADAO_ID);

        // Inicialização de ReportAtualizarDTO usando .set()
        mockAtualizarDTO = new ReportAtualizarDTO();
        mockAtualizarDTO.setId(REPORT_ID);
        mockAtualizarDTO.setCoordenadas("{\"lat\": 5.0, \"lon\": 6.0}");
        mockAtualizarDTO.setClassificacaoRisco("MEDIO");
        mockAtualizarDTO.setFkCidadaoID(CIDADAO_ID);


        // Configurações Padrão de Repositório
        when(cidadaoRepository.findById(CIDADAO_ID)).thenReturn(Optional.of(mockCidadao));
    }

    // --- Testes para cadastrarReport ---

    @Test
    void cadastrarReport_shouldSaveAndReturnResponseDTO_onSuccess() {
        // Arrange
        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);

        when(reportRepository.save(reportCaptor.capture())).thenAnswer(invocation -> {
            Report capturedReport = invocation.getArgument(0);
            capturedReport.setId(REPORT_ID);
            return capturedReport;
        });

        // Act
        ReportResponseDTO result = reportService.cadastrarReport(mockCriacaoDTO);

        // Assert
        Report capturedReport = reportCaptor.getValue();

        verify(reportRepository, times(1)).save(any(Report.class));

        assertEquals(mockCriacaoDTO.getCoordenadas(), capturedReport.getCoordenadas());
        assertEquals(mockCidadao.getNome(), result.getNomeCidadao());
    }

    @Test
    void cadastrarReport_shouldThrowIllegalArgumentException_whenCidadaoNotFound() {
        // Arrange
        UUID nonExistingCidadaoId = UUID.randomUUID();
        ReportCriacaoDTO dto = new ReportCriacaoDTO();
        dto.setCoordenadas("{}");
        dto.setClassificacaoRisco("ALTO");
        dto.setFkCidadaoID(nonExistingCidadaoId); // ID que não existe

        when(cidadaoRepository.findById(nonExistingCidadaoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> reportService.cadastrarReport(dto),
                "Deve lançar IllegalArgumentException quando o Cidadao não for encontrado.");

        verify(reportRepository, never()).save(any(Report.class));
    }

    // --- Testes para atualizarReport ---

    @Test
    void atualizarReport_shouldUpdateAndReturnResponseDTO_onSuccess() {
        // Arrange
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.of(mockReport));
        when(reportRepository.save(any(Report.class))).thenReturn(mockReport);
        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);

        // Act
        ReportResponseDTO result = reportService.atualizarReport(mockAtualizarDTO);

        // Assert
        verify(reportRepository, times(1)).save(reportCaptor.capture());

        Report capturedReport = reportCaptor.getValue();

        // Verifica se os campos foram atualizados
        assertEquals(mockAtualizarDTO.getCoordenadas(), capturedReport.getCoordenadas());
        assertEquals(mockAtualizarDTO.getClassificacaoRisco(), capturedReport.getClassificacaoRisco());
        assertNotEquals(mockReport.getCreatedAt(), capturedReport.getUpdatedBy(), "UpdatedBy deve ser atualizado.");

        // Verifica o DTO de resposta
        assertEquals(mockAtualizarDTO.getCoordenadas(), result.getCoordenadas());
    }

    @Test
    void atualizarReport_shouldThrowIllegalArgumentException_whenReportNotFound() {
        // Arrange
        UUID nonExistingReportId = UUID.randomUUID();
        ReportAtualizarDTO dto = new ReportAtualizarDTO();
        dto.setId(nonExistingReportId); // ID do Report que não existe
        dto.setCoordenadas("{}");
        dto.setClassificacaoRisco("ALTO");
        dto.setFkCidadaoID(CIDADAO_ID);

        when(reportRepository.findById(nonExistingReportId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> reportService.atualizarReport(dto),
                "Deve lançar IllegalArgumentException quando o Report não for encontrado.");

        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void atualizarReport_shouldThrowIllegalArgumentException_whenCidadaoNotFound() {
        // Arrange
        UUID nonExistingCidadaoId = UUID.randomUUID();
        ReportAtualizarDTO dto = new ReportAtualizarDTO();
        dto.setId(REPORT_ID); // ID do Report que existe
        dto.setCoordenadas("{}");
        dto.setClassificacaoRisco("ALTO");
        dto.setFkCidadaoID(nonExistingCidadaoId); // ID do Cidadao que não existe

        // Simula o Report encontrado (passo 1)
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.of(mockReport));

        // Simula a falha na busca do Cidadao (passo 2)
        when(cidadaoRepository.findById(nonExistingCidadaoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> reportService.atualizarReport(dto),
                "Deve lançar IllegalArgumentException quando o Cidadao (para atualização) não for encontrado.");

        verify(reportRepository, never()).save(any(Report.class));
    }


    // --- Testes para Métodos de Busca ---

    @Test
    void buscarTodoReport_shouldReturnAllReports() {
        // Arrange
        Report mockReport2 = new Report(UUID.randomUUID(), "{\"lat\": 7.0, \"lon\": 8.0}", "MEDIO", mockCidadao, OffsetDateTime.now(), OffsetDateTime.now());
        List<Report> mockReports = List.of(mockReport, mockReport2);

        when(reportRepository.findAll()).thenReturn(mockReports);

        // Act
        List<ReportResponseDTO> results = reportService.buscarTodoReport();

        // Assert
        assertEquals(2, results.size());
        assertEquals(mockReport.getId(), results.get(0).getId());
        verify(reportRepository, times(1)).findAll();
    }

    @Test
    void buscarReportEspecifico_shouldReturnSingleReport() {
        // Arrange
        when(reportRepository.findAllById(Collections.singleton(REPORT_ID)))
                .thenReturn(List.of(mockReport));

        // Act
        List<ReportResponseDTO> results = reportService.buscarReportEspecifico(REPORT_ID);

        // Assert
        assertEquals(1, results.size());
        assertEquals(REPORT_ID, results.get(0).getId());
        verify(reportRepository, times(1)).findAllById(Collections.singleton(REPORT_ID));
    }

    @Test
    void buscarReportCidadaoEspecifico_shouldReturnReportsByCidadaoId() {
        // Arrange
        List<Report> mockReports = List.of(mockReport);

        when(reportRepository.findByCidadao_Id(CIDADAO_ID)).thenReturn(mockReports);

        // Act
        List<ReportResponseDTO> results = reportService.buscarReportCidadaoEspecifico(CIDADAO_ID);

        // Assert
        assertEquals(1, results.size());
        assertEquals(mockCidadao.getNome(), results.get(0).getNomeCidadao());
        verify(reportRepository, times(1)).findByCidadao_Id(CIDADAO_ID);
    }

    // --- Testes para Métodos de Deleção ---

    @Test
    void deletarReportEspecifico_shouldCallDeleteById() {
        // Arrange
        UUID reportToDeleteId = UUID.randomUUID();

        // Act
        reportService.deletarReportEspecifico(reportToDeleteId);

        // Assert
        verify(reportRepository, times(1)).deleteById(reportToDeleteId);
    }

    @Test
    void deletarTodoReportCidadao_shouldCallDeleteAllByCidadaoId() {
        // Arrange
        UUID cidadaoIdToDeleteReports = UUID.randomUUID();

        // Act
        reportService.deletarTodoReportCidadao(cidadaoIdToDeleteReports);

        // Assert
        verify(reportRepository, times(1)).deleteAllByCidadaoId(cidadaoIdToDeleteReports);
    }
}