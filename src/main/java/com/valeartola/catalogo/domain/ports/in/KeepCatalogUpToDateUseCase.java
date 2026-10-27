package com.valeartola.catalogo.domain.ports.in;

import com.valeartola.catalogo.domain.model.CatalogSyncOutcome;

/**
 * Puerto de entrada: verificar el estado del catálogo y ponerlo al día,
 * eligiendo entre sincronización incremental o snapshot completo.
 */
public interface KeepCatalogUpToDateUseCase {

    /** Nunca lanza excepciones: ante fallas reintenta y devuelve FAILED si no lo logra. */
    CatalogSyncOutcome synchronize();
}