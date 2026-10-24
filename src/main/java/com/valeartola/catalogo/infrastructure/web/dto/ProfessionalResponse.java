package com.valeartola.catalogo.infrastructure.web.dto;

import com.valeartola.catalogo.domain.model.ProfessionalCategory;
import com.valeartola.catalogo.domain.model.ProfessionalSearchResult;
import com.valeartola.catalogo.domain.model.WeeklySchedule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

/**
 * JSON que devuelve la búsqueda de profesionales a la aplicación KMP.
 */
public record ProfessionalResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        boolean enabled,
        CategoryResponse category,
        List<ScheduleResponse> schedules
) {

    public record CategoryResponse(Long id, String name) {
    }

    public record ScheduleResponse(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
                                   int slotDurationMinutes, boolean enabled) {
    }

    public static ProfessionalResponse from(ProfessionalSearchResult result) {
        ProfessionalCategory category = result.category();
        return new ProfessionalResponse(
                result.professional().id(),
                result.professional().firstName(),
                result.professional().lastName(),
                result.professional().fullName(),
                result.professional().enabled(),
                category == null ? null : new CategoryResponse(category.id(), category.name()),
                result.schedules().stream()
                        .sorted(Comparator.comparing(WeeklySchedule::dayOfWeek)
                                .thenComparing(WeeklySchedule::startTime))
                        .map(s -> new ScheduleResponse(s.dayOfWeek(), s.startTime(), s.endTime(),
                                s.slotDurationMinutes(), s.enabled()))
                        .toList());
    }
}
