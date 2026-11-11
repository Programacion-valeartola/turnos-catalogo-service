package com.valeartola.catalogo.infrastructure.web;

import com.valeartola.catalogo.infrastructure.security.JwtTestTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"catalog.sync.on-startup=false", "catalog.sync.kafka-enabled=false"})
class ProfessionalByIdEndpointTest {

    record Response(int status, String body) {
    }

    @Value("${local.server.port}")
    private int port;

    @Value("${jwt.secret}")
    private String jwtSecret;

    /** Token como el que emite el servicio de turnos para hablar con el catálogo. */
    private String serviceToken() {
        Instant now = Instant.now();
        return JwtTestTokens.create(jwtSecret, "turnos-service", "ROLE_SERVICE", now, now.plus(Duration.ofMinutes(5)));
    }

    private Response get(String path, String token) {
        var request = RestClient.create("http://localhost:" + port).get().uri(path);
        if (token != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return request.exchange((req, res) -> new Response(res.getStatusCode().value(),
                new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8)));
    }

    @Test
    void conTokenDeServicioDevuelveElProfesionalConSusHorarios() {
        Response response = get("/api/professionals/201", serviceToken());

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.body()).contains("\"id\":201", "\"enabled\"", "\"schedules\"");
    }

    @Test
    void unProfesionalInexistenteDevuelve404ConCode() {
        Response response = get("/api/professionals/999999", serviceToken());

        assertThat(response.status()).isEqualTo(404);
        assertThat(response.body()).contains("PROFESSIONAL_NOT_FOUND");
    }

    @Test
    void sinTokenDevuelve401() {
        assertThat(get("/api/professionals/201", null).status()).isEqualTo(401);
    }
}