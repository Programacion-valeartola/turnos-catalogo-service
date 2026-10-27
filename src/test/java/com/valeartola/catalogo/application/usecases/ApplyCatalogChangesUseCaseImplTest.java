package com.valeartola.catalogo.application.usecases;

import com.valeartola.catalogo.domain.exception.CatalogChangesUnavailableException;
import com.valeartola.catalogo.domain.exception.CatalogResyncRequiredException;
import com.valeartola.catalogo.domain.model.*;
import com.valeartola.catalogo.domain.ports.out.CatalogChangesProvider;
import com.valeartola.catalogo.domain.ports.out.CatalogRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplyCatalogChangesUseCaseImplTest {

    private static final Instant T = Instant.parse("2026-09-10T13:40:00Z");

    /** Redis falso: ventana de versiones, cambios por versión y profesionales vigentes. */
    static class FakeChangesProvider implements CatalogChangesProvider {
        CatalogVersionWindow window;
        RuntimeException failure;
        final Map<Long, CatalogChangeSet> changes = new HashMap<>();
        final Map<Long, Professional> professionals = new HashMap<>();

        @Override
        public CatalogVersionWindow fetchVersionWindow() {
            if (failure != null) throw failure;
            return window;
        }

        @Override
        public Optional<CatalogChangeSet> fetchChanges(long version) {
            return Optional.ofNullable(changes.get(version));
        }

        @Override
        public List<ProfessionalCategory> fetchCategories(Collection<Long> ids) {
            return List.of();
        }

        @Override
        public List<Professional> fetchProfessionals(Collection<Long> ids) {
            return ids.stream().map(professionals::get).toList();
        }

        @Override
        public List<WeeklySchedule> fetchSchedules(Collection<Long> ids) {
            return List.of();
        }

        void addVersion(long version, long professionalId) {
            changes.put(version, new CatalogChangeSet(version, List.of(), List.of(professionalId), List.of()));
            professionals.put(professionalId,
                    new Professional(professionalId, 101L, "Ana", "Gómez", true, T, T));
        }
    }

    /** Base falsa: recuerda qué versiones se aplicaron y en qué orden. */
    static class FakeCatalogRepository implements CatalogRepository {
        Long localVersion;
        String lastError;
        final List<Long> appliedVersions = new ArrayList<>();

        @Override
        public CatalogSyncState getSyncState() {
            return new CatalogSyncState(localVersion, null, null, lastError);
        }

        @Override
        public void replaceWithSnapshot(CatalogSnapshot snapshot) {
            throw new AssertionError("El caso de uso incremental no debe aplicar snapshots");
        }

        @Override
        public void applyIncrementalUpdate(CatalogIncrementalUpdate update) {
            appliedVersions.add(update.version());
            localVersion = update.version();
        }

        @Override
        public void recordSyncError(String errorMessage) {
            lastError = errorMessage;
        }
    }

    private final FakeChangesProvider provider = new FakeChangesProvider();
    private final FakeCatalogRepository repository = new FakeCatalogRepository();
    private final ApplyCatalogChangesUseCaseImpl useCase = new ApplyCatalogChangesUseCaseImpl(provider, repository);

    @Test
    void aplicaLasVersionesPendientesEnOrden() {
        repository.localVersion = 1L;
        provider.window = new CatalogVersionWindow(3, 1, T);
        provider.addVersion(2, 215);
        provider.addVersion(3, 216);

        int applied = useCase.applyPendingVersions();

        assertThat(applied).isEqualTo(2);
        assertThat(repository.appliedVersions).containsExactly(2L, 3L);
        assertThat(repository.localVersion).isEqualTo(3L);
    }

    @Test
    void siEstaAlDiaNoAplicaNada() {
        repository.localVersion = 3L;
        provider.window = new CatalogVersionWindow(3, 1, T);

        assertThat(useCase.applyPendingVersions()).isZero();
        assertThat(repository.appliedVersions).isEmpty();
    }

    @Test
    void siFallaUnaVersionSeConservaLaUltimaAplicadaYSeRegistraElError() {
        repository.localVersion = 1L;
        provider.window = new CatalogVersionWindow(3, 1, T);
        provider.addVersion(2, 215);   // la versión 3 no está disponible

        assertThatThrownBy(useCase::applyPendingVersions)
                .isInstanceOf(CatalogResyncRequiredException.class);
        assertThat(repository.localVersion).isEqualTo(2L);
        assertThat(repository.lastError).contains("versión 3");
    }

    @Test
    void siLaVersionLocalQuedoFueraDeLaVentanaNoAplicaNada() {
        repository.localVersion = 3L;
        provider.window = new CatalogVersionWindow(7, 4, T);   // caso 18.2 del contrato

        assertThatThrownBy(useCase::applyPendingVersions)
                .isInstanceOf(CatalogResyncRequiredException.class);
        assertThat(repository.appliedVersions).isEmpty();
        assertThat(repository.localVersion).isEqualTo(3L);
    }

    @Test
    void siRedisNoRespondeRegistraElErrorSinCambiarLaVersion() {
        repository.localVersion = 1L;
        provider.failure = new CatalogChangesUnavailableException("Redis no disponible");

        assertThatThrownBy(useCase::applyPendingVersions)
                .isInstanceOf(CatalogChangesUnavailableException.class);
        assertThat(repository.localVersion).isEqualTo(1L);
        assertThat(repository.lastError).contains("Redis no disponible");
    }
}

