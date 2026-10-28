package com.valeartola.catalogo.infrastructure.web.dto;

import com.valeartola.catalogo.domain.model.CatalogSyncState;

import java.time.Instant;

/**
 * Respuesta de GET /api/catalog/sync-status.
 * initialized = false indica que todavía no se aplicó ningún snapshot.
 */
public record SyncStatusResponse(
        boolean initialized,
        Long localVersion,
        Instant lastSyncAt,
        String lastSyncType,
        String lastError
) {

    public static SyncStatusResponse from(CatalogSyncState state) {
        return new SyncStatusResponse(
                !state.requiresSnapshot(),
                state.localVersion(),
                state.lastSyncAt(),
                state.lastSyncType() == null ? null : state.lastSyncType().name(),
                state.lastError());
    }
}