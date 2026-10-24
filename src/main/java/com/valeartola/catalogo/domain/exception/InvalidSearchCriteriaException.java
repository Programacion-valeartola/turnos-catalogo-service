package com.valeartola.catalogo.domain.exception;

/**
 * Los filtros de búsqueda recibidos no son válidos (por ejemplo, hora sin día).
 */
public class InvalidSearchCriteriaException extends RuntimeException {

    public InvalidSearchCriteriaException(String message) {
        super(message);
    }
}
