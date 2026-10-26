package com.valeartola.catalogo.domain.model;

import java.time.Instant;

/**
 * Ventana de versiones publicada por la cátedra en Redis.
 * currentVersion: última versión del catálogo.
 * oldestAvailableVersion: versión más antigua cuyos cambios todavía se conservan.
 */
public record CatalogVersionWindow(
        long currentVersion,
        long oldestAvailableVersion,
        Instant publishedAt
) {

    /** La copia local ya tiene la última versión publicada. */
    public boolean isUpToDate(long localVersion) {
        return localVersion == currentVersion;
    }

    /**
     * No se puede continuar de forma incremental: la versión local quedó fuera
     * de la ventana disponible, o es mayor que la publicada (estado no verificable).
     */
    public boolean requiresSnapshot(long localVersion) {
        return localVersion < oldestAvailableVersion || localVersion > currentVersion;
    }
}