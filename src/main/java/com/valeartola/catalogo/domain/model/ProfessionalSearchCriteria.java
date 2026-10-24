package com.valeartola.catalogo.domain.model;

import com.valeartola.catalogo.domain.exception.InvalidSearchCriteriaException;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Filtros de la búsqueda de profesionales. Todos son opcionales (null = sin filtrar).
 *
 * @param categoryId solo profesionales de esa categoría
 * @param name       texto contenido en el nombre o el apellido (sin importar mayúsculas ni tildes)
 * @param enabled    true = solo habilitados, false = solo deshabilitados
 * @param dayOfWeek  disponibilidad: atiende ese día de la semana
 * @param time       disponibilidad: atiende a esa hora (requiere dayOfWeek)
 */
public record ProfessionalSearchCriteria(
        Long categoryId,
        String name,
        Boolean enabled,
        DayOfWeek dayOfWeek,
        LocalTime time
) {

    public ProfessionalSearchCriteria {
        if (time != null && dayOfWeek == null) {
            throw new InvalidSearchCriteriaException("El filtro 'time' requiere indicar 'dayOfWeek'");
        }
        if (name != null && name.isBlank()) {
            name = null;
        }
    }
}
