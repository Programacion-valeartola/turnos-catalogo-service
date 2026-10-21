package com.valeartola.catalogo.application.usecases;

import com.valeartola.catalogo.domain.model.CatalogSnapshot;
import com.valeartola.catalogo.domain.model.CatalogSyncState;
import com.valeartola.catalogo.domain.ports.in.SynchronizeCatalogUseCase;
import com.valeartola.catalogo.domain.ports.out.CatalogRepository;
import com.valeartola.catalogo.domain.ports.out.CatalogSnapshotProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Caso de uso: pide el snapshot a la cátedra (puerto CatalogSnapshotProvider)
 * y lo guarda en la copia local (puerto CatalogRepository).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SynchronizeCatalogUseCaseImpl implements SynchronizeCatalogUseCase {

    private final CatalogSnapshotProvider snapshotProvider;
    private final CatalogRepository catalogRepository;

    @Override
    public boolean synchronizeIfEmpty() {
        CatalogSyncState state = catalogRepository.getSyncState();
        if (!state.requiresSnapshot()) {
            log.info("La copia local ya está en la versión {}: no se requiere snapshot", state.localVersion());
            return false;
        }
        log.info("Base local vacía: se solicita el snapshot completo");
        applySnapshot();
        return true;
    }

    @Override
    public CatalogSyncState applySnapshot() {
        try {
            CatalogSnapshot snapshot = snapshotProvider.fetchSnapshot();
            catalogRepository.replaceWithSnapshot(snapshot);
            log.info("Snapshot aplicado correctamente: versión local {}", snapshot.version());
            return catalogRepository.getSyncState();
        } catch (RuntimeException e) {
            // Los datos anteriores quedan intactos; solo se registra el error
            catalogRepository.recordSyncError(e.getClass().getSimpleName() + ": " + e.getMessage());
            throw e;
        }
    }
}
