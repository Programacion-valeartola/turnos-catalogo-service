package com.valeartola.catalogo.domain.model;

import java.util.List;

/**
 * Cambios de UNA versión del catálogo, con el estado vigente de cada entidad afectada.
 * Se aplica como una unidad: o se guarda todo y la versión avanza, o no cambia nada.
 */
public record CatalogIncrementalUpdate(
        long version,
        List<ProfessionalCategory> categories,
        List<Professional> professionals,
        List<WeeklySchedule> schedules
) {
}
