package br.ifpb.project.denguemaps.pdmreportms.producer;

import br.ifpb.project.denguemaps.pdmreportms.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReportPublisher {

    private final RabbitTemplate rabbitTemplate;

    // Injeção de dependência do RabbitTemplate configurado anteriormente
    public ReportPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Método para publicar um evento de reporte na Exchange.
     * @param routingKey A chave de roteamento (ex: "report.created")
     * @param message O objeto ou mensagem que queres enviar
     */
    public void publishReportEvent(String routingKey, Object message) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, message);
            System.out.println("[RabbitMQ] Mensagem enviada com sucesso para a rota: " + routingKey);
        } catch (Exception e) {
            System.err.println("[RabbitMQ] Erro ao enviar mensagem: " + e.getMessage());
        }
    }
}