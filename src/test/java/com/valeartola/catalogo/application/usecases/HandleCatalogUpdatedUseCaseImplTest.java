package com.valeartola.catalogo.application.usecases;

import com.valeartola.catalogo.domain.model.*;
import com.valeartola.catalogo.domain.ports.out.CatalogRepository;
import com.valeartola.catalogo.domain.ports.out.ProcessedEventRepository;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

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

    /** Registro falso de eventos procesados, en memoria. */
    static class FakeProcessedEvents implements ProcessedEventRepository {
        final Set<String> ids = new HashSet<>();

        @Override
        public boolean isProcessed(String eventId) {
            return ids.contains(eventId);
        }

        @Override
        public void markProcessed(String eventId, String eventType) {
            ids.add(eventId);
        }
    }

    private final FakeRepository repository = new FakeRepository();
    private final FakeProcessedEvents processedEvents = new FakeProcessedEvents();
    private CatalogSyncOutcome nextOutcome = CatalogSyncOutcome.INCREMENTAL;
    private int synchronizations;

    private final HandleCatalogUpdatedUseCaseImpl useCase = new HandleCatalogUpdatedUseCaseImpl(
            repository,
            () -> {
                synchronizations++;
                return nextOutcome;
            },
            processedEvents);

    private static CatalogUpdatedEvent event(String eventId, long version) {
        return new CatalogUpdatedEvent(eventId, version, null, "MANUAL");
    }

    @Test
    void unaVersionNuevaDisparaLaSincronizacionYRegistraElEvento() {
        repository.localVersion = 1L;

        useCase.handle(event("catalog-event-2", 2));

        assertThat(synchronizations).isEqualTo(1);
        assertThat(processedEvents.ids).containsExactly("catalog-event-2");
    }

    @Test
    void unEventoDuplicadoNoProduceEfectos() {
        repository.localVersion = 1L;

        useCase.handle(event("catalog-event-2", 2));
        repository.localVersion = 1L;   // aunque la versión local no hubiera cambiado...
        useCase.handle(event("catalog-event-2", 2));   // ...el mismo eventId no se reprocesa

        assertThat(synchronizations).isEqualTo(1);
    }

    @Test
    void unaVersionYaAplicadaNoSincronizaPeroQuedaRegistrada() {
        repository.localVersion = 5L;

        useCase.handle(event("catalog-event-5", 5));
        useCase.handle(event("catalog-event-3", 3));

        assertThat(synchronizations).isZero();
        assertThat(processedEvents.ids).containsExactlyInAnyOrder("catalog-event-5", "catalog-event-3");
    }

    @Test
    void conLaBaseVaciaSincronizaAunqueLaVersionSeaBaja() {
        repository.localVersion = null;

        useCase.handle(event("catalog-event-1", 1));

        assertThat(synchronizations).isEqualTo(1);
    }

    @Test
    void siLaSincronizacionFallaElEventoNoSeRegistra() {
        repository.localVersion = 1L;
        nextOutcome = CatalogSyncOutcome.FAILED;

        useCase.handle(event("catalog-event-2", 2));

        assertThat(processedEvents.ids).isEmpty();
    }
}