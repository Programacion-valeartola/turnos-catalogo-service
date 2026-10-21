package com.valeartola.catalogo.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.valeartola.catalogo.domain.model.CatalogSnapshot;
import com.valeartola.catalogo.domain.model.Professional;
import com.valeartola.catalogo.domain.model.ProfessionalCategory;
import com.valeartola.catalogo.domain.model.WeeklySchedule;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

/**
 * JSON devuelto por GET /api/synchronization/snapshot.
 * Los campos desconocidos se ignoran, como exige el contrato v1.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SnapshotResponse(
        Long snapshotVersion,
        Instant generatedAt,
        List<CategoryDto> professionalCategories,
        List<ProfessionalDto> professionals,
        List<WeeklyScheduleDto> weeklySchedules
) {

    public CatalogSnapshot toDomain() {
        return new CatalogSnapshot(
                snapshotVersion,
                generatedAt,
                professionalCategories.stream().map(CategoryDto::toDomain).toList(),
                professionals.stream().map(ProfessionalDto::toDomain).toList(),
                weeklySchedules.stream().map(WeeklyScheduleDto::toDomain).toList());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CategoryDto(Long id, String name, String description, boolean enabled,
                              Instant createdAt, Instant updatedAt) {
        ProfessionalCategory toDomain() {
            return new ProfessionalCategory(id, name, description, enabled, createdAt, updatedAt);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProfessionalDto(Long id, Long categoryId, String firstName, String lastName,
                                  boolean enabled, Instant createdAt, Instant updatedAt) {
        Professional toDomain() {
            return new Professional(id, categoryId, firstName, lastName, enabled, createdAt, updatedAt);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeeklyScheduleDto(Long id, Long professionalId, DayOfWeek dayOfWeek,
                                    LocalTime startTime, LocalTime endTime, int slotDurationMinutes,
                                    boolean enabled, Instant createdAt, Instant updatedAt) {
        WeeklySchedule toDomain() {
            return new WeeklySchedule(id, professionalId, dayOfWeek, startTime, endTime,
                    slotDurationMinutes, enabled, createdAt, updatedAt);
        }
    }
}
