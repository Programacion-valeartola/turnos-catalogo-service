package com.valeartola.catalogo.domain.ports.in;

/**
 * Puerto de entrada: poner al día la copia local con los cambios incrementales publicados.
 */
public interface ApplyCatalogChangesUseCase {

    /**
     * Aplica, en orden, todas las versiones publicadas posteriores a la local.
     * @return cantidad de versiones aplicadas (0 si ya estaba al día)
     * @throws com.valeartola.catalogo.domain.exception.CatalogResyncRequiredException
     *         si no se puede continuar de forma incremental y hace falta un snapshot
     */
    int applyPendingVersions();
}