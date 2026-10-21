package com.valeartola.catalogo.infrastructure.persistence.repository;

import com.valeartola.catalogo.infrastructure.persistence.entity.CatalogSyncStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogSyncStateJpaRepository extends JpaRepository<CatalogSyncStateEntity, Short> {
}
