package com.valeartola.catalogo.infrastructure.messaging;

import com.valeartola.catalogo.domain.ports.in.HandleCatalogUpdatedUseCase;
import com.valeartola.catalogo.infrastructure.messaging.dto.CatalogUpdatedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Adaptador de entrada: escucha el tópico del catálogo asignado por la cátedra.
 * El valor se recibe como texto y se convierte acá: así un mensaje malformado
 * se registra y se descarta sin trabar el consumo de los siguientes.
 * El offset se confirma recién cuando este método termina (ack-mode: record).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogUpdatedKafkaListener {

    private final HandleCatalogUpdatedUseCase handleCatalogUpdated;
    private final JsonMapper jsonMapper;

    @KafkaListener(topics = "${catalog.sync.kafka-topic}",
            autoStartup = "${catalog.sync.kafka-enabled:true}")
    public void onMessage(ConsumerRecord<String, String> record) {
        CatalogUpdatedMessage message;
        try {
            message = jsonMapper.readValue(record.value(), CatalogUpdatedMessage.class);
        } catch (JacksonException | IllegalArgumentException e) {
            log.warn("Mensaje descartado (partición {}, offset {}): JSON inválido", record.partition(), record.offset());
            return;
        }

        if (message == null || !message.isValid()) {
            log.warn("Mensaje descartado (partición {}, offset {}): no es un CatalogUpdated válido",
                    record.partition(), record.offset());
            return;
        }

        handleCatalogUpdated.handle(message.toDomain());
    }
}