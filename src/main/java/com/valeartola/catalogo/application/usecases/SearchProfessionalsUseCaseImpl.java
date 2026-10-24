package com.valeartola.catalogo.application.usecases;

import com.valeartola.catalogo.domain.model.Professional;
import com.valeartola.catalogo.domain.model.ProfessionalCategory;
import com.valeartola.catalogo.domain.model.ProfessionalSearchCriteria;
import com.valeartola.catalogo.domain.model.ProfessionalSearchResult;
import com.valeartola.catalogo.domain.model.WeeklySchedule;
import com.valeartola.catalogo.domain.ports.in.SearchProfessionalsUseCase;
import com.valeartola.catalogo.domain.ports.out.CatalogReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Caso de uso: busca profesionales aplicando los filtros sobre la copia local.
 * Nunca consulta a la cátedra.
 */
@Component
@RequiredArgsConstructor
public class SearchProfessionalsUseCaseImpl implements SearchProfessionalsUseCase {

    private final CatalogReader catalogReader;

    @Override
    public List<ProfessionalSearchResult> search(ProfessionalSearchCriteria criteria) {
        Map<Long, ProfessionalCategory> categoriesById = catalogReader.findAllCategories().stream()
                .collect(Collectors.toMap(ProfessionalCategory::id, Function.identity()));

        Map<Long, List<WeeklySchedule>> schedulesByProfessional = catalogReader.findAllSchedules().stream()
                .collect(Collectors.groupingBy(WeeklySchedule::professionalId));

        return catalogReader.findAllProfessionals().stream()
                .filter(p -> matchesCategory(p, criteria))
                .filter(p -> matchesName(p, criteria))
                .filter(p -> matchesEnabled(p, criteria))
                .filter(p -> matchesAvailability(schedulesByProfessional.getOrDefault(p.id(), List.of()), criteria))
                .sorted(Comparator.comparing((Professional p) -> normalize(p.lastName()))
                        .thenComparing(p -> normalize(p.firstName())))
                .map(p -> new ProfessionalSearchResult(
                        p,
                        categoriesById.get(p.categoryId()),
                        schedulesByProfessional.getOrDefault(p.id(), List.of())))
                .toList();
    }

    private boolean matchesCategory(Professional p, ProfessionalSearchCriteria c) {
        return c.categoryId() == null || c.categoryId().equals(p.categoryId());
    }

    private boolean matchesName(Professional p, ProfessionalSearchCriteria c) {
        return c.name() == null || normalize(p.fullName()).contains(normalize(c.name()));
    }

    private boolean matchesEnabled(Professional p, ProfessionalSearchCriteria c) {
        return c.enabled() == null || c.enabled() == p.enabled();
    }

    /** Disponible = tiene un horario habilitado ese día y, si se indicó hora, la cubre. */
    private boolean matchesAvailability(List<WeeklySchedule> schedules, ProfessionalSearchCriteria c) {
        if (c.dayOfWeek() == null) {
            return true;
        }
        return schedules.stream()
                .filter(WeeklySchedule::enabled)
                .filter(s -> s.dayOfWeek() == c.dayOfWeek())
                .anyMatch(s -> c.time() == null
                        || (!c.time().isBefore(s.startTime()) && c.time().isBefore(s.endTime())));
    }

    /** Pasa a minúsculas y quita tildes: "Pérez" -> "perez". */
    private static String normalize(String text) {
        String withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT).trim();
    }
}
