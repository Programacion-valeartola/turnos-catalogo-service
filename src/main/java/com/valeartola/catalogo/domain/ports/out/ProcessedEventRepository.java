package com.valeartola.catalogo.domain.ports.out;

/**
 * Puerto de salida: registro de los eventos ya procesados (idempotencia por eventId).
 */
public interface ProcessedEventRepository {

    boolean isProcessed(String eventId);

    /** Registra el evento como procesado. Registrarlo dos veces no produce error. */
    void markProcessed(String eventId, String eventType);
}