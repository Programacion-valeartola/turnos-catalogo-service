package com.valeartola.catalogo.application.usecases;

import com.valeartola.catalogo.domain.model.CatalogSyncState;
import com.valeartola.catalogo.domain.ports.in.GetCatalogSyncStatusUseCase;
import com.valeartola.catalogo.domain.ports.out.CatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Devuelve el estado de sincronización tal como está guardado en la base local.
 */
@Component
@RequiredArgsConstructor
public class GetCatalogSyncStatusUseCaseImpl implements GetCatalogSyncStatusUseCase {

    private final CatalogRepository catalogRepository;

    @Override
    public CatalogSyncState getStatus() {
        return catalogRepository.getSyncState();
    }
}