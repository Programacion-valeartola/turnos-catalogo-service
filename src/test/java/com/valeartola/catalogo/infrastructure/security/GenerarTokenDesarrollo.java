package com.valeartola.catalogo.infrastructure.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Herramienta SOLO para pruebas manuales en Postman, mientras el servicio de turnos
 * todavía no emite tokens. Lee JWT_SECRET del .env e imprime dos tokens válidos por 24 h.
 * Se ejecuta desde IntelliJ con el botón ▶ junto al método main.
 */
public class GenerarTokenDesarrollo {

    public static void main(String[] args) throws Exception {
        String secret = Files.readAllLines(Path.of(".env")).stream()
                .filter(line -> line.startsWith("JWT_SECRET="))
                .map(line -> line.substring("JWT_SECRET=".length()).trim())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Falta JWT_SECRET en el archivo .env"));

        Instant now = Instant.now();
        Instant expires = now.plus(Duration.ofHours(24));

        System.out.println("Token de USUARIO (ROLE_USER, 24 h):");
        System.out.println(JwtTestTokens.create(secret, "usuario-prueba", "ROLE_USER", now, expires));
        System.out.println();
        System.out.println("Token de SERVICIO (ROLE_SERVICE, 24 h):");
        System.out.println(JwtTestTokens.create(secret, "turnos-service", "ROLE_SERVICE", now, expires));
    }
}
