package com.valeartola.catalogo.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.valeartola.catalogo.domain.model.CatalogUpdatedEvent;

import java.time.Instant;

/**
 * JSON publicado por la cátedra en el tópico del catálogo (contrato v1, sección 15.3).
 * Los campos desconocidos se ignoran, como exige el contrato.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogUpdatedMessage(
        String eventId,
        String eventType,
        Long newVersion,
        Instant occurredAt,
        String source,
        Integer schemaVersion
) {

    public static final String EVENT_TYPE = "CatalogUpdated";

    /** El mensaje tiene lo mínimo indispensable para procesarlo. */
    public boolean isValid() {
        return EVENT_TYPE.equals(eventType)
                && eventId != null && !eventId.isBlank()
                && newVersion != null && newVersion > 0;
    }

    public CatalogUpdatedEvent toDomain() {
        return new CatalogUpdatedEvent(eventId, newVersion, occurredAt, source);
    }
}