package com.valeartola.catalogo.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "catalog_sync_state")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CatalogSyncStateEntity {

    /** Fila única: el id siempre vale 1. */
    public static final short SINGLE_ROW_ID = 1;

    @Id
    private Short id;

    @Column(name = "local_version")
    private Long localVersion;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;

    @Column(name = "last_sync_type", length = 20)
    private String lastSyncType;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
