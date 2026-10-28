package com.valeartola.catalogo.infrastructure.web;

import com.valeartola.catalogo.domain.ports.in.GetCatalogSyncStatusUseCase;
import com.valeartola.catalogo.infrastructure.web.dto.SyncStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST: estado de la sincronización del catálogo.
 * Requiere un JWT válido (regla "anyRequest().authenticated()" de SecurityConfig).
 */
@RestController
@RequestMapping("/api/catalog/sync-status")
@RequiredArgsConstructor
public class CatalogSyncStatusController {

    private final GetCatalogSyncStatusUseCase getCatalogSyncStatus;

    @GetMapping
    public SyncStatusResponse getStatus() {
        return SyncStatusResponse.from(getCatalogSyncStatus.getStatus());
    }
}