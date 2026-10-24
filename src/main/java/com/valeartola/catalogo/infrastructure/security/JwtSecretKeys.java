package com.valeartola.catalogo.infrastructure.security;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Convierte la clave secreta compartida (en Base64, leída del .env) en una clave HMAC-SHA256.
 */
public final class JwtSecretKeys {

    private static final int MIN_BYTES = 32; // HS256 requiere al menos 256 bits

    private JwtSecretKeys() {
    }

    public static SecretKey fromBase64(String base64Secret) {
        if (base64Secret == null || base64Secret.isBlank()) {
            throw new IllegalStateException("Falta configurar JWT_SECRET");
        }
        byte[] bytes = Base64.getDecoder().decode(base64Secret.trim());
        if (bytes.length < MIN_BYTES) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 256 bits (32 bytes)");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}
