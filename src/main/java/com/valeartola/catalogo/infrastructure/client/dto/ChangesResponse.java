package com.valeartola.catalogo.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.valeartola.catalogo.domain.model.CatalogChangeSet;

import java.util.List;

/**
 * JSON guardado en catedra:sync:changes:{version}.
 * Ejemplo: {"version":7,"changes":{"professionalCategories":[],"professionals":[15],"weeklySchedules":[]}}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChangesResponse(Long version, Changes changes) {

    public CatalogChangeSet toDomain() {
        return new CatalogChangeSet(
                version,
                orEmpty(changes.professionalCategories()),
                orEmpty(changes.professionals()),
                orEmpty(changes.weeklySchedules()));
    }

    private static List<Long> orEmpty(List<Long> ids) {
        return ids == null ? List.of() : ids;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Changes(List<Long> professionalCategories,
                          List<Long> professionals,
                          List<Long> weeklySchedules) {
    }
}