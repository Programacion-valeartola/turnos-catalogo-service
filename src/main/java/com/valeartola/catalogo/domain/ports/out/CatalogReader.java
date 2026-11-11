package com.valeartola.catalogo.domain.ports.out;

import com.valeartola.catalogo.domain.model.Professional;
import com.valeartola.catalogo.domain.model.ProfessionalCategory;
import com.valeartola.catalogo.domain.model.WeeklySchedule;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida de solo lectura sobre la copia local del catálogo.
 */
public interface CatalogReader {

    List<ProfessionalCategory> findAllCategories();

    List<Professional> findAllProfessionals();

    List<WeeklySchedule> findAllSchedules();

    /** Un profesional por ID. Las implementaciones pueden resolverlo con una consulta directa. */
    default Optional<Professional> findProfessionalById(Long id) {
        return findAllProfessionals().stream().filter(p -> p.id().equals(id)).findFirst();
    }

    default Optional<ProfessionalCategory> findCategoryById(Long id) {
        return findAllCategories().stream().filter(c -> c.id().equals(id)).findFirst();
    }

    default List<WeeklySchedule> findSchedulesByProfessionalId(Long professionalId) {
        return findAllSchedules().stream().filter(s -> s.professionalId().equals(professionalId)).toList();
    }
}