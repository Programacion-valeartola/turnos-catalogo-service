package com.valeartola.catalogo.domain.model;

import java.time.Instant;

/**
 * Profesional del catálogo. Pertenece a una categoría (categoryId).
 * Un profesional deshabilitado (enabled = false) es una baja lógica.
 */
public record Professional(
        Long id,
        Long categoryId,
        String firstName,
        String lastName,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {

    public String fullName() {
        return firstName + " " + lastName;
    }
}
