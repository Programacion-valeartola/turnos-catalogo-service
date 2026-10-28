package com.valeartola.catalogo.domain.ports.in;

import com.valeartola.catalogo.domain.model.CatalogUpdatedEvent;

/**
 * Puerto de entrada: qué hacer cuando llega un aviso de nueva versión del catálogo.
 */
public interface HandleCatalogUpdatedUseCase {

    void handle(CatalogUpdatedEvent event);
}