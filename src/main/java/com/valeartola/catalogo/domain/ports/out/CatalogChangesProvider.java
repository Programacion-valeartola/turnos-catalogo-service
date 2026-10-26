package com.valeartola.catalogo.domain.ports.out;

import com.valeartola.catalogo.domain.model.CatalogChangeSet;
import com.valeartola.catalogo.domain.model.CatalogVersionWindow;
import com.valeartola.catalogo.domain.model.Professional;
import com.valeartola.catalogo.domain.model.ProfessionalCategory;
import com.valeartola.catalogo.domain.model.WeeklySchedule;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: fuente de los cambios incrementales del catálogo.
 * Todas las operaciones lanzan CatalogChangesUnavailableException si la fuente
 * no responde o devuelve datos inválidos.
 */
public interface CatalogChangesProvider {

    CatalogVersionWindow fetchVersionWindow();

    /** Vacío si la versión ya no se conserva (o todavía no existe). */
    Optional<CatalogChangeSet> fetchChanges(long version);

    /** Estado vigente de las entidades pedidas. Falla si falta alguna. */
    List<ProfessionalCategory> fetchCategories(Collection<Long> ids);

    List<Professional> fetchProfessionals(Collection<Long> ids);

    List<WeeklySchedule> fetchSchedules(Collection<Long> ids);
}