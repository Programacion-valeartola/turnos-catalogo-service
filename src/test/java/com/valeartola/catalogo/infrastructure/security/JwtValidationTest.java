package com.valeartola.catalogo.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifica que el catálogo acepte solo tokens con firma válida y no vencidos,
 * y que lea correctamente los roles.
 */
class JwtValidationTest {

    // Claves de prueba (64 bytes en Base64). No son las reales del .env.
    private static final String SECRET =
            "dGVzdC1zZWNyZXQtcGFyYS1wcnVlYmFzLWRlbC1jYXRhbG9nby0xMjM0NTY3ODkwMTIzNDU2Nzg5MA==";
    private static final String OTHER_SECRET =
            "b3RyYS1jbGF2ZS1kaXN0aW50YS1xdWUtbm8tZGViZXJpYS1zZXItYWNlcHRhZGEtcG9yLWVsLXNlcnZp";

    private final JwtDecoder decoder = new SecurityConfig().jwtDecoder(SECRET);

    @Test
    void aceptaUnTokenValidoYLeeElRolDeUsuario() {
        Instant now = Instant.now();
        String token = JwtTestTokens.create(SECRET, "ana", "ROLE_USER", now, now.plus(Duration.ofHours(1)));

        Jwt jwt = decoder.decode(token);
        var authorities = SecurityConfig.jwtAuthenticationConverter().convert(jwt).getAuthorities();

        assertThat(jwt.getSubject()).isEqualTo("ana");
        assertThat(authorities).extracting(GrantedAuthority::getAuthority).contains("ROLE_USER");
    }

    @Test
    void leeElRolDeServicio() {
        Instant now = Instant.now();
        String token = JwtTestTokens.create(SECRET, "turnos-service", "ROLE_SERVICE", now, now.plus(Duration.ofHours(1)));

        var authorities = SecurityConfig.jwtAuthenticationConverter().convert(decoder.decode(token)).getAuthorities();

        assertThat(authorities).extracting(GrantedAuthority::getAuthority).contains("ROLE_SERVICE");
    }

    @Test
    void rechazaUnTokenFirmadoConOtraClave() {
        Instant now = Instant.now();
        String token = JwtTestTokens.create(OTHER_SECRET, "intruso", "ROLE_USER", now, now.plus(Duration.ofHours(1)));

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rechazaUnTokenVencido() {
        Instant past = Instant.now().minus(Duration.ofHours(2));
        String token = JwtTestTokens.create(SECRET, "ana", "ROLE_USER", past, past.plus(Duration.ofMinutes(30)));

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rechazaUnaClaveDemasiadoCorta() {
        assertThatThrownBy(() -> JwtSecretKeys.fromBase64("Y29ydGE="))
                .isInstanceOf(IllegalStateException.class);
    }
}
