package com.valeartola.catalogo.domain.ports.in;

import com.valeartola.catalogo.domain.model.ProfessionalSearchResult;

/**
 * Puerto de entrada: obtener un profesional con su categoría y sus horarios,
 * desde la copia local. Lo usa el servicio de turnos antes de cada operación.
 */
public interface GetProfessionalUseCase {

    /** @throws com.valeartola.catalogo.domain.exception.ProfessionalNotFoundException si no existe */
    ProfessionalSearchResult getById(Long id);
}