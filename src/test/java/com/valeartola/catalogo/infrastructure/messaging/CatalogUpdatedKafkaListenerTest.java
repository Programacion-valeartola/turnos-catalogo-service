package com.valeartola.catalogo.infrastructure.messaging;

import com.valeartola.catalogo.domain.model.CatalogUpdatedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Prueba el listener sin Kafka real: se le pasan registros armados a mano.
 */
class CatalogUpdatedKafkaListenerTest {

    private final List<CatalogUpdatedEvent> handled = new ArrayList<>();
    private final CatalogUpdatedKafkaListener listener =
            new CatalogUpdatedKafkaListener(handled::add, JsonMapper.builder().build());

    private static ConsumerRecord<String, String> record(String value) {
        return new ConsumerRecord<>("catedra.catalog.proyecto-artola", 0, 42L, "7", value);
    }

    @Test
    void procesaUnAvisoValidoIgnorandoCamposDesconocidos() {
        listener.onMessage(record("""
                {"eventId":"catalog-event-7-4f3b","eventType":"CatalogUpdated","newVersion":7,
                 "occurredAt":"2026-09-10T13:44:59Z","source":"MANUAL","schemaVersion":1,"campoNuevo":"x"}
                """));

        assertThat(handled).hasSize(1);
        assertThat(handled.getFirst().eventId()).isEqualTo("catalog-event-7-4f3b");
        assertThat(handled.getFirst().newVersion()).isEqualTo(7);
    }

    @Test
    void unJsonMalformadoSeDescartaSinLanzarExcepcion() {
        assertThatCode(() -> listener.onMessage(record("{esto no es json")))
                .doesNotThrowAnyException();
        assertThat(handled).isEmpty();
    }

    @Test
    void unEventoDeOtroTipoSeDescarta() {
        listener.onMessage(record("""
                {"eventId":"x-1","eventType":"OtroEvento","newVersion":7}
                """));

        assertThat(handled).isEmpty();
    }

    @Test
    void unAvisoSinEventIdSeDescarta() {
        listener.onMessage(record("""
                {"eventType":"CatalogUpdated","newVersion":7}
                """));

        assertThat(handled).isEmpty();
    }

    @Test
    void despuesDeUnMensajeMaloSigueProcesandoLosSiguientes() {
        listener.onMessage(record("basura"));
        listener.onMessage(record("""
                {"eventId":"catalog-event-8","eventType":"CatalogUpdated","newVersion":8}
                """));

        assertThat(handled).extracting(CatalogUpdatedEvent::newVersion).containsExactly(8L);
    }
}