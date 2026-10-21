package com.valeartola.catalogo.domain.model;

import java.time.Instant;

/**
 * Categoría o especialidad profesional del catálogo (por ejemplo, "Cardiologia").
 * El id coincide con el asignado por la cátedra.
 */
public record ProfessionalCategory(
        Long id,
        String name,
        String description,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
