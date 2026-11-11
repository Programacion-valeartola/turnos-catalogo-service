package com.valeartola.catalogo.application.usecases;

import com.valeartola.catalogo.domain.exception.ProfessionalNotFoundException;
import com.valeartola.catalogo.domain.model.Professional;
import com.valeartola.catalogo.domain.model.ProfessionalSearchResult;
import com.valeartola.catalogo.domain.ports.in.GetProfessionalUseCase;
import com.valeartola.catalogo.domain.ports.out.CatalogReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Devuelve un profesional con su categoría y sus horarios, leídos de la copia local.
 * Un profesional deshabilitado se devuelve igual (enabled = false): quien consulta
 * decide qué hacer, y la respuesta lo informa de forma explícita.
 */
@Component
@RequiredArgsConstructor
public class GetProfessionalUseCaseImpl implements GetProfessionalUseCase {

    private final CatalogReader catalogReader;

    @Override
    public ProfessionalSearchResult getById(Long id) {
        Professional professional = catalogReader.findProfessionalById(id)
                .orElseThrow(() -> new ProfessionalNotFoundException(id));

        return new ProfessionalSearchResult(
                professional,
                catalogReader.findCategoryById(professional.categoryId()).orElse(null),
                catalogReader.findSchedulesByProfessionalId(id));
    }
}