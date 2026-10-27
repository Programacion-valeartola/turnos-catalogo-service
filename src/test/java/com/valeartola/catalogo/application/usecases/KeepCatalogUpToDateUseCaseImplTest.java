package com.valeartola.catalogo.application.usecases;

import com.valeartola.catalogo.domain.exception.CatalogChangesUnavailableException;
import com.valeartola.catalogo.domain.exception.CatalogResyncRequiredException;
import com.valeartola.catalogo.domain.model.*;
import com.valeartola.catalogo.domain.ports.in.ApplyCatalogChangesUseCase;
import com.valeartola.catalogo.domain.ports.in.SynchronizeCatalogUseCase;
import com.valeartola.catalogo.domain.ports.out.CatalogRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.assertj.core.api.Assertions.assertThat;

class KeepCatalogUpToDateUseCaseImplTest {

    /** Base falsa: solo importa en qué versión está. */
    static class FakeRepository implements CatalogRepository {
        Long localVersion;

        @Override
        public CatalogSyncState getSyncState() {
            return new CatalogSyncState(localVersion, null, null, null);
        }

        @Override public void replaceWithSnapshot(CatalogSnapshot snapshot) { }
        @Override public void applyIncrementalUpdate(CatalogIncrementalUpdate update) { }
        @Override public void recordSyncError(String errorMessage) { }
    }

    /** Snapshot falso: cuenta cuántas veces se pidió. */
    static class FakeSnapshotUseCase implements SynchronizeCatalogUseCase {
        int calls;

        @Override
        public boolean synchronizeIfEmpty() {
            throw new AssertionError("No se usa");
        }

        @Override
        public CatalogSyncState applySnapshot() {
            calls++;
            return null;
        }
    }

    /** Incremental falso: devuelve, en orden, los resultados cargados (un número o una excepción). */
    static class FakeChangesUseCase implements ApplyCatalogChangesUseCase {
        final Deque<Object> results = new ArrayDeque<>();
        int calls;

        @Override
        public int applyPendingVersions() {
            calls++;
            Object result = results.isEmpty() ? 0 : results.poll();
            if (result instanceof RuntimeException e) {
                throw e;
            }
            return (Integer) result;
        }
    }

    private final FakeRepository repository = new FakeRepository();
    private final FakeSnapshotUseCase snapshot = new FakeSnapshotUseCase();
    private final FakeChangesUseCase changes = new FakeChangesUseCase();
    // 3 intentos y sin espera entre intentos, para que el test sea rápido
    private final KeepCatalogUpToDateUseCaseImpl useCase =
            new KeepCatalogUpToDateUseCaseImpl(repository, snapshot, changes, 3, 0);

    @Test
    void conBaseVaciaAplicaSnapshot() {
        assertThat(useCase.synchronize()).isEqualTo(CatalogSyncOutcome.SNAPSHOT);
        assertThat(snapshot.calls).isEqualTo(1);
        assertThat(changes.calls).isZero();
    }

    @Test
    void siEstaAlDiaNoHaceNada() {
        repository.localVersion = 1L;
        changes.results.add(0);

        assertThat(useCase.synchronize()).isEqualTo(CatalogSyncOutcome.UP_TO_DATE);
        assertThat(snapshot.calls).isZero();
    }

    @Test
    void siHayVersionesNuevasLasAplicaIncrementalmente() {
        repository.localVersion = 1L;
        changes.results.add(2);

        assertThat(useCase.synchronize()).isEqualTo(CatalogSyncOutcome.INCREMENTAL);
        assertThat(snapshot.calls).isZero();
    }

    @Test
    void anteUnaDiscontinuidadReconstruyeConSnapshot() {
        repository.localVersion = 3L;
        changes.results.add(new CatalogResyncRequiredException("versión local fuera de la ventana"));

        assertThat(useCase.synchronize()).isEqualTo(CatalogSyncOutcome.SNAPSHOT);
        assertThat(snapshot.calls).isEqualTo(1);
    }

    @Test
    void anteUnaFallaTransitoriaReintentaYSeRecupera() {
        repository.localVersion = 1L;
        changes.results.add(new CatalogChangesUnavailableException("Redis no disponible"));
        changes.results.add(1);

        assertThat(useCase.synchronize()).isEqualTo(CatalogSyncOutcome.INCREMENTAL);
        assertThat(changes.calls).isEqualTo(2);
    }

    @Test
    void losReintentosSonLimitados() {
        repository.localVersion = 1L;
        for (int i = 0; i < 5; i++) {
            changes.results.add(new CatalogChangesUnavailableException("Redis no disponible"));
        }

        assertThat(useCase.synchronize()).isEqualTo(CatalogSyncOutcome.FAILED);
        assertThat(changes.calls).isEqualTo(3);   // no 5: se detiene al tercer intento
    }
}
