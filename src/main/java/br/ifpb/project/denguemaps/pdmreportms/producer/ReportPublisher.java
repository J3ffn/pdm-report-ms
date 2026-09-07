package br.ifpb.project.denguemaps.pdmreportms.producer;

import br.ifpb.project.denguemaps.pdmreportms.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportPublisher {

    private static final Logger log = LoggerFactory.getLogger(ReportPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publica um evento de reporte na Exchange topic do RabbitMQ.
     *
     * @param routingKey Chave de roteamento (ex: "report.focus.created", "report.symptom.created")
     * @param message    Objeto a ser serializado como JSON e enviado
     */
    public void publishReportEvent(String routingKey, Object message) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, message);
            log.info("[RabbitMQ] Evento publicado. exchange={}, routingKey={}", RabbitMQConfig.EXCHANGE_NAME, routingKey);
        } catch (Exception e) {
            log.error("[RabbitMQ] Falha ao publicar evento. routingKey={}, erro={}", routingKey, e.getMessage(), e);
        }
    }
}