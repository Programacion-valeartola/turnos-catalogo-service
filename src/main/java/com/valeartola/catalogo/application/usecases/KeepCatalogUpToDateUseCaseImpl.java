package com.valeartola.catalogo.application.usecases;

import com.valeartola.catalogo.domain.exception.CatalogResyncRequiredException;
import com.valeartola.catalogo.domain.model.CatalogSyncOutcome;
import com.valeartola.catalogo.domain.ports.in.ApplyCatalogChangesUseCase;
import com.valeartola.catalogo.domain.ports.in.KeepCatalogUpToDateUseCase;
import com.valeartola.catalogo.domain.ports.in.SynchronizeCatalogUseCase;
import com.valeartola.catalogo.domain.ports.out.CatalogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Decide cómo poner al día la copia local:
 * - base vacía → snapshot;
 * - versiones nuevas disponibles → incremental;
 * - discontinuidad (versión fuera de la ventana, versión faltante) → snapshot.
 * Ante fallas reintenta un número limitado de veces, con espera creciente.
 */
@Slf4j
@Component
public class KeepCatalogUpToDateUseCaseImpl implements KeepCatalogUpToDateUseCase {

    private final CatalogRepository catalogRepository;
    private final SynchronizeCatalogUseCase snapshotUseCase;
    private final ApplyCatalogChangesUseCase changesUseCase;
    private final int maxAttempts;
    private final long retryBackoffMs;

    public KeepCatalogUpToDateUseCaseImpl(CatalogRepository catalogRepository,
                                          SynchronizeCatalogUseCase snapshotUseCase,
                                          ApplyCatalogChangesUseCase changesUseCase,
                                          @Value("${catalog.sync.max-attempts:3}") int maxAttempts,
                                          @Value("${catalog.sync.retry-backoff-ms:2000}") long retryBackoffMs) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("catalog.sync.max-attempts debe ser al menos 1");
        }
        this.catalogRepository = catalogRepository;
        this.snapshotUseCase = snapshotUseCase;
        this.changesUseCase = changesUseCase;
        this.maxAttempts = maxAttempts;
        this.retryBackoffMs = retryBackoffMs;
    }

    /**
     * synchronized: el arranque, la verificación periódica y Kafka pueden pedir
     * una sincronización al mismo tiempo; se atienden de a una.
     */
    @Override
    public synchronized CatalogSyncOutcome synchronize() {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return synchronizeOnce();
            } catch (RuntimeException e) {
                log.warn("Intento {}/{} de sincronización del catálogo fallido: {}",
                        attempt, maxAttempts, e.getMessage());
                if (attempt < maxAttempts) {
                    waitBeforeRetry(attempt);
                }
            }
        }
        log.error("La sincronización del catálogo falló tras {} intentos; se reintentará en la próxima verificación",
                maxAttempts);
        return CatalogSyncOutcome.FAILED;
    }

    private CatalogSyncOutcome synchronizeOnce() {
        if (catalogRepository.getSyncState().requiresSnapshot()) {
            log.info("Base local vacía: se aplica un snapshot completo");
            snapshotUseCase.applySnapshot();
            return CatalogSyncOutcome.SNAPSHOT;
        }
        try {
            int applied = changesUseCase.applyPendingVersions();
            return applied == 0 ? CatalogSyncOutcome.UP_TO_DATE : CatalogSyncOutcome.INCREMENTAL;
        } catch (CatalogResyncRequiredException e) {
            log.warn("Discontinuidad detectada ({}): se reconstruye la copia local con un snapshot", e.getMessage());
            snapshotUseCase.applySnapshot();
            return CatalogSyncOutcome.SNAPSHOT;
        }
    }

    /** Espera creciente: 2 s antes del 2.º intento, 4 s antes del 3.º. */
    private void waitBeforeRetry(int attempt) {
        try {
            Thread.sleep(retryBackoffMs * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}