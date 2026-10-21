package com.valeartola.catalogo.infrastructure.client;

import com.valeartola.catalogo.domain.exception.CatedraIntegrationException;
import com.valeartola.catalogo.domain.model.CatalogSnapshot;
import com.valeartola.catalogo.domain.ports.out.CatalogSnapshotProvider;
import com.valeartola.catalogo.infrastructure.client.dto.ProblemResponse;
import com.valeartola.catalogo.infrastructure.client.dto.SnapshotResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Adaptador de salida: implementa CatalogSnapshotProvider llamando a
 * GET /api/synchronization/snapshot de la cátedra.
 */
@Slf4j
@Component
public class CatedraSnapshotAdapter implements CatalogSnapshotProvider {

    static final String SNAPSHOT_PATH = "/api/synchronization/snapshot";

    private final RestClient restClient;

    public CatedraSnapshotAdapter(RestClient catedraRestClient) {
        this.restClient = catedraRestClient;
    }

    @Override
    public CatalogSnapshot fetchSnapshot() {
        try {
            SnapshotResponse response = restClient.get()
                    .uri(SNAPSHOT_PATH)
                    .exchange((request, httpResponse) -> {
                        if (httpResponse.getStatusCode().isError()) {
                            throw toIntegrationException(httpResponse.getStatusCode().value(),
                                    readProblem(httpResponse));
                        }
                        return httpResponse.bodyTo(SnapshotResponse.class);
                    });

            if (response == null || response.snapshotVersion() == null) {
                throw new CatedraIntegrationException(502, CatedraIntegrationException.UNEXPECTED_RESPONSE,
                        "La cátedra devolvió un snapshot vacío o incompleto");
            }

            CatalogSnapshot snapshot = response.toDomain();
            log.info("Snapshot recibido: versión {}, {} categorías, {} profesionales, {} horarios",
                    snapshot.version(), snapshot.categories().size(),
                    snapshot.professionals().size(), snapshot.schedules().size());
            return snapshot;

        } catch (ResourceAccessException e) {
            // Timeout o servidor inaccesible (por ejemplo, sin conexión a ZeroTier)
            throw new CatedraIntegrationException(503, CatedraIntegrationException.UNAVAILABLE,
                    "No se pudo conectar con el servicio de la cátedra", e);
        }
    }

    private ProblemResponse readProblem(RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse response) {
        try {
            return response.bodyTo(ProblemResponse.class);
        } catch (Exception e) {
            return null; // los 401/403 de Spring Security pueden venir sin cuerpo
        }
    }

    private CatedraIntegrationException toIntegrationException(int status, ProblemResponse problem) {
        String code = (problem != null && problem.code() != null) ? problem.code() : "HTTP_" + status;
        log.warn("Error al obtener el snapshot de la cátedra: status {}, code {}", status, code);
        return new CatedraIntegrationException(status, code, "La cátedra respondió con error " + code);
    }
}
