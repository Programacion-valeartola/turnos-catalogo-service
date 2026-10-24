package com.valeartola.catalogo.infrastructure.web;

import com.valeartola.catalogo.domain.model.ProfessionalSearchCriteria;
import com.valeartola.catalogo.domain.ports.in.SearchProfessionalsUseCase;
import com.valeartola.catalogo.infrastructure.web.dto.ProfessionalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Adaptador de entrada REST: búsqueda de profesionales para la aplicación KMP.
 * Ejemplo: GET /api/professionals?categoryId=101&enabled=true&dayOfWeek=MONDAY&time=12:30
 */
@RestController
@RequestMapping("/api/professionals")
@RequiredArgsConstructor
public class ProfessionalSearchController {

    private final SearchProfessionalsUseCase searchProfessionalsUseCase;

    @GetMapping
    public List<ProfessionalResponse> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) DayOfWeek dayOfWeek,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime time) {

        ProfessionalSearchCriteria criteria =
                new ProfessionalSearchCriteria(categoryId, name, enabled, dayOfWeek, time);

        return searchProfessionalsUseCase.search(criteria).stream()
                .map(ProfessionalResponse::from)
                .toList();
    }
}
