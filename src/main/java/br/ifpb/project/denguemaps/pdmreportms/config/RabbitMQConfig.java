package br.ifpb.project.denguemaps.pdmreportms.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do RabbitMQ para o pdm-report-ms.
 *
 * <h3>Topologia de Mensageria</h3>
 * <pre>
 *   Exchange: reports.events (TopicExchange)
 *     │
 *     ├── Binding: report.*.created  → reports.geo.queue
 *     │     Worker: worker-geo (recalcula H3 / atualiza heatmap)
 *     │
 *     ├── Binding: report.symptom.created → reports.alert.queue
 *     │     Worker: worker-alert (avalia limiar de risco epidemiológico)
 *     │
 *     └── Binding: report.focus.created  → reports.notify.queue
 *           Worker: worker-notification (e-mail para agentes de saúde)
 * </pre>
 *
 * <p>As filas já estão declaradas aqui — quando os workers forem criados,
 * basta apontar para os nomes definidos nas constantes abaixo (sem alterar este arquivo).
 */
@Configuration
public class RabbitMQConfig {

    // -------------------------------------------------------------------------
    // Exchange
    // -------------------------------------------------------------------------
    public static final String EXCHANGE = "reports.events";

    // -------------------------------------------------------------------------
    // Filas — nomes consumidos pelos workers
    // -------------------------------------------------------------------------
    /** Consumida por: worker-geo (recalcula heatmap H3 após novo report) */
    public static final String QUEUE_GEO     = "reports.geo.queue";

    /** Consumida por: worker-alert (avalia limiar de risco por célula H3) */
    public static final String QUEUE_ALERT   = "reports.alert.queue";

    /** Consumida por: worker-notification (e-mail/push para agentes de saúde) */
    public static final String QUEUE_NOTIFY  = "reports.notify.queue";

    // -------------------------------------------------------------------------
    // Routing Keys
    // -------------------------------------------------------------------------
    /** Qualquer tipo de report criado (usado pelo worker-geo) */
    public static final String RK_ANY_CREATED     = "report.*.created";

    /** Apenas sintomas criados (usado pelo worker-alert — avaliação de doença) */
    public static final String RK_SYMPTOM_CREATED = "report.symptom.created";

    /** Apenas focos criados (usado pelo worker-notification — alerta de campo) */
    public static final String RK_FOCUS_CREATED   = "report.focus.created";

    // -------------------------------------------------------------------------
    // Beans Spring AMQP
    // -------------------------------------------------------------------------

    @Bean
    public TopicExchange reportsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue geoQueue() {
        return QueueBuilder.durable(QUEUE_GEO).build();
    }

    @Bean
    public Queue alertQueue() {
        return QueueBuilder.durable(QUEUE_ALERT).build();
    }

    @Bean
    public Queue notifyQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFY).build();
    }

    /** worker-geo recebe TODOS os reports criados (focus + symptoms) */
    @Bean
    public Binding geoBinding(Queue geoQueue, TopicExchange reportsExchange) {
        return BindingBuilder.bind(geoQueue).to(reportsExchange).with(RK_ANY_CREATED);
    }

    /** worker-alert recebe apenas reports de sintomas (avalia risco de doença) */
    @Bean
    public Binding alertBinding(Queue alertQueue, TopicExchange reportsExchange) {
        return BindingBuilder.bind(alertQueue).to(reportsExchange).with(RK_SYMPTOM_CREATED);
    }

    /** worker-notification recebe apenas focos (notifica agentes de campo) */
    @Bean
    public Binding notifyBinding(Queue notifyQueue, TopicExchange reportsExchange) {
        return BindingBuilder.bind(notifyQueue).to(reportsExchange).with(RK_FOCUS_CREATED);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
