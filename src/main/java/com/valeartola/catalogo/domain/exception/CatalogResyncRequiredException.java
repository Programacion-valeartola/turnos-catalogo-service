package com.valeartola.catalogo.domain.exception;

/**
 * No se puede continuar la sincronización incremental de forma segura:
 * la copia local debe reconstruirse con un snapshot completo.
 */
public class CatalogResyncRequiredException extends RuntimeException {

    public CatalogResyncRequiredException(String message) {
        super(message);
    }
}
