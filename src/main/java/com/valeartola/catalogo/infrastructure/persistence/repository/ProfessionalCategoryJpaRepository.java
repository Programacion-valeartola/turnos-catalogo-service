package com.valeartola.catalogo.infrastructure.persistence.repository;

import com.valeartola.catalogo.infrastructure.persistence.entity.ProfessionalCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessionalCategoryJpaRepository extends JpaRepository<ProfessionalCategoryEntity, Long> {
}
