package com.valeartola.catalogo.domain.ports.out;

import com.valeartola.catalogo.domain.model.CatalogIncrementalUpdate;
import com.valeartola.catalogo.domain.model.CatalogSnapshot;
import com.valeartola.catalogo.domain.model.CatalogSyncState;

/**
 * Puerto de salida: donde se guarda la copia local del catálogo.
 * El dominio no sabe que detrás hay PostgreSQL y JPA.
 */
public interface CatalogRepository {

    /** Devuelve en qué versión está la copia local y cómo terminó la última sincronización. */
    CatalogSyncState getSyncState();

    /**
     * Reemplaza toda la copia local por el snapshot recibido y registra su versión.
     * Debe ejecutarse como una única unidad: se aplica todo o nada.
     */
    void replaceWithSnapshot(CatalogSnapshot snapshot);

    /** Registra el error de la última sincronización fallida, sin modificar los datos. */
    void recordSyncError(String errorMessage);
    /**
     * Aplica los cambios de UNA versión y avanza la versión local, en una única transacción.
     * Si la versión ya fue aplicada, no hace nada (reprocesarla no altera el resultado).
     * Solo acepta la versión siguiente a la local; si no, lanza CatalogResyncRequiredException.
     */
    void applyIncrementalUpdate(CatalogIncrementalUpdate update);
}
