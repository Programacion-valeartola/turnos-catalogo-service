package com.valeartola.catalogo.infrastructure.startup;

import com.valeartola.catalogo.domain.ports.in.SynchronizeCatalogUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada: cuando el servicio termina de arrancar,
 * verifica si la base está vacía y, en ese caso, pide el snapshot.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogStartupSynchronizer {

    private final SynchronizeCatalogUseCase synchronizeCatalogUseCase;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            synchronizeCatalogUseCase.synchronizeIfEmpty();
        } catch (Exception e) {
            // El servicio sigue funcionando; el error queda registrado en catalog_sync_state
            log.error("No se pudo inicializar el catálogo desde el snapshot: {}", e.getMessage());
        }
    }
}
