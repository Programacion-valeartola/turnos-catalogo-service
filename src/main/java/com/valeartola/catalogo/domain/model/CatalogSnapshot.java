package com.valeartola.catalogo.domain.model;

import java.time.Instant;
import java.util.List;

/**
 * Copia completa del catálogo publicada por la cátedra en una versión determinada.
 */
public record CatalogSnapshot(
        long version,
        Instant generatedAt,
        List<ProfessionalCategory> categories,
        List<Professional> professionals,
        List<WeeklySchedule> schedules
) {
}
