package com.valeartola.catalogo.domain.model;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;

/**
 * Horario semanal de atención de un profesional.
 * Ejemplo: lunes de 12:00 a 15:00, con turnos de 30 minutos.
 * Las horas son locales del consultorio; los instantes (createdAt, updatedAt) están en UTC.
 */
public record WeeklySchedule(
        Long id,
        Long professionalId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        int slotDurationMinutes,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
