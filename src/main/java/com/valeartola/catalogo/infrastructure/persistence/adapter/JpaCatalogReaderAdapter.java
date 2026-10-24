package com.valeartola.catalogo.infrastructure.persistence.adapter;

import com.valeartola.catalogo.domain.model.Professional;
import com.valeartola.catalogo.domain.model.ProfessionalCategory;
import com.valeartola.catalogo.domain.model.WeeklySchedule;
import com.valeartola.catalogo.domain.ports.out.CatalogReader;
import com.valeartola.catalogo.infrastructure.persistence.mapper.CatalogPersistenceMapper;
import com.valeartola.catalogo.infrastructure.persistence.repository.ProfessionalCategoryJpaRepository;
import com.valeartola.catalogo.infrastructure.persistence.repository.ProfessionalJpaRepository;
import com.valeartola.catalogo.infrastructure.persistence.repository.WeeklyScheduleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Adaptador de salida: lectura de la copia local del catálogo con JPA.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaCatalogReaderAdapter implements CatalogReader {

    private final ProfessionalCategoryJpaRepository categoryRepository;
    private final ProfessionalJpaRepository professionalRepository;
    private final WeeklyScheduleJpaRepository scheduleRepository;
    private final CatalogPersistenceMapper mapper;

    @Override
    public List<ProfessionalCategory> findAllCategories() {
        return categoryRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Professional> findAllProfessionals() {
        return professionalRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<WeeklySchedule> findAllSchedules() {
        return scheduleRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}
