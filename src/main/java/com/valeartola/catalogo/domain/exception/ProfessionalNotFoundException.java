package com.valeartola.catalogo.domain.exception;

/** El profesional pedido no existe en la copia local del catálogo. */
public class ProfessionalNotFoundException extends RuntimeException {

    public static final String CODE = "PROFESSIONAL_NOT_FOUND";

    public ProfessionalNotFoundException(Long id) {
        super("No existe el profesional " + id);
    }
}
