package com.valeartola.catalogo.infrastructure.web;

import com.valeartola.catalogo.infrastructure.security.JwtTestTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"catalog.sync.on-startup=false", "catalog.sync.kafka-enabled=false"})
class CatalogSyncStatusControllerTest {

    @Value("${local.server.port}")
    private int port;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private RestClient client() {
        return RestClient.create("http://localhost:" + port);
    }

    private String userToken() {
        Instant now = Instant.now();
        return JwtTestTokens.create(jwtSecret, "usuario-test", "ROLE_USER", now, now.plus(Duration.ofHours(1)));
    }

    @Test
    void sinTokenResponde401() {
        int status = client().get().uri("/api/catalog/sync-status")
                .exchange((request, response) -> response.getStatusCode().value());

        assertThat(status).isEqualTo(401);
    }

    @Test
    void conTokenValidoDevuelveElEstado() {
        String body = client().get().uri("/api/catalog/sync-status")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken())
                .retrieve()
                .body(String.class);

        assertThat(body).contains("\"initialized\"", "\"localVersion\"", "\"lastSyncType\"", "\"lastError\"");
    }
}