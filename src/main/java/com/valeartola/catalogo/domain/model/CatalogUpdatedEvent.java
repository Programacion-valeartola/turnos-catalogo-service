package com.valeartola.catalogo.domain.model;

import java.time.Instant;

/**
 * Aviso de la cátedra: hay una nueva versión del catálogo.
 * Es solo una notificación; los datos se obtienen de Redis o del snapshot.
 */
public record CatalogUpdatedEvent(
        String eventId,
        long newVersion,
        Instant occurredAt,
        String source
) {

    public static final String TYPE = "CatalogUpdated";   // ← NUEVO
}