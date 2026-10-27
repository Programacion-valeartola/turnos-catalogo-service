package com.valeartola.catalogo.infrastructure.startup;

import com.valeartola.catalogo.domain.model.CatalogSyncOutcome;
import com.valeartola.catalogo.domain.ports.in.KeepCatalogUpToDateUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada: al terminar de arrancar, pone al día el catálogo
 * (snapshot si la base está vacía o hubo discontinuidad; incremental si hay versiones nuevas).
 * Se puede desactivar con catalog.sync.on-startup=false (por ejemplo, en pruebas).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "catalog.sync.on-startup", havingValue = "true", matchIfMissing = true)
public class CatalogStartupSynchronizer {

    private final KeepCatalogUpToDateUseCase keepCatalogUpToDate;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        CatalogSyncOutcome outcome = keepCatalogUpToDate.synchronize();
        log.info("Sincronización inicial del catálogo: {}", outcome);
    }
}