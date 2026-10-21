package com.valeartola.catalogo.infrastructure.persistence.repository;

import com.valeartola.catalogo.infrastructure.persistence.entity.ProfessionalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessionalJpaRepository extends JpaRepository<ProfessionalEntity, Long> {
}
