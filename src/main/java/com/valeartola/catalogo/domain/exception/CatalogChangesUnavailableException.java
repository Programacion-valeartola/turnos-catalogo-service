package com.valeartola.catalogo.domain.exception;

/**
 * No se pudieron obtener los cambios incrementales del catálogo
 * (Redis no disponible, datos faltantes o con formato inesperado).
 */
public class CatalogChangesUnavailableException extends RuntimeException {

    public CatalogChangesUnavailableException(String message) {
        super(message);
    }

    public CatalogChangesUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}