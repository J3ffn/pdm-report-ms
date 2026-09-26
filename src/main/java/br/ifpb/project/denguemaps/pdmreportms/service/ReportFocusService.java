package br.ifpb.project.denguemaps.pdmreportms.service;

import br.ifpb.project.denguemaps.pdmreportms.crypto.CpfCryptoService;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportFocusRequestDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.event.ReportCreatedEvent;
import br.ifpb.project.denguemaps.pdmreportms.exception.BusinessRuleException;
import br.ifpb.project.denguemaps.pdmreportms.mapper.ReportMapper;
import br.ifpb.project.denguemaps.pdmreportms.model.GeoEntidade;
import br.ifpb.project.denguemaps.pdmreportms.model.ReportFocusEntidade;
import br.ifpb.project.denguemaps.pdmreportms.observability.ReportMetrics;
import br.ifpb.project.denguemaps.pdmreportms.repository.GeoRepository;
import br.ifpb.project.denguemaps.pdmreportms.repository.ReportFocusRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Processa o reporte de um foco de dengue (local com água acumulada / proliferação potencial).
 *
 * <p>Fluxo:
 * <ol>
 *   <li>Valida identidade: JWT (cidadão logado) OU CPF (anônimo) — obrigatório um dos dois</li>
 *   <li>Criptografa CPF com AES-256-GCM (apenas para anônimos)</li>
 *   <li>Persiste lat/lng em tb_geo e o report em tb_reports + tb_report_focus</li>
 *   <li>Publica evento pós-commit no RabbitMQ → o pdm-geo-worker calcula e persiste os índices H3</li>
 * </ol>
 *
 * <p>O cálculo de H3 (res8/res6) não é mais responsabilidade deste microserviço.
 * O pdm-geo-worker consome o evento {@code report.focus.created} e preenche
 * os índices H3 na tabela tb_geo de forma assíncrona.
 */
@Service
@RequiredArgsConstructor
public class ReportFocusService implements ReportService<ReportFocusRequestDTO, ReportResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(ReportFocusService.class);

    private final GeoRepository             geoRepository;
    private final ReportFocusRepository     focusRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CpfCryptoService          cpfCryptoService;
    private final ReportMetrics             metrics;

    @Override
    @Transactional
    public ReportResponseDTO criar(ReportFocusRequestDTO dto, UUID cidadaoId) {

        log.info("[Focus] Recebendo report. cidadaoId={}, lat={}, lng={}",
                cidadaoId, dto.getLat(), dto.getLng());

        // Regra de negócio: ao menos uma forma de identificação é obrigatória
        if (cidadaoId == null && (dto.getCpf() == null || dto.getCpf().isBlank())) {
            throw new BusinessRuleException(
                    "Identificação obrigatória: faça login ou informe o CPF para reportar um foco."
            );
        }

        // Criptografa CPF apenas para cidadãos anônimos
        String cpfCriptografado = null;
        if (cidadaoId == null) {
            cpfCriptografado = cpfCryptoService.encrypt(dto.getCpf());
            log.debug("[Focus] CPF de cidadão anônimo criptografado com AES-256-GCM.");
        }

        // Persiste apenas lat/lng — índices H3 serão calculados pelo pdm-geo-worker via evento
        GeoEntidade geo = GeoEntidade.builder()
                .lat(dto.getLat())
                .lng(dto.getLng())
                .build();
        geo = geoRepository.save(geo);

        ReportFocusEntidade foco = new ReportFocusEntidade();
        foco.setGeo(geo);
        foco.setDescription("Foco de dengue reportado");   // valor fixo — campo semântico do pai
        foco.setLocalDescription(dto.getLocalDescription());
        foco.setIsEnabled(true);
        foco.setIsDisease(false);
        foco.setIsVisited(false);
        foco.setFkPersonId(cidadaoId);
        foco.setCpfHash(cpfCriptografado);

        ReportFocusEntidade salvo = focusRepository.save(foco);

        // Métricas Prometheus
        metrics.incrementFocusCreated();

        // Evento publicado APÓS commit (via @TransactionalEventListener) → geo-worker processa H3
        ReportResponseDTO resposta = ReportMapper.toResponseDTO(salvo);
        eventPublisher.publishEvent(new ReportCreatedEvent("report.focus.created", resposta));

        log.info("[Focus] Report criado. reportId={}", salvo.getId());
        return resposta;
    }
}
