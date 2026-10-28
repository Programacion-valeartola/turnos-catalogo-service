package com.valeartola.catalogo.infrastructure.persistence.repository;

import com.valeartola.catalogo.infrastructure.persistence.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity, String> {
}