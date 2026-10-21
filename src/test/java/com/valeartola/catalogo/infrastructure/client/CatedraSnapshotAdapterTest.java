package com.valeartola.catalogo.infrastructure.client;

import com.valeartola.catalogo.domain.exception.CatedraIntegrationException;
import com.valeartola.catalogo.domain.model.CatalogSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Prueba el adaptador sin conectarse a la cátedra real: un servidor simulado
 * responde lo que la cátedra respondería.
 */
class CatedraSnapshotAdapterTest {

    private static final String BASE_URL = "http://catedra.test";

    private static final String SNAPSHOT_JSON = """
            {
              "snapshotVersion": 7,
              "generatedAt": "2026-09-10T13:44:59Z",
              "campoNuevoDesconocido": "se ignora",
              "professionalCategories": [
                {"id": 1, "name": "Clinica medica", "description": "Atencion general", "enabled": true,
                 "createdAt": "2026-07-01T12:00:00Z", "updatedAt": "2026-09-10T13:40:00Z"}
              ],
              "professionals": [
                {"id": 15, "categoryId": 1, "firstName": "Ana", "lastName": "Gomez", "enabled": true,
                 "createdAt": "2026-07-01T12:00:00Z", "updatedAt": "2026-09-10T13:40:00Z"}
              ],
              "weeklySchedules": [
                {"id": 21, "professionalId": 15, "dayOfWeek": "MONDAY", "startTime": "09:00:00",
                 "endTime": "12:00:00", "slotDurationMinutes": 30, "enabled": true,
                 "createdAt": "2026-07-01T12:00:00Z", "updatedAt": "2026-09-10T13:40:00Z"}
              ]
            }
            """;

    private MockRestServiceServer server;
    private CatedraSnapshotAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new CatedraSnapshotAdapter(builder.build());
    }

    @Test
    void convierteElSnapshotAlModeloDeDominio() {
        server.expect(requestTo(BASE_URL + CatedraSnapshotAdapter.SNAPSHOT_PATH))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(SNAPSHOT_JSON, MediaType.APPLICATION_JSON));

        CatalogSnapshot snapshot = adapter.fetchSnapshot();

        assertThat(snapshot.version()).isEqualTo(7);
        assertThat(snapshot.categories()).hasSize(1);
        assertThat(snapshot.professionals().getFirst().fullName()).isEqualTo("Ana Gomez");
        assertThat(snapshot.schedules().getFirst().dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(snapshot.schedules().getFirst().startTime()).isEqualTo(LocalTime.of(9, 0));
        server.verify();
    }

    @Test
    void traduceElErrorDeLaCatedraUsandoElCode() {
        server.expect(requestTo(BASE_URL + CatedraSnapshotAdapter.SNAPSHOT_PATH))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                        .body("{\"status\":403,\"code\":\"STUDENT_INTEGRATION_NOT_FOUND\",\"detail\":\"x\"}"));

        assertThatThrownBy(() -> adapter.fetchSnapshot())
                .isInstanceOf(CatedraIntegrationException.class)
                .satisfies(e -> {
                    CatedraIntegrationException ex = (CatedraIntegrationException) e;
                    assertThat(ex.getStatus()).isEqualTo(403);
                    assertThat(ex.getCode()).isEqualTo("STUDENT_INTEGRATION_NOT_FOUND");
                });
    }

    @Test
    void unErrorSinCuerpoUsaElStatusComoCode() {
        server.expect(requestTo(BASE_URL + CatedraSnapshotAdapter.SNAPSHOT_PATH))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> adapter.fetchSnapshot())
                .isInstanceOf(CatedraIntegrationException.class)
                .satisfies(e -> assertThat(((CatedraIntegrationException) e).getCode()).isEqualTo("HTTP_401"));
    }
}
