package br.ifpb.project.denguemaps.pdmreportms.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Nome da Exchange solicitado na issue do GitHub
    public static final String EXCHANGE_NAME = "reports.events";

    /**
     * Define a Exchange do tipo Topic no RabbitMQ.
     */
    @Bean
    public TopicExchange reportsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    /**
     * Configura o RabbitTemplate para usar JSON ao enviar mensagens,
     * em vez do formato de serialização padrão do Java.
     */
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