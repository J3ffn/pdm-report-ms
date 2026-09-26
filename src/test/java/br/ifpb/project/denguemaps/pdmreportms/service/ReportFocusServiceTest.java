package br.ifpb.project.denguemaps.pdmreportms.service;

import br.ifpb.project.denguemaps.pdmreportms.crypto.CpfCryptoService;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportFocusRequestDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.exception.BusinessRuleException;
import br.ifpb.project.denguemaps.pdmreportms.model.GeoEntidade;
import br.ifpb.project.denguemaps.pdmreportms.model.ReportFocusEntidade;
import br.ifpb.project.denguemaps.pdmreportms.observability.ReportMetrics;
import br.ifpb.project.denguemaps.pdmreportms.repository.GeoRepository;
import br.ifpb.project.denguemaps.pdmreportms.repository.ReportFocusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportFocusService")
class ReportFocusServiceTest {

    @Mock GeoRepository             geoRepository;
    @Mock ReportFocusRepository     focusRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock CpfCryptoService          cpfCryptoService;
    @Mock ReportMetrics             metrics;

    @InjectMocks ReportFocusService service;

    private ReportFocusRequestDTO dtoBase;
    private GeoEntidade geoMock;

    @BeforeEach
    void setUp() {
        dtoBase = ReportFocusRequestDTO.builder()
                .lat(-7.23).lng(-35.88)
                .localDescription("Caixa d'água descoberta com larvas")
                .build();

        geoMock = GeoEntidade.builder()
                .id(UUID.randomUUID()).lat(-7.23).lng(-35.88)
                .build();  // H3 fields são null — preenchidos assincronamente pelo geo-worker

        ReportFocusEntidade saved = new ReportFocusEntidade();
        saved.setGeo(geoMock);

        lenient().when(geoRepository.save(any(GeoEntidade.class))).thenReturn(geoMock);
        lenient().when(focusRepository.save(any())).thenReturn(saved);
    }

    @Test
    @DisplayName("Usuário logado — cria foco e retorna 201 com DTO")
    void deveCriarFocoParaCidadaoAutenticado() {
        UUID cidadaoId = UUID.randomUUID();

        ReportResponseDTO result = service.criar(dtoBase, cidadaoId);

        assertThat(result).isNotNull();
        verify(cpfCryptoService, never()).encrypt(any());
        verify(metrics).incrementFocusCreated();
    }

    @Test
    @DisplayName("Usuário anônimo com CPF — criptografa antes de persistir")
    void deveCriptografarCpfDeAnonimoAntesDePeristir() {
        dtoBase.setCpf("98765432100");
        when(cpfCryptoService.encrypt("98765432100")).thenReturn("ENCRYPTED_CPF_TOKEN");

        service.criar(dtoBase, null);

        verify(cpfCryptoService).encrypt("98765432100");
        verify(focusRepository).save(argThat(e ->
                "ENCRYPTED_CPF_TOKEN".equals(e.getCpfHash())
        ));
    }

    @Test
    @DisplayName("Anônimo SEM CPF — deve lançar BusinessRuleException (422)")
    void deveRejeitarAnonimoSemCpf() {
        assertThatThrownBy(() -> service.criar(dtoBase, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Identificação obrigatória");

        verify(focusRepository, never()).save(any());
    }

    @Test
    @DisplayName("description da entidade pai deve ser sempre o valor fixo")
    void descriptionDaEntidadePaiDeveSerValorFixo() {
        UUID cidadaoId = UUID.randomUUID();
        service.criar(dtoBase, cidadaoId);

        verify(focusRepository).save(argThat(e ->
                "Foco de dengue reportado".equals(e.getDescription())
        ));
    }

    @Test
    @DisplayName("isDisease deve ser sempre false para reports de foco")
    void focoNuncaDeveSerClassificadoComoDoenca() {
        UUID cidadaoId = UUID.randomUUID();
        service.criar(dtoBase, cidadaoId);

        verify(focusRepository).save(argThat(e -> !e.getIsDisease()));
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
