package com.valeartola.catalogo.application.usecases;

import com.valeartola.catalogo.domain.exception.CatalogResyncRequiredException;
import com.valeartola.catalogo.domain.model.CatalogChangeSet;
import com.valeartola.catalogo.domain.model.CatalogIncrementalUpdate;
import com.valeartola.catalogo.domain.model.CatalogSyncState;
import com.valeartola.catalogo.domain.model.CatalogVersionWindow;
import com.valeartola.catalogo.domain.ports.in.ApplyCatalogChangesUseCase;
import com.valeartola.catalogo.domain.ports.out.CatalogChangesProvider;
import com.valeartola.catalogo.domain.ports.out.CatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Caso de uso: lee de la fuente de cambios (Redis) las versiones pendientes
 * y las aplica una por una sobre la copia local.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplyCatalogChangesUseCaseImpl implements ApplyCatalogChangesUseCase {

    private final CatalogChangesProvider changesProvider;
    private final CatalogRepository catalogRepository;

    @Override
    public int applyPendingVersions() {
        CatalogSyncState state = catalogRepository.getSyncState();
        if (state.requiresSnapshot()) {
            throw new CatalogResyncRequiredException("La copia local está vacía: se requiere un snapshot");
        }
        long localVersion = state.localVersion();

        try {
            CatalogVersionWindow window = changesProvider.fetchVersionWindow();
            if (window.isUpToDate(localVersion)) {
                log.debug("Catálogo al día en la versión {}", localVersion);
                return 0;
            }
            if (window.requiresSnapshot(localVersion)) {
                throw new CatalogResyncRequiredException("La versión local " + localVersion
                        + " está fuera de la ventana disponible [" + window.oldestAvailableVersion()
                        + ", " + window.currentVersion() + "]");
            }

            int applied = 0;
            for (long version = localVersion + 1; version <= window.currentVersion(); version++) {
                applyVersion(version);
                applied++;
            }
            log.info("Catálogo actualizado de la versión {} a la {}", localVersion, window.currentVersion());
            return applied;

        } catch (RuntimeException e) {
            // Las versiones ya aplicadas quedan guardadas; solo se registra el error
            catalogRepository.recordSyncError(e.getClass().getSimpleName() + ": " + e.getMessage());
            throw e;
        }
    }

    private void applyVersion(long version) {
        CatalogChangeSet changes = changesProvider.fetchChanges(version)
                .orElseThrow(() -> new CatalogResyncRequiredException(
                        "Los cambios de la versión " + version + " ya no están disponibles"));

        // Se lee el estado VIGENTE de cada entidad afectada, según el contrato
        CatalogIncrementalUpdate update = new CatalogIncrementalUpdate(
                version,
                changesProvider.fetchCategories(changes.categoryIds()),
                changesProvider.fetchProfessionals(changes.professionalIds()),
                changesProvider.fetchSchedules(changes.scheduleIds()));

        catalogRepository.applyIncrementalUpdate(update);
        log.info("Versión {} aplicada: {} categorías, {} profesionales, {} horarios", version,
                update.categories().size(), update.professionals().size(), update.schedules().size());
    }
}