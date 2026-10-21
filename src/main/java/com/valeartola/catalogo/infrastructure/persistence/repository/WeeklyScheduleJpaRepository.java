package com.valeartola.catalogo.infrastructure.persistence.repository;

import com.valeartola.catalogo.infrastructure.persistence.entity.WeeklyScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyScheduleJpaRepository extends JpaRepository<WeeklyScheduleEntity, Long> {
}
