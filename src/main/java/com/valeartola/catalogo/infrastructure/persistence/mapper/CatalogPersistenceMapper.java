package com.valeartola.catalogo.infrastructure.persistence.mapper;

import com.valeartola.catalogo.domain.model.CatalogSyncState;
import com.valeartola.catalogo.domain.model.Professional;
import com.valeartola.catalogo.domain.model.ProfessionalCategory;
import com.valeartola.catalogo.domain.model.SyncType;
import com.valeartola.catalogo.domain.model.WeeklySchedule;
import com.valeartola.catalogo.infrastructure.persistence.entity.CatalogSyncStateEntity;
import com.valeartola.catalogo.infrastructure.persistence.entity.ProfessionalCategoryEntity;
import com.valeartola.catalogo.infrastructure.persistence.entity.ProfessionalEntity;
import com.valeartola.catalogo.infrastructure.persistence.entity.WeeklyScheduleEntity;
import org.springframework.stereotype.Component;

/**
 * Convierte entre los modelos del dominio y las entidades JPA.
 * Así el dominio no conoce nada de la base de datos.
 */
@Component
public class CatalogPersistenceMapper {

    // ---------- Categorías ----------

    public ProfessionalCategory toDomain(ProfessionalCategoryEntity e) {
        return new ProfessionalCategory(e.getId(), e.getName(), e.getDescription(),
                e.isEnabled(), e.getCreatedAt(), e.getUpdatedAt());
    }

    public ProfessionalCategoryEntity toEntity(ProfessionalCategory c) {
        return new ProfessionalCategoryEntity(c.id(), c.name(), c.description(),
                c.enabled(), c.createdAt(), c.updatedAt());
    }

    // ---------- Profesionales ----------

    public Professional toDomain(ProfessionalEntity e) {
        return new Professional(e.getId(), e.getCategoryId(), e.getFirstName(), e.getLastName(),
                e.isEnabled(), e.getCreatedAt(), e.getUpdatedAt());
    }

    public ProfessionalEntity toEntity(Professional p) {
        return new ProfessionalEntity(p.id(), p.categoryId(), p.firstName(), p.lastName(),
                p.enabled(), p.createdAt(), p.updatedAt());
    }

    // ---------- Horarios semanales ----------

    public WeeklySchedule toDomain(WeeklyScheduleEntity e) {
        return new WeeklySchedule(e.getId(), e.getProfessionalId(), e.getDayOfWeek(),
                e.getStartTime(), e.getEndTime(), e.getSlotDurationMinutes(),
                e.isEnabled(), e.getCreatedAt(), e.getUpdatedAt());
    }

    public WeeklyScheduleEntity toEntity(WeeklySchedule s) {
        return new WeeklyScheduleEntity(s.id(), s.professionalId(), s.dayOfWeek(),
                s.startTime(), s.endTime(), s.slotDurationMinutes(),
                s.enabled(), s.createdAt(), s.updatedAt());
    }

    // ---------- Estado de sincronización ----------

    public CatalogSyncState toDomain(CatalogSyncStateEntity e) {
        SyncType type = e.getLastSyncType() == null ? null : SyncType.valueOf(e.getLastSyncType());
        return new CatalogSyncState(e.getLocalVersion(), e.getLastSyncAt(), type, e.getLastError());
    }
}
