package com.valeartola.catalogo.infrastructure.persistence.adapter;

import com.valeartola.catalogo.domain.model.CatalogSnapshot;
import com.valeartola.catalogo.domain.model.CatalogSyncState;
import com.valeartola.catalogo.domain.model.SyncType;
import com.valeartola.catalogo.domain.ports.out.CatalogRepository;
import com.valeartola.catalogo.infrastructure.persistence.entity.CatalogSyncStateEntity;
import com.valeartola.catalogo.infrastructure.persistence.mapper.CatalogPersistenceMapper;
import com.valeartola.catalogo.infrastructure.persistence.repository.CatalogSyncStateJpaRepository;
import com.valeartola.catalogo.infrastructure.persistence.repository.ProfessionalCategoryJpaRepository;
import com.valeartola.catalogo.infrastructure.persistence.repository.ProfessionalJpaRepository;
import com.valeartola.catalogo.infrastructure.persistence.repository.WeeklyScheduleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Adaptador de salida: implementa CatalogRepository con JPA sobre PostgreSQL.
 */
@Component
@RequiredArgsConstructor
public class JpaCatalogRepositoryAdapter implements CatalogRepository {

    private final ProfessionalCategoryJpaRepository categoryRepository;
    private final ProfessionalJpaRepository professionalRepository;
    private final WeeklyScheduleJpaRepository scheduleRepository;
    private final CatalogSyncStateJpaRepository syncStateRepository;
    private final CatalogPersistenceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public CatalogSyncState getSyncState() {
        return mapper.toDomain(loadSyncState());
    }

    /**
     * Todo este método es UNA transacción: si cualquier paso falla,
     * la base vuelve exactamente a como estaba antes (todo o nada).
     */
    @Override
    @Transactional
    public void replaceWithSnapshot(CatalogSnapshot snapshot) {
        // 1. Borrar la copia anterior, en orden inverso a las claves foráneas
        scheduleRepository.deleteAllInBatch();
        professionalRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();

        // 2. Insertar la nueva copia: primero categorías, luego profesionales, luego horarios
        categoryRepository.saveAll(snapshot.categories().stream().map(mapper::toEntity).toList());
        professionalRepository.saveAll(snapshot.professionals().stream().map(mapper::toEntity).toList());
        scheduleRepository.saveAll(snapshot.schedules().stream().map(mapper::toEntity).toList());

        // 3. Registrar la versión aplicada (solo se confirma si todo lo anterior salió bien)
        Instant now = Instant.now();
        CatalogSyncStateEntity state = loadSyncState();
        state.setLocalVersion(snapshot.version());
        state.setLastSyncAt(now);
        state.setLastSyncType(SyncType.SNAPSHOT.name());
        state.setLastError(null);
        state.setUpdatedAt(now);
        syncStateRepository.save(state);
    }

    @Override
    @Transactional
    public void recordSyncError(String errorMessage) {
        CatalogSyncStateEntity state = loadSyncState();
        state.setLastError(errorMessage);
        state.setUpdatedAt(Instant.now());
        syncStateRepository.save(state);
    }

    private CatalogSyncStateEntity loadSyncState() {
        return syncStateRepository.findById(CatalogSyncStateEntity.SINGLE_ROW_ID)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe la fila de catalog_sync_state; revisar la migración V1"));
    }
}
