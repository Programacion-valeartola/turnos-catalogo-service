package com.valeartola.catalogo.domain.ports.out;

import com.valeartola.catalogo.domain.model.Professional;
import com.valeartola.catalogo.domain.model.ProfessionalCategory;
import com.valeartola.catalogo.domain.model.WeeklySchedule;

import java.util.List;

/**
 * Puerto de salida de solo lectura sobre la copia local del catálogo.
 */
public interface CatalogReader {

    List<ProfessionalCategory> findAllCategories();

    List<Professional> findAllProfessionals();

    List<WeeklySchedule> findAllSchedules();
}
