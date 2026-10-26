package com.valeartola.catalogo.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogVersionWindowTest {

    // Cátedra en versión 7; conserva los cambios desde la versión 4
    private final CatalogVersionWindow window = new CatalogVersionWindow(7, 4, null);

    @Test
    void versionLocalIgualALaActualEstaAlDia() {
        assertThat(window.isUpToDate(7)).isTrue();
        assertThat(window.requiresSnapshot(7)).isFalse();
    }

    @Test
    void versionLocalDentroDeLaVentanaPermiteContinuarIncrementalmente() {
        assertThat(window.requiresSnapshot(4)).isFalse();
        assertThat(window.requiresSnapshot(6)).isFalse();
    }

    @Test
    void versionLocalAnteriorALaVentanaRequiereSnapshot() {
        // Caso 18.2 del contrato: local 3, oldest 4 -> snapshot
        assertThat(window.requiresSnapshot(3)).isTrue();
    }

    @Test
    void versionLocalMayorQueLaPublicadaRequiereSnapshot() {
        assertThat(window.requiresSnapshot(8)).isTrue();
    }
}