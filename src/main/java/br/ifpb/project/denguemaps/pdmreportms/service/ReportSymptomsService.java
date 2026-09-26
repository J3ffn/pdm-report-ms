package br.ifpb.project.denguemaps.pdmreportms.service;

import br.ifpb.project.denguemaps.pdmreportms.crypto.CpfCryptoService;
import br.ifpb.project.denguemaps.pdmreportms.dto.ReportSymptomsRequestDTO;
import br.ifpb.project.denguemaps.pdmreportms.event.ReportCreatedEvent;
import br.ifpb.project.denguemaps.pdmreportms.exception.BusinessRuleException;
import br.ifpb.project.denguemaps.pdmreportms.mapper.ReportMapper;
import br.ifpb.project.denguemaps.pdmreportms.model.GeoEntidade;
import br.ifpb.project.denguemaps.pdmreportms.model.ReportSymptomsEntidade;
import br.ifpb.project.denguemaps.pdmreportms.observability.ReportMetrics;
import br.ifpb.project.denguemaps.pdmreportms.repository.GeoRepository;
import br.ifpb.project.denguemaps.pdmreportms.repository.ReportSymptomsRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Processa o envio do questionário de sintomas de dengue.
 *
 * <p>Fluxo:
 * <ol>
 *   <li>Valida identidade: JWT (cidadão logado) OU CPF (anônimo) — obrigatório um dos dois</li>
 *   <li>Criptografa CPF com AES-256-GCM (apenas para anônimos)</li>
 *   <li>Calcula isDisease com base no scoreTotal enviado pelo frontend</li>
 *   <li>Persiste lat/lng em tb_geo e o report em tb_reports + tb_report_symptoms</li>
 *   <li>Publica evento pós-commit no RabbitMQ → o pdm-geo-worker calcula e persiste os índices H3</li>
 * </ol>
 *
 * <p>Retorno HTTP: 202 Accepted sem body — o frontend já calculou e exibiu o resultado ao usuário.
 *
 * <p>O cálculo de H3 (res8/res6) não é mais responsabilidade deste microserviço.
 * O pdm-geo-worker consome o evento {@code report.symptom.created} e preenche
 * os índices H3 na tabela tb_geo de forma assíncrona.
 */
@Service
@RequiredArgsConstructor
public class ReportSymptomsService implements ReportService<ReportSymptomsRequestDTO, Void> {

    private static final Logger log = LoggerFactory.getLogger(ReportSymptomsService.class);

    /** Limiar de score para classificar o cidadão como provável caso de dengue. */
    private static final int LIMIAR_DOENCA = 50;

    private final GeoRepository             geoRepository;
    private final ReportSymptomsRepository  symptomsRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CpfCryptoService          cpfCryptoService;
    private final ReportMetrics             metrics;

    @Override
    @Transactional
    public Void criar(ReportSymptomsRequestDTO dto, UUID cidadaoId) {

        log.info("[Symptoms] Recebendo report. cidadaoId={}, score={}, templateId={}",
                cidadaoId, dto.getScoreTotal(), dto.getQuestionnaireId());

        // Regra de negócio: ao menos uma forma de identificação é obrigatória
        if (cidadaoId == null && (dto.getCpf() == null || dto.getCpf().isBlank())) {
            throw new BusinessRuleException(
                    "Identificação obrigatória: faça login ou informe o CPF para enviar o questionário."
            );
        }

        // Criptografa CPF apenas para cidadãos anônimos
        String cpfCriptografado = null;
        if (cidadaoId == null) {
            cpfCriptografado = cpfCryptoService.encrypt(dto.getCpf());
            log.debug("[Symptoms] CPF de cidadão anônimo criptografado com AES-256-GCM.");
        }

        boolean isDisease = dto.getScoreTotal() >= LIMIAR_DOENCA;

        // Persiste apenas lat/lng — índices H3 serão calculados pelo pdm-geo-worker via evento
        GeoEntidade geo = GeoEntidade.builder()
                .lat(dto.getLat())
                .lng(dto.getLng())
                .build();
        geo = geoRepository.save(geo);

        ReportSymptomsEntidade sintomas = new ReportSymptomsEntidade();
        sintomas.setGeo(geo);
        sintomas.setDescription("Questionário de sintomas respondido");
        sintomas.setIsEnabled(true);
        sintomas.setIsDisease(isDisease);
        sintomas.setIsVisited(false);
        sintomas.setFkPersonId(cidadaoId);
        sintomas.setCpfHash(cpfCriptografado);
        sintomas.setRespostas(dto.getRespostas());
        sintomas.setScoreTotal(dto.getScoreTotal());
        sintomas.setFkQuestionnaireId(dto.getQuestionnaireId());

        ReportSymptomsEntidade salvo = symptomsRepository.save(sintomas);

        // Métricas Prometheus
        metrics.incrementSymptomsCreated();
        metrics.recordScore(dto.getScoreTotal());
        if (isDisease) {
            metrics.incrementDiseaseDetected();
        }

        // Evento publicado APÓS commit (via @TransactionalEventListener) → geo-worker processa H3
        eventPublisher.publishEvent(
                new ReportCreatedEvent("report.symptom.created", ReportMapper.toResponseDTO(salvo))
        );

        log.info("[Symptoms] Report criado. reportId={}, isDisease={}", salvo.getId(), isDisease);
        return null;  // Void — o HTTP controller retorna 202 sem body
    }
}
