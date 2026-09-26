package br.ifpb.project.denguemaps.pdmreportms.service;

import br.ifpb.project.denguemaps.pdmreportms.crypto.CpfCryptoService;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportSymptomsRequestDTO;
import br.ifpb.project.denguemaps.pdmreportms.exception.BusinessRuleException;
import br.ifpb.project.denguemaps.pdmreportms.model.GeoEntidade;
import br.ifpb.project.denguemaps.pdmreportms.model.ReportSymptomsEntidade;
import br.ifpb.project.denguemaps.pdmreportms.observability.ReportMetrics;
import br.ifpb.project.denguemaps.pdmreportms.repository.GeoRepository;
import br.ifpb.project.denguemaps.pdmreportms.repository.ReportSymptomsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportSymptomsService")
class ReportSymptomsServiceTest {

    @Mock GeoRepository             geoRepository;
    @Mock ReportSymptomsRepository  symptomsRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock CpfCryptoService          cpfCryptoService;
    @Mock ReportMetrics             metrics;

    @InjectMocks ReportSymptomsService service;

    private ReportSymptomsRequestDTO dtoBase;
    private GeoEntidade geoMock;

    @BeforeEach
    void setUp() {
        dtoBase = ReportSymptomsRequestDTO.builder()
                .lat(-7.23).lng(-35.88)
                .respostas(Map.of("q1", "a1"))
                .scoreTotal(70)
                .questionnaireId(UUID.randomUUID())
                .build();

        geoMock = GeoEntidade.builder()
                .id(UUID.randomUUID()).lat(-7.23).lng(-35.88)
                .build();  // H3 fields são null — preenchidos assincronamente pelo geo-worker

        ReportSymptomsEntidade saved = new ReportSymptomsEntidade();
        saved.setGeo(geoMock);

        lenient().when(geoRepository.save(any(GeoEntidade.class))).thenReturn(geoMock);
        lenient().when(symptomsRepository.save(any())).thenReturn(saved);
    }

    @Test
    @DisplayName("Usuário logado (JWT) — cria report sem CPF")
    void deveAceitarCidadaoAutenticadoSemCpf() {
        UUID cidadaoId = UUID.randomUUID();

        assertThatNoException().isThrownBy(() -> service.criar(dtoBase, cidadaoId));

        verify(cpfCryptoService, never()).encrypt(any());
        verify(metrics).incrementSymptomsCreated();
        verify(metrics).recordScore(70);
        verify(metrics).incrementDiseaseDetected();  // score 70 >= 50
    }

    @Test
    @DisplayName("Usuário anônimo com CPF — criptografa e persiste")
    void deveAceitarAnonimoComCpf() {
        dtoBase.setCpf("12345678900");
        when(cpfCryptoService.encrypt("12345678900")).thenReturn("AES_ENCRYPTED_TOKEN");

        assertThatNoException().isThrownBy(() -> service.criar(dtoBase, null));

        verify(cpfCryptoService).encrypt("12345678900");
        verify(symptomsRepository).save(argThat(e ->
                "AES_ENCRYPTED_TOKEN".equals(e.getCpfHash())
        ));
    }

    @Test
    @DisplayName("Anônimo SEM CPF — deve lançar BusinessRuleException (422)")
    void deveRejeitarAnonimoSemCpf() {
        assertThatThrownBy(() -> service.criar(dtoBase, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Identificação obrigatória");

        verify(symptomsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Score abaixo do limiar — isDisease deve ser false")
    void scoreAbaixoDoLimiarNaoDeveClassificarComoDoenca() {
        UUID cidadaoId = UUID.randomUUID();
        dtoBase.setScoreTotal(49);  // abaixo de 50

        service.criar(dtoBase, cidadaoId);

        verify(symptomsRepository).save(argThat(e -> !e.getIsDisease()));
        verify(metrics, never()).incrementDiseaseDetected();
    }

    @Test
    @DisplayName("Score no limiar exato (50) — isDisease deve ser true")
    void scoreLimiarExatoDeveClassificarComoDoenca() {
        UUID cidadaoId = UUID.randomUUID();
        dtoBase.setScoreTotal(50);

        service.criar(dtoBase, cidadaoId);

        verify(symptomsRepository).save(argThat(ReportSymptomsEntidade::getIsDisease));
        verify(metrics).incrementDiseaseDetected();
    }

    @Test
    @DisplayName("Geo é persistido apenas com lat/lng — H3 fields são nulos (preenchidos pelo geo-worker)")
    void geoDeveSerPersistidoSemCamposH3() {
        UUID cidadaoId = UUID.randomUUID();
        service.criar(dtoBase, cidadaoId);

        verify(geoRepository).save(argThat(g ->
                g.getLat().equals(-7.23) &&
                g.getLng().equals(-35.88) &&
                g.getH3Res8() == null &&
                g.getH3Res6() == null
        ));
    }
}
