package com.valeartola.catalogo.domain.model;

import java.util.List;

/**
 * Un profesional encontrado, junto con su categoría y sus horarios semanales.
 */
public record ProfessionalSearchResult(
        Professional professional,
        ProfessionalCategory category,
        List<WeeklySchedule> schedules
) {
}
