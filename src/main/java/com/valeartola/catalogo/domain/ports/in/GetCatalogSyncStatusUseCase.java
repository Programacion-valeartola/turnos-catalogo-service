package com.valeartola.catalogo.domain.ports.in;

import com.valeartola.catalogo.domain.model.CatalogSyncState;

/**
 * Puerto de entrada: consultar el estado persistido de la sincronización del catálogo.
 */
public interface GetCatalogSyncStatusUseCase {

    CatalogSyncState getStatus();
}