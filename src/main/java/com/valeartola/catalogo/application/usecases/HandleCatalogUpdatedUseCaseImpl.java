package com.valeartola.catalogo.application.usecases;

import com.valeartola.catalogo.domain.model.CatalogSyncOutcome;
import com.valeartola.catalogo.domain.model.CatalogSyncState;
import com.valeartola.catalogo.domain.model.CatalogUpdatedEvent;
import com.valeartola.catalogo.domain.ports.in.HandleCatalogUpdatedUseCase;
import com.valeartola.catalogo.domain.ports.in.KeepCatalogUpToDateUseCase;
import com.valeartola.catalogo.domain.ports.out.CatalogRepository;
import com.valeartola.catalogo.domain.ports.out.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Ante un aviso CatalogUpdated, pone al día la copia local reutilizando
 * la sincronización existente. Cada eventId se procesa una sola vez:
 * Kafka garantiza entrega "al menos una vez", así que un aviso puede repetirse.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HandleCatalogUpdatedUseCaseImpl implements HandleCatalogUpdatedUseCase {

    private final CatalogRepository catalogRepository;
    private final KeepCatalogUpToDateUseCase keepCatalogUpToDate;
    private final ProcessedEventRepository processedEvents;

    @Override
    public void handle(CatalogUpdatedEvent event) {
        if (processedEvents.isProcessed(event.eventId())) {
            log.info("Evento {} ya procesado: se ignora el duplicado", event.eventId());
            return;
        }
        log.info("Aviso CatalogUpdated recibido: versión {} (evento {})", event.newVersion(), event.eventId());

        CatalogSyncState state = catalogRepository.getSyncState();
        if (!state.requiresSnapshot() && event.newVersion() <= state.localVersion()) {
            log.info("La versión {} ya está aplicada (versión local {}): no se sincroniza",
                    event.newVersion(), state.localVersion());
            processedEvents.markProcessed(event.eventId(), CatalogUpdatedEvent.TYPE);
            return;
        }

        CatalogSyncOutcome outcome = keepCatalogUpToDate.synchronize();
        log.info("Sincronización por aviso de la versión {}: {}", event.newVersion(), outcome);

        // Solo se marca como procesado si la sincronización terminó bien.
        // Si falló, la verificación periódica recupera el atraso.
        if (outcome != CatalogSyncOutcome.FAILED) {
            processedEvents.markProcessed(event.eventId(), CatalogUpdatedEvent.TYPE);
        }
    }
}