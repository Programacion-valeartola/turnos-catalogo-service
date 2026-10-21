package com.valeartola.catalogo.domain.model;

import java.time.Instant;

/**
 * Estado de la copia local del catálogo: qué versión está aplicada
 * y cómo terminó la última sincronización.
 * Si localVersion es null, la base está vacía y se necesita un snapshot.
 */
public record CatalogSyncState(
        Long localVersion,
        Instant lastSyncAt,
        SyncType lastSyncType,
        String lastError
) {

    public boolean requiresSnapshot() {
        return localVersion == null;
    }
}
