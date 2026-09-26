package br.ifpb.project.denguemaps.pdmreportms.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Métricas de negócio do pdm-report-ms expostas via Prometheus.
 *
 * <p>Acesse em: {@code GET /actuator/prometheus}
 *
 * <p>Métricas disponíveis:
 * <ul>
 *   <li>{@code report_symptoms_created_total} — total de questionários de sintomas enviados</li>
 *   <li>{@code report_focus_created_total} — total de focos reportados</li>
 *   <li>{@code report_disease_detected_total} — reports com score >= limiar (isDisease=true)</li>
 *   <li>{@code report_score_distribution} — histograma de distribuição de scores</li>
 * </ul>
 *
 */
@Component
@RequiredArgsConstructor
public class ReportMetrics {

    private final MeterRegistry registry;

    private Counter symptomsCreated;
    private Counter focusCreated;
    private Counter diseaseDetected;
    private DistributionSummary scoreDistribution;

    @jakarta.annotation.PostConstruct
    void init() {
        symptomsCreated = Counter.builder("report.symptoms.created")
                .description("Total de questionários de sintomas recebidos")
                .register(registry);

        focusCreated = Counter.builder("report.focus.created")
                .description("Total de focos de dengue reportados")
                .register(registry);

        diseaseDetected = Counter.builder("report.disease.detected")
                .description("Total de reports com score >= limiar (provável doença)")
                .register(registry);

        scoreDistribution = DistributionSummary.builder("report.score.distribution")
                .description("Distribuição dos scores calculados pelo frontend")
                .baseUnit("pontos")
                .maximumExpectedValue(100.0)
                .register(registry);
    }

    /** Chamado após persistência bem-sucedida de um report de sintomas. */
    public void incrementSymptomsCreated() {
        symptomsCreated.increment();
    }

    /** Chamado após persistência bem-sucedida de um report de foco. */
    public void incrementFocusCreated() {
        focusCreated.increment();
    }

    /** Chamado quando {@code isDisease = true} em um report de sintomas. */
    public void incrementDiseaseDetected() {
        diseaseDetected.increment();
    }

    /** Registra o score do questionário para análise de distribuição. */
    public void recordScore(int score) {
        scoreDistribution.record(score);
    }
}
