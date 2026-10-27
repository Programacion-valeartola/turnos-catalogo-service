package com.valeartola.catalogo.infrastructure.startup;

import com.valeartola.catalogo.domain.model.CatalogSyncOutcome;
import com.valeartola.catalogo.domain.ports.in.KeepCatalogUpToDateUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada: verificación periódica. Kafka es solo una notificación;
 * si un aviso se pierde, esta verificación detecta el atraso y lo recupera.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogPeriodicSynchronizer {

    private final KeepCatalogUpToDateUseCase keepCatalogUpToDate;

    @Scheduled(initialDelayString = "${catalog.sync.check-interval-ms:300000}",
            fixedDelayString = "${catalog.sync.check-interval-ms:300000}")
    public void checkForUpdates() {
        CatalogSyncOutcome outcome = keepCatalogUpToDate.synchronize();
        if (outcome != CatalogSyncOutcome.UP_TO_DATE) {
            log.info("Verificación periódica del catálogo: {}", outcome);
        }
    }
}
