package com.valeartola.catalogo.application.usecases;

import com.valeartola.catalogo.domain.exception.InvalidSearchCriteriaException;
import com.valeartola.catalogo.domain.model.Professional;
import com.valeartola.catalogo.domain.model.ProfessionalCategory;
import com.valeartola.catalogo.domain.model.ProfessionalSearchCriteria;
import com.valeartola.catalogo.domain.model.ProfessionalSearchResult;
import com.valeartola.catalogo.domain.model.WeeklySchedule;
import com.valeartola.catalogo.domain.ports.out.CatalogReader;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prueba los cuatro filtros obligatorios sobre un catálogo en memoria.
 */
class SearchProfessionalsUseCaseImplTest {

    private static final Instant T = Instant.parse("2026-09-01T03:00:00Z");

    /** Catálogo falso: 2 categorías, 3 profesionales (uno deshabilitado) y sus horarios. */
    private final CatalogReader reader = new CatalogReader() {
        @Override
        public List<ProfessionalCategory> findAllCategories() {
            return List.of(
                    new ProfessionalCategory(101L, "Cardiologia", "", true, T, T),
                    new ProfessionalCategory(102L, "Pediatria", "", true, T, T));
        }

        @Override
        public List<Professional> findAllProfessionals() {
            return List.of(
                    new Professional(201L, 101L, "Ana", "Torres", true, T, T),
                    new Professional(204L, 102L, "Laura", "Médina", true, T, T),
                    new Professional(206L, 102L, "Carolina", "Molina", false, T, T));
        }

        @Override
        public List<WeeklySchedule> findAllSchedules() {
            return List.of(
                    schedule(301L, 201L, DayOfWeek.MONDAY, "12:00", "15:00", true),
                    schedule(306L, 204L, DayOfWeek.FRIDAY, "11:00", "15:00", true),
                    schedule(310L, 206L, DayOfWeek.MONDAY, "13:00", "15:00", false));
        }
    };

    private final SearchProfessionalsUseCaseImpl useCase = new SearchProfessionalsUseCaseImpl(reader);

    private static WeeklySchedule schedule(Long id, Long professionalId, DayOfWeek day,
                                           String start, String end, boolean enabled) {
        return new WeeklySchedule(id, professionalId, day, LocalTime.parse(start), LocalTime.parse(end),
                30, enabled, T, T);
    }

    private List<Long> ids(List<ProfessionalSearchResult> results) {
        return results.stream().map(r -> r.professional().id()).toList();
    }

    @Test
    void sinFiltrosDevuelveTodosOrdenadosPorApellido() {
        var results = useCase.search(new ProfessionalSearchCriteria(null, null, null, null, null));
        assertThat(ids(results)).containsExactly(204L, 206L, 201L); // Médina, Molina, Torres
    }

    @Test
    void filtraPorCategoria() {
        var results = useCase.search(new ProfessionalSearchCriteria(102L, null, null, null, null));
        assertThat(ids(results)).containsExactlyInAnyOrder(204L, 206L);
        assertThat(results.getFirst().category().name()).isEqualTo("Pediatria");
    }

    @Test
    void filtraPorNombreSinImportarMayusculasNiTildes() {
        var results = useCase.search(new ProfessionalSearchCriteria(null, "MEDINA", null, null, null));
        assertThat(ids(results)).containsExactly(204L);
    }

    @Test
    void filtraPorEstadoHabilitado() {
        var results = useCase.search(new ProfessionalSearchCriteria(null, null, true, null, null));
        assertThat(ids(results)).containsExactlyInAnyOrder(201L, 204L);
    }

    @Test
    void filtraPorDisponibilidadSegunDiaYHora() {
        var lunes = useCase.search(new ProfessionalSearchCriteria(null, null, null, DayOfWeek.MONDAY, null));
        assertThat(ids(lunes)).containsExactly(201L); // el horario del lunes de Molina está deshabilitado

        var lunes1230 = useCase.search(
                new ProfessionalSearchCriteria(null, null, null, DayOfWeek.MONDAY, LocalTime.of(12, 30)));
        assertThat(ids(lunes1230)).containsExactly(201L);

        var lunes1500 = useCase.search(
                new ProfessionalSearchCriteria(null, null, null, DayOfWeek.MONDAY, LocalTime.of(15, 0)));
        assertThat(lunes1500).isEmpty(); // a las 15:00 ya terminó la atención
    }

    @Test
    void combinaFiltros() {
        var results = useCase.search(new ProfessionalSearchCriteria(102L, null, true, DayOfWeek.FRIDAY, null));
        assertThat(ids(results)).containsExactly(204L);
    }

    @Test
    void horaSinDiaEsUnCriterioInvalido() {
        assertThatThrownBy(() -> new ProfessionalSearchCriteria(null, null, null, null, LocalTime.NOON))
                .isInstanceOf(InvalidSearchCriteriaException.class);
    }
}
