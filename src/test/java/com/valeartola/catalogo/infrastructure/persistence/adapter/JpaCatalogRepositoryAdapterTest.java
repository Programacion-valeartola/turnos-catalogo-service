package com.valeartola.catalogo.infrastructure.persistence.adapter;

import com.valeartola.catalogo.domain.exception.CatalogResyncRequiredException;
import com.valeartola.catalogo.domain.model.*;
import com.valeartola.catalogo.infrastructure.persistence.repository.ProfessionalJpaRepository;
import com.valeartola.catalogo.infrastructure.persistence.repository.WeeklyScheduleJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prueba contra el PostgreSQL de Docker. Cada test corre dentro de una transacción
 * que se deshace al final: los datos reales del catálogo no se modifican.
 */
@SpringBootTest
@Transactional
class JpaCatalogRepositoryAdapterTest {

    private static final Instant T0 = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant T1 = Instant.parse("2026-09-10T13:40:00Z");

    @Autowired
    private JpaCatalogRepositoryAdapter adapter;
    @Autowired
    private ProfessionalJpaRepository professionalRepository;
    @Autowired
    private WeeklyScheduleJpaRepository scheduleRepository;

    @BeforeEach
    void partirDeLaVersion1() {
        adapter.replaceWithSnapshot(new CatalogSnapshot(1, T0,
                List.of(new ProfessionalCategory(101L, "Clinica medica", "General", true, T0, T0)),
                List.of(professional(true)),
                List.of(schedule(301L))));
    }

    @Test
    void actualizaLoExistenteInsertaLoNuevoYAvanzaLaVersion() {
        adapter.applyIncrementalUpdate(new CatalogIncrementalUpdate(2,
                List.of(), List.of(professional(false)), List.of(schedule(302L))));

        assertThat(professionalRepository.findById(215L).orElseThrow().isEnabled()).isFalse();
        assertThat(scheduleRepository.count()).isEqualTo(2);
        CatalogSyncState state = adapter.getSyncState();
        assertThat(state.localVersion()).isEqualTo(2L);
        assertThat(state.lastSyncType()).isEqualTo(SyncType.INCREMENTAL);
    }

    @Test
    void reprocesarUnaVersionYaAplicadaNoCambiaNada() {
        adapter.applyIncrementalUpdate(new CatalogIncrementalUpdate(2,
                List.of(), List.of(professional(false)), List.of()));

        // Llega otra vez la versión 2 (con otros datos): se ignora
        adapter.applyIncrementalUpdate(new CatalogIncrementalUpdate(2,
                List.of(), List.of(professional(true)), List.of()));

        assertThat(professionalRepository.findById(215L).orElseThrow().isEnabled()).isFalse();
        assertThat(adapter.getSyncState().localVersion()).isEqualTo(2L);
    }

    @Test
    void noPermiteSaltearVersiones() {
        assertThatThrownBy(() -> adapter.applyIncrementalUpdate(
                new CatalogIncrementalUpdate(3, List.of(), List.of(professional(false)), List.of())))
                .isInstanceOf(CatalogResyncRequiredException.class);

        assertThat(professionalRepository.findById(215L).orElseThrow().isEnabled()).isTrue();
    }

    private static Professional professional(boolean enabled) {
        return new Professional(215L, 101L, "Ana", "Gómez", enabled, T0, T1);
    }

    private static WeeklySchedule schedule(long id) {
        return new WeeklySchedule(id, 215L, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0), 30, true, T0, T0);
    }
}