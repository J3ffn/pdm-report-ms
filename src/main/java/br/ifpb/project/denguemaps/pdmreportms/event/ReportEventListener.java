package br.ifpb.project.denguemaps.pdmreportms.event;

import br.ifpb.project.denguemaps.pdmreportms.producer.ReportPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener de eventos de domínio transacionais.
 * Garante que mensagens só sejam publicadas no RabbitMQ APÓS o commit bem-sucedido
 * da transação no banco de dados, eliminando riscos de inconsistência por rollback.
 */
@Component
@RequiredArgsConstructor
public class ReportEventListener {

    private static final Logger log = LoggerFactory.getLogger(ReportEventListener.class);

    private final ReportPublisher reportPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReportCreated(ReportCreatedEvent event) {
        log.debug("Disparando publicação de evento pós-commit: {}", event.routingKey());
        reportPublisher.publishReportEvent(event.routingKey(), event.payload());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReportDisabled(ReportDisabledEvent event) {
        log.debug("Disparando publicação de evento pós-commit: {}", event.routingKey());
        reportPublisher.publishReportEvent(event.routingKey(), event.payload());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReportVisited(ReportVisitedEvent event) {
        log.debug("Disparando publicação de evento pós-commit: {}", event.routingKey());
        reportPublisher.publishReportEvent(event.routingKey(), event.payload());
    }
}
