package com.valeartola.catalogo.domain.ports.in;

import com.valeartola.catalogo.domain.model.ProfessionalSearchCriteria;
import com.valeartola.catalogo.domain.model.ProfessionalSearchResult;

import java.util.List;

/**
 * Puerto de entrada: búsqueda de profesionales sobre la copia local.
 */
public interface SearchProfessionalsUseCase {

    List<ProfessionalSearchResult> search(ProfessionalSearchCriteria criteria);
}
