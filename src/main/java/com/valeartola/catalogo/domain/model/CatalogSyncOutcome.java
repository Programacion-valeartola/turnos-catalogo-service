package com.valeartola.catalogo.domain.model;

/**
 * Resultado de una verificación de sincronización del catálogo.
 */
public enum CatalogSyncOutcome {
    /** La copia local ya estaba en la última versión. */
    UP_TO_DATE,
    /** Se aplicaron una o más versiones incrementales. */
    INCREMENTAL,
    /** Se reconstruyó la copia local con un snapshot completo. */
    SNAPSHOT,
    /** No se pudo sincronizar tras agotar los reintentos. */
    FAILED
}