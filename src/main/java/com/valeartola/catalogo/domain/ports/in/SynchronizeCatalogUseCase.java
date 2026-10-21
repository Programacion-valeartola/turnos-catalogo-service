package com.valeartola.catalogo.domain.ports.in;

import com.valeartola.catalogo.domain.model.CatalogSyncState;

/**
 * Puerto de entrada: lo que el servicio sabe hacer respecto de la sincronización.
 */
public interface SynchronizeCatalogUseCase {

    /**
     * Si la base local está vacía, la inicializa con un snapshot completo.
     * @return true si se aplicó un snapshot, false si la base ya tenía datos
     */
    boolean synchronizeIfEmpty();

    /** Reemplaza la copia local con un snapshot completo de la cátedra. */
    CatalogSyncState applySnapshot();
}
