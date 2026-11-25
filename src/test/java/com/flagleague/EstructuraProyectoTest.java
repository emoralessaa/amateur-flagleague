package com.flagleague;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Pruebas de humo de recursos; NO sustituyen las pruebas de los módulos. */
class EstructuraProyectoTest {

    @Test
    void existeVistaInicialFXML() {
        assertNotNull(getClass().getResource("/fxml/dashboard.fxml"));
    }

    @Test
    void existeHojaEstilos() {
        assertNotNull(getClass().getResource("/css/flagleague.css"));
    }
}
