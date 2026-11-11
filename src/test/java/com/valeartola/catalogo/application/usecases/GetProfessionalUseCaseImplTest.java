package com.valeartola.catalogo.application.usecases;

import com.valeartola.catalogo.domain.exception.ProfessionalNotFoundException;
import com.valeartola.catalogo.domain.model.*;
import com.valeartola.catalogo.domain.ports.out.CatalogReader;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetProfessionalUseCaseImplTest {

    private static final Instant T = Instant.parse("2026-07-01T12:00:00Z");

    /** Copia local falsa: dos profesionales, uno deshabilitado. */
    private final CatalogReader reader = new CatalogReader() {
        @Override
        public List<ProfessionalCategory> findAllCategories() {
            return List.of(new ProfessionalCategory(101L, "Clinica medica", "General", true, T, T));
        }

        @Override
        public List<Professional> findAllProfessionals() {
            return List.of(new Professional(215L, 101L, "Ana", "Gómez", true, T, T),
                    new Professional(216L, 101L, "Luis", "Paz", false, T, T));
        }

        @Override
        public List<WeeklySchedule> findAllSchedules() {
            return List.of(new WeeklySchedule(301L, 215L, DayOfWeek.MONDAY,
                    LocalTime.of(9, 0), LocalTime.of(12, 0), 30, true, T, T));
        }
    };

    private final GetProfessionalUseCaseImpl useCase = new GetProfessionalUseCaseImpl(reader);

    @Test
    void devuelveElProfesionalConSuCategoriaYSusHorarios() {
        ProfessionalSearchResult result = useCase.getById(215L);

        assertThat(result.professional().lastName()).isEqualTo("Gómez");
        assertThat(result.category().name()).isEqualTo("Clinica medica");
        assertThat(result.schedules()).extracting(WeeklySchedule::id).containsExactly(301L);
    }

    @Test
    void unProfesionalDeshabilitadoSeDevuelveIndicandoloExplicitamente() {
        ProfessionalSearchResult result = useCase.getById(216L);

        assertThat(result.professional().enabled()).isFalse();
        assertThat(result.schedules()).isEmpty();
    }

    @Test
    void unProfesionalInexistenteLanzaElErrorDelDominio() {
        assertThatThrownBy(() -> useCase.getById(999L))
                .isInstanceOf(ProfessionalNotFoundException.class)
                .hasMessageContaining("999");
    }
}