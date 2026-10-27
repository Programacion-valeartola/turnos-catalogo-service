package com.valeartola.catalogo.application.usecases;

import com.valeartola.catalogo.domain.exception.CatedraIntegrationException;
import com.valeartola.catalogo.domain.model.CatalogSnapshot;
import com.valeartola.catalogo.domain.model.CatalogSyncState;
import com.valeartola.catalogo.domain.model.SyncType;
import com.valeartola.catalogo.domain.ports.out.CatalogRepository;
import org.junit.jupiter.api.Test;
import com.valeartola.catalogo.domain.model.CatalogIncrementalUpdate;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prueba el caso de uso sin base de datos ni cátedra:
 * se reemplazan ambos puertos por versiones falsas en memoria.
 */
class SynchronizeCatalogUseCaseImplTest {

    private static final CatalogSnapshot SNAPSHOT =
            new CatalogSnapshot(1L, Instant.now(), List.of(), List.of(), List.of());

    /** Repositorio falso: guarda todo en variables en lugar de PostgreSQL. */
    static class FakeCatalogRepository implements CatalogRepository {
        Long localVersion;
        String lastError;
        int snapshotsApplied;

        @Override
        public CatalogSyncState getSyncState() {
            return new CatalogSyncState(localVersion, null,
                    localVersion == null ? null : SyncType.SNAPSHOT, lastError);
        }

        @Override
        public void replaceWithSnapshot(CatalogSnapshot snapshot) {
            localVersion = snapshot.version();
            lastError = null;
            snapshotsApplied++;
        }

        @Override
        public void recordSyncError(String errorMessage) {
            lastError = errorMessage;
        }

        @Override
        public void applyIncrementalUpdate(CatalogIncrementalUpdate update) {
            localVersion = update.version();
        }
    }

    @Test
    void conBaseVaciaAplicaElSnapshotYGuardaLaVersion() {
        FakeCatalogRepository repository = new FakeCatalogRepository();
        var useCase = new SynchronizeCatalogUseCaseImpl(() -> SNAPSHOT, repository);

        boolean applied = useCase.synchronizeIfEmpty();

        assertThat(applied).isTrue();
        assertThat(repository.localVersion).isEqualTo(1L);
        assertThat(repository.snapshotsApplied).isEqualTo(1);
    }

    @Test
    void siYaHayVersionLocalNoPideSnapshot() {
        FakeCatalogRepository repository = new FakeCatalogRepository();
        repository.localVersion = 5L;
        var useCase = new SynchronizeCatalogUseCaseImpl(
                () -> { throw new AssertionError("No debería pedir el snapshot"); }, repository);

        boolean applied = useCase.synchronizeIfEmpty();

        assertThat(applied).isFalse();
        assertThat(repository.localVersion).isEqualTo(5L);
    }

    @Test
    void siLaCatedraFallaRegistraElErrorYNoCambiaLaVersion() {
        FakeCatalogRepository repository = new FakeCatalogRepository();
        var useCase = new SynchronizeCatalogUseCaseImpl(() -> {
            throw new CatedraIntegrationException(503, CatedraIntegrationException.UNAVAILABLE, "sin conexión");
        }, repository);

        assertThatThrownBy(useCase::applySnapshot).isInstanceOf(CatedraIntegrationException.class);
        assertThat(repository.localVersion).isNull();
        assertThat(repository.lastError).contains("sin conexión");
    }
}
