package com.valeartola.catalogo.infrastructure.client.redis;

import com.valeartola.catalogo.domain.exception.CatalogChangesUnavailableException;
import com.valeartola.catalogo.domain.model.CatalogChangeSet;
import com.valeartola.catalogo.domain.model.CatalogVersionWindow;
import com.valeartola.catalogo.domain.model.Professional;
import com.valeartola.catalogo.domain.model.ProfessionalCategory;
import com.valeartola.catalogo.domain.model.WeeklySchedule;
import com.valeartola.catalogo.domain.ports.out.CatalogChangesProvider;
import com.valeartola.catalogo.infrastructure.client.dto.ChangesResponse;
import com.valeartola.catalogo.infrastructure.client.dto.SnapshotResponse.CategoryDto;
import com.valeartola.catalogo.infrastructure.client.dto.SnapshotResponse.ProfessionalDto;
import com.valeartola.catalogo.infrastructure.client.dto.SnapshotResponse.WeeklyScheduleDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Lee de Redis los cambios incrementales publicados por la cátedra.
 * Solo usa claves de lectura bajo catedra:sync:*, permitidas por la ACL asignada.
 */
@Slf4j
@Component
public class RedisCatalogChangesAdapter implements CatalogChangesProvider {

    static final String METADATA_KEY = "catedra:sync:metadata";
    static final String CHANGES_KEY_PREFIX = "catedra:sync:changes:";
    static final String CATEGORIES_KEY = "catedra:sync:professional-categories";
    static final String PROFESSIONALS_KEY = "catedra:sync:professionals";
    static final String SCHEDULES_KEY = "catedra:sync:weekly-schedules";

    private final StringRedisTemplate redis;
    private final JsonMapper jsonMapper;

    public RedisCatalogChangesAdapter(StringRedisTemplate redis, JsonMapper jsonMapper) {
        this.redis = redis;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public CatalogVersionWindow fetchVersionWindow() {
        Map<String, String> metadata = read(() -> redis.<String, String>opsForHash().entries(METADATA_KEY));
        if (metadata == null || metadata.isEmpty()) {
            throw new CatalogChangesUnavailableException("No existe la metadata del catálogo en Redis");
        }
        try {
            return new CatalogVersionWindow(
                    Long.parseLong(metadata.get("currentVersion")),
                    Long.parseLong(metadata.get("oldestAvailableVersion")),
                    metadata.get("publishedAt") == null ? null : Instant.parse(metadata.get("publishedAt")));
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new CatalogChangesUnavailableException("Metadata del catálogo con formato inesperado", e);
        }
    }

    @Override
    public Optional<CatalogChangeSet> fetchChanges(long version) {
        String json = read(() -> redis.opsForValue().get(CHANGES_KEY_PREFIX + version));
        if (json == null) {
            return Optional.empty();
        }
        ChangesResponse response = parse(json, ChangesResponse.class, "changes:" + version);
        if (response.version() == null || response.version() != version || response.changes() == null) {
            throw new CatalogChangesUnavailableException("Contenido inconsistente en changes:" + version);
        }
        return Optional.of(response.toDomain());
    }

    @Override
    public List<ProfessionalCategory> fetchCategories(Collection<Long> ids) {
        return fetchEntities(CATEGORIES_KEY, ids, CategoryDto.class, CategoryDto::toDomain);
    }

    @Override
    public List<Professional> fetchProfessionals(Collection<Long> ids) {
        return fetchEntities(PROFESSIONALS_KEY, ids, ProfessionalDto.class, ProfessionalDto::toDomain);
    }

    @Override
    public List<WeeklySchedule> fetchSchedules(Collection<Long> ids) {
        return fetchEntities(SCHEDULES_KEY, ids, WeeklyScheduleDto.class, WeeklyScheduleDto::toDomain);
    }

    /** Lee varios fields de un Hash de una sola vez (HMGET) y los convierte al dominio. */
    private <D, T> List<T> fetchEntities(String key, Collection<Long> ids,
                                         Class<D> dtoType, Function<D, T> toDomain) {
        if (ids.isEmpty()) {
            return List.of();
        }
        List<String> fields = ids.stream().map(String::valueOf).toList();
        List<String> values = read(() -> redis.<String, String>opsForHash().multiGet(key, fields));

        List<T> result = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++) {
            String json = values == null ? null : values.get(i);
            if (json == null) {
                throw new CatalogChangesUnavailableException(
                        "No se encontró el ID " + fields.get(i) + " en " + key);
            }
            result.add(toDomain.apply(parse(json, dtoType, key + " " + fields.get(i))));
        }
        return result;
    }

    private <R> R read(RedisCall<R> call) {
        try {
            return call.execute();
        } catch (DataAccessException e) {
            // Se muestra la causa concreta (dirección rechazada, credenciales inválidas, etc.)
            log.warn("Redis no disponible: {}", e.getMostSpecificCause().getMessage());   // ← CAMBIO
            throw new CatalogChangesUnavailableException("Redis no disponible", e);
        }
    }

    private <R> R parse(String json, Class<R> type, String origin) {
        try {
            return jsonMapper.readValue(json, type);
        } catch (JacksonException e) {
            throw new CatalogChangesUnavailableException("JSON inválido en " + origin, e);
        }
    }

    @FunctionalInterface
    private interface RedisCall<R> {
        R execute();
    }
}