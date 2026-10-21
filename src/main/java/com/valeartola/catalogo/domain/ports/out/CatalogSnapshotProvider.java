package com.valeartola.catalogo.domain.ports.out;

import com.valeartola.catalogo.domain.model.CatalogSnapshot;

/**
 * Puerto de salida: obtiene el catálogo completo desde la fuente autoritativa.
 * El dominio no sabe si los datos vienen de REST, de un archivo o de una prueba.
 */
public interface CatalogSnapshotProvider {

    CatalogSnapshot fetchSnapshot();
}
