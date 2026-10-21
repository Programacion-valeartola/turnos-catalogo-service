package com.valeartola.catalogo.domain.exception;

/**
 * Error al comunicarse con el servicio de la cátedra.
 * Las decisiones se toman por status y code, nunca por el texto del detalle.
 */
public class CatedraIntegrationException extends RuntimeException {

    public static final String UNAVAILABLE = "CATEDRA_UNAVAILABLE";
    public static final String UNEXPECTED_RESPONSE = "UNEXPECTED_RESPONSE";

    private final int status;
    private final String code;

    public CatedraIntegrationException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public CatedraIntegrationException(int status, String code, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
