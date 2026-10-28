package com.valeartola.catalogo.application.usecases;

import com.valeartola.catalogo.domain.model.*;
import com.valeartola.catalogo.domain.ports.out.CatalogRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HandleCatalogUpdatedUseCaseImplTest {

    /** Base falsa: solo importa la versión local. */
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

    private final FakeRepository repository = new FakeRepository();
    private int synchronizations;
    private final HandleCatalogUpdatedUseCaseImpl useCase = new HandleCatalogUpdatedUseCaseImpl(repository, () -> {
        synchronizations++;
        return CatalogSyncOutcome.INCREMENTAL;
    });

    private static CatalogUpdatedEvent event(long version) {
        return new CatalogUpdatedEvent("catalog-event-" + version, version, null, "MANUAL");
    }

    @Test
    void unaVersionNuevaDisparaLaSincronizacion() {
        repository.localVersion = 1L;

        useCase.handle(event(2));

        assertThat(synchronizations).isEqualTo(1);
    }

    @Test
    void unaVersionYaAplicadaNoSincroniza() {
        repository.localVersion = 5L;

        useCase.handle(event(5));
        useCase.handle(event(3));

        assertThat(synchronizations).isZero();
    }

    @Test
    void conLaBaseVaciaSincronizaAunqueLaVersionSeaBaja() {
        repository.localVersion = null;

        useCase.handle(event(1));

        assertThat(synchronizations).isEqualTo(1);
    }
}