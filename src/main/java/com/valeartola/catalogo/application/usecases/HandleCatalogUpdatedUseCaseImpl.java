package com.valeartola.catalogo.application.usecases;

import com.valeartola.catalogo.domain.model.CatalogSyncOutcome;
import com.valeartola.catalogo.domain.model.CatalogSyncState;
import com.valeartola.catalogo.domain.model.CatalogUpdatedEvent;
import com.valeartola.catalogo.domain.ports.in.HandleCatalogUpdatedUseCase;
import com.valeartola.catalogo.domain.ports.in.KeepCatalogUpToDateUseCase;
import com.valeartola.catalogo.domain.ports.out.CatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Ante un aviso CatalogUpdated, pone al día la copia local reutilizando
 * la sincronización existente (incremental o snapshot según corresponda).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HandleCatalogUpdatedUseCaseImpl implements HandleCatalogUpdatedUseCase {

    private final CatalogRepository catalogRepository;
    private final KeepCatalogUpToDateUseCase keepCatalogUpToDate;

    @Override
    public void handle(CatalogUpdatedEvent event) {
        log.info("Aviso CatalogUpdated recibido: versión {} (evento {})", event.newVersion(), event.eventId());

        CatalogSyncState state = catalogRepository.getSyncState();
        if (!state.requiresSnapshot() && event.newVersion() <= state.localVersion()) {
            // Aviso viejo o repetido: la copia local ya incluye esa versión
            log.info("La versión {} ya está aplicada (versión local {}): no se sincroniza",
                    event.newVersion(), state.localVersion());
            return;
        }

        CatalogSyncOutcome outcome = keepCatalogUpToDate.synchronize();
        log.info("Sincronización por aviso de la versión {}: {}", event.newVersion(), outcome);
    }
}