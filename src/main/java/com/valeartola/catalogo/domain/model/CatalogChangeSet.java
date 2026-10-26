package com.valeartola.catalogo.domain.model;

import java.util.List;

/**
 * Cambios de una versión del catálogo: solo los IDs afectados.
 * El estado vigente de cada entidad se lee por separado.
 */
public record CatalogChangeSet(
        long version,
        List<Long> categoryIds,
        List<Long> professionalIds,
        List<Long> scheduleIds
) {

    public boolean isEmpty() {
        return categoryIds.isEmpty() && professionalIds.isEmpty() && scheduleIds.isEmpty();
    }
}