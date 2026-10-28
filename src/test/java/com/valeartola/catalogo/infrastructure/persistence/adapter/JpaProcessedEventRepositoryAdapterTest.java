package com.valeartola.catalogo.infrastructure.persistence.adapter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Prueba contra el PostgreSQL de Docker; cada test se deshace al terminar.
 */
@SpringBootTest(properties = {"catalog.sync.on-startup=false", "catalog.sync.kafka-enabled=false"})
@Transactional
class JpaProcessedEventRepositoryAdapterTest {

    @Autowired
    private JpaProcessedEventRepositoryAdapter adapter;

    @Test
    void unEventoRegistradoQuedaComoProcesado() {
        assertThat(adapter.isProcessed("test-event-1")).isFalse();

        adapter.markProcessed("test-event-1", "CatalogUpdated");

        assertThat(adapter.isProcessed("test-event-1")).isTrue();
    }

    @Test
    void registrarDosVecesElMismoEventoNoFalla() {
        adapter.markProcessed("test-event-2", "CatalogUpdated");

        assertThatCode(() -> adapter.markProcessed("test-event-2", "CatalogUpdated"))
                .doesNotThrowAnyException();
        assertThat(adapter.isProcessed("test-event-2")).isTrue();
    }
}