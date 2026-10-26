package com.valeartola.catalogo.infrastructure.client.redis;

import com.valeartola.catalogo.domain.exception.CatalogChangesUnavailableException;
import com.valeartola.catalogo.domain.model.CatalogChangeSet;
import com.valeartola.catalogo.domain.model.CatalogVersionWindow;
import com.valeartola.catalogo.domain.model.Professional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.json.JsonMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Prueba el adaptador simulando Redis con Mockito (no necesita la red de la cátedra).
 */
class RedisCatalogChangesAdapterTest {

    private HashOperations<String, String, String> hashOps;
    private ValueOperations<String, String> valueOps;
    private RedisCatalogChangesAdapter adapter;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        hashOps = mock(HashOperations.class);
        valueOps = mock(ValueOperations.class);
        when(redis.<String, String>opsForHash()).thenReturn(hashOps);
        when(redis.opsForValue()).thenReturn(valueOps);
        adapter = new RedisCatalogChangesAdapter(redis, JsonMapper.builder().build());
    }

    @Test
    void leeLaVentanaDeVersionesDesdeLaMetadata() {
        when(hashOps.entries(RedisCatalogChangesAdapter.METADATA_KEY)).thenReturn(Map.of(
                "currentVersion", "7",
                "oldestAvailableVersion", "4",
                "publishedAt", "2026-09-22T00:08:12.755027225Z"));

        CatalogVersionWindow window = adapter.fetchVersionWindow();

        assertThat(window.currentVersion()).isEqualTo(7);
        assertThat(window.oldestAvailableVersion()).isEqualTo(4);
        assertThat(window.publishedAt()).isNotNull();
    }

    @Test
    void leeLosIdsAfectadosPorUnaVersion() {
        when(valueOps.get("catedra:sync:changes:7")).thenReturn("""
                {"version":7,"changes":{"professionalCategories":[],"professionals":[215],"weeklySchedules":[301,302]}}
                """);

        CatalogChangeSet changes = adapter.fetchChanges(7).orElseThrow();

        assertThat(changes.version()).isEqualTo(7);
        assertThat(changes.categoryIds()).isEmpty();
        assertThat(changes.professionalIds()).containsExactly(215L);
        assertThat(changes.scheduleIds()).containsExactly(301L, 302L);
    }

    @Test
    void unaVersionQueYaNoSeConservaDevuelveVacio() {
        when(valueOps.get(anyString())).thenReturn(null);

        Optional<CatalogChangeSet> changes = adapter.fetchChanges(2);

        assertThat(changes).isEmpty();
    }

    @Test
    void leeElEstadoVigenteDeLosProfesionalesIgnorandoCamposDesconocidos() {
        when(hashOps.multiGet(RedisCatalogChangesAdapter.PROFESSIONALS_KEY, List.of("215"))).thenReturn(List.of("""
                {"id":215,"categoryId":101,"firstName":"Ana","lastName":"Gómez","enabled":false,
                 "createdAt":"2026-07-01T12:00:00Z","updatedAt":"2026-09-10T13:40:00Z","campoNuevo":"x"}
                """));

        List<Professional> professionals = adapter.fetchProfessionals(List.of(215L));

        assertThat(professionals).hasSize(1);
        assertThat(professionals.getFirst().lastName()).isEqualTo("Gómez");
        assertThat(professionals.getFirst().enabled()).isFalse();
    }

    @Test
    void siFaltaUnaEntidadAfectadaFalla() {
        when(hashOps.multiGet(RedisCatalogChangesAdapter.PROFESSIONALS_KEY, List.of("999")))
                .thenReturn(Arrays.asList((String) null));

        assertThatThrownBy(() -> adapter.fetchProfessionals(List.of(999L)))
                .isInstanceOf(CatalogChangesUnavailableException.class)
                .hasMessageContaining("999");
    }

    @Test
    void siRedisNoRespondeSeTraduceAUnaExcepcionDelDominio() {
        when(hashOps.entries(anyString())).thenThrow(new RedisConnectionFailureException("timeout"));

        assertThatThrownBy(() -> adapter.fetchVersionWindow())
                .isInstanceOf(CatalogChangesUnavailableException.class)
                .hasMessageContaining("Redis no disponible");
    }
}