package com.valeartola.catalogo;

import com.valeartola.catalogo.domain.model.*;
import com.valeartola.catalogo.domain.ports.in.HandleCatalogUpdatedUseCase;
import com.valeartola.catalogo.domain.ports.in.KeepCatalogUpToDateUseCase;
import com.valeartola.catalogo.domain.ports.out.CatalogChangesProvider;
import com.valeartola.catalogo.domain.ports.out.CatalogRepository;
import com.valeartola.catalogo.domain.ports.out.CatalogSnapshotProvider;
import com.valeartola.catalogo.infrastructure.persistence.entity.CatalogSyncStateEntity;
import com.valeartola.catalogo.infrastructure.persistence.repository.CatalogSyncStateJpaRepository;
import com.valeartola.catalogo.infrastructure.persistence.repository.ProfessionalJpaRepository;
import com.valeartola.catalogo.infrastructure.persistence.repository.WeeklyScheduleJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas de flujo completo de la sincronización: casos de uso reales y PostgreSQL real.
 * Solo la cátedra se simula (Redis y el snapshot REST), con Mockito.
 * Cada test corre en una transacción que se deshace al terminar.
 */
@SpringBootTest(properties = {
        "catalog.sync.on-startup=false",
        "catalog.sync.kafka-enabled=false",
        "catalog.sync.max-attempts=1",
        "catalog.sync.retry-backoff-ms=0"})
@Transactional
class CatalogSynchronizationFlowTest {

    private static final Instant T0 = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant T1 = Instant.parse("2026-09-10T13:40:00Z");

    // La cátedra simulada
    @MockitoBean
    private CatalogSnapshotProvider snapshotProvider;
    @MockitoBean
    private CatalogChangesProvider changesProvider;

    // Lo real
    @Autowired
    private KeepCatalogUpToDateUseCase keepCatalogUpToDate;
    @Autowired
    private HandleCatalogUpdatedUseCase handleCatalogUpdated;
    @Autowired
    private CatalogRepository catalogRepository;
    @Autowired
    private ProfessionalJpaRepository professionalRepository;
    @Autowired
    private WeeklyScheduleJpaRepository scheduleRepository;
    @Autowired
    private CatalogSyncStateJpaRepository syncStateRepository;

    @Test
    void conLaBaseVaciaSeAplicaElSnapshotInicial() {
        vaciarEstadoLocal();
        when(snapshotProvider.fetchSnapshot()).thenReturn(snapshotVersion1());

        CatalogSyncOutcome outcome = keepCatalogUpToDate.synchronize();

        assertThat(outcome).isEqualTo(CatalogSyncOutcome.SNAPSHOT);
        assertThat(catalogRepository.getSyncState().localVersion()).isEqualTo(1L);
        assertThat(professionalRepository.findById(215L)).isPresent();
    }

    @Test
    void lasVersionesNuevasSeAplicanIncrementalmenteEnOrden() {
        catalogRepository.replaceWithSnapshot(snapshotVersion1());
        when(changesProvider.fetchVersionWindow()).thenReturn(new CatalogVersionWindow(3, 1, T1));
        // Versión 2: se deshabilita el profesional 215
        when(changesProvider.fetchChanges(2)).thenReturn(Optional.of(
                new CatalogChangeSet(2, List.of(), List.of(215L), List.of())));
        when(changesProvider.fetchProfessionals(List.of(215L))).thenReturn(List.of(professional(215L, false)));
        // Versión 3: se agrega un horario nuevo
        when(changesProvider.fetchChanges(3)).thenReturn(Optional.of(
                new CatalogChangeSet(3, List.of(), List.of(), List.of(302L))));
        when(changesProvider.fetchSchedules(List.of(302L))).thenReturn(List.of(schedule(302L, 215L)));

        CatalogSyncOutcome outcome = keepCatalogUpToDate.synchronize();

        assertThat(outcome).isEqualTo(CatalogSyncOutcome.INCREMENTAL);
        assertThat(catalogRepository.getSyncState().localVersion()).isEqualTo(3L);
        assertThat(catalogRepository.getSyncState().lastSyncType()).isEqualTo(SyncType.INCREMENTAL);
        assertThat(professionalRepository.findById(215L).orElseThrow().isEnabled()).isFalse();
        assertThat(scheduleRepository.count()).isEqualTo(2);
    }

    @Test
    void unaDiscontinuidadReconstruyeLaCopiaConUnSnapshot() {
        catalogRepository.replaceWithSnapshot(snapshotVersion1());
        // Caso 18.2 del contrato: la cátedra ya no conserva los cambios que conectan la versión local
        when(changesProvider.fetchVersionWindow()).thenReturn(new CatalogVersionWindow(7, 4, T1));
        when(snapshotProvider.fetchSnapshot()).thenReturn(new CatalogSnapshot(7, T1,
                List.of(category()), List.of(professional(216L, true)), List.of(schedule(310L, 216L))));

        CatalogSyncOutcome outcome = keepCatalogUpToDate.synchronize();

        assertThat(outcome).isEqualTo(CatalogSyncOutcome.SNAPSHOT);
        assertThat(catalogRepository.getSyncState().localVersion()).isEqualTo(7L);
        assertThat(professionalRepository.existsById(215L)).isFalse();
        assertThat(professionalRepository.existsById(216L)).isTrue();
    }

    @Test
    void unAvisoDuplicadoNoSeProcesaDosVeces() {
        catalogRepository.replaceWithSnapshot(snapshotVersion1());
        when(changesProvider.fetchVersionWindow()).thenReturn(new CatalogVersionWindow(2, 1, T1));
        when(changesProvider.fetchChanges(2)).thenReturn(Optional.of(
                new CatalogChangeSet(2, List.of(), List.of(215L), List.of())));
        when(changesProvider.fetchProfessionals(List.of(215L))).thenReturn(List.of(professional(215L, false)));
        CatalogUpdatedEvent aviso = new CatalogUpdatedEvent("catalog-event-2", 2, T1, "MANUAL");

        handleCatalogUpdated.handle(aviso);
        handleCatalogUpdated.handle(aviso);   // Kafka lo entrega de nuevo

        assertThat(catalogRepository.getSyncState().localVersion()).isEqualTo(2L);
        verify(changesProvider, times(1)).fetchChanges(2);   // la versión se leyó una sola vez
    }

    // ---------- datos de prueba ----------

    private void vaciarEstadoLocal() {
        CatalogSyncStateEntity state = syncStateRepository.findById(CatalogSyncStateEntity.SINGLE_ROW_ID).orElseThrow();
        state.setLocalVersion(null);
        syncStateRepository.save(state);
    }

    private static CatalogSnapshot snapshotVersion1() {
        return new CatalogSnapshot(1, T0,
                List.of(category()), List.of(professional(215L, true)), List.of(schedule(301L, 215L)));
    }

    private static ProfessionalCategory category() {
        return new ProfessionalCategory(101L, "Clinica medica", "Atencion general", true, T0, T0);
    }

    private static Professional professional(long id, boolean enabled) {
        return new Professional(id, 101L, "Ana", "Gómez", enabled, T0, T1);
    }

    private static WeeklySchedule schedule(long id, long professionalId) {
        return new WeeklySchedule(id, professionalId, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0), 30, true, T0, T0);
    }
}