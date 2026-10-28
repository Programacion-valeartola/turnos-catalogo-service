package com.valeartola.catalogo.infrastructure.persistence.adapter;

import com.valeartola.catalogo.domain.ports.out.ProcessedEventRepository;
import com.valeartola.catalogo.infrastructure.persistence.entity.ProcessedEventEntity;
import com.valeartola.catalogo.infrastructure.persistence.repository.ProcessedEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Adaptador de salida: guarda los eventId procesados en la tabla processed_event.
 */
@Component
@RequiredArgsConstructor
public class JpaProcessedEventRepositoryAdapter implements ProcessedEventRepository {

    private final ProcessedEventJpaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public boolean isProcessed(String eventId) {
        return repository.existsById(eventId);
    }

    @Override
    @Transactional
    public void markProcessed(String eventId, String eventType) {
        if (!repository.existsById(eventId)) {
            repository.save(new ProcessedEventEntity(eventId, eventType, Instant.now()));
        }
    }
}