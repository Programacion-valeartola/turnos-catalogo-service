package com.valeartola.catalogo.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Cliente HTTP hacia la API REST de la cátedra.
 * La URL, el JWT técnico y los timeouts se leen de la configuración (.env).
 */
@Configuration
public class CatedraClientConfig {

    @Bean
    public RestClient catedraRestClient(
            @Value("${catedra.api.base-url}") String baseUrl,
            @Value("${catedra.api.token}") String token,
            @Value("${catedra.api.connect-timeout-seconds:5}") int connectTimeoutSeconds,
            @Value("${catedra.api.read-timeout-seconds:30}") int readTimeoutSeconds) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds));
        requestFactory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
    }
}
