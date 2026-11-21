package org.acelera.brasil;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Contador_ArrowTest {

    @Test
    public void quantidadeReturnsZeroForUnseenProduct() {
        Contador contador = new Contador();

        assertEquals(0, contador.quantidade("arroz"));
    }

    @Test
    public void addSetsProductQuantityToOne() {
        Contador contador = new Contador();

        contador.add("arroz");

        assertEquals(1, contador.quantidade("arroz"));
    }

    @Test
    public void addingSameProductAgainStillLeavesQuantityAtOne() {
        Contador contador = new Contador();

        contador.add("arroz");
        contador.add("arroz");

        assertEquals(1, contador.quantidade("arroz"));
    }

    @Test
    public void addNullThrowsNullPointerException() {
        Contador contador = new Contador();

        assertThrows(NullPointerException.class, () -> contador.add(null));
    }

    @Test
    public void quantidadeNullThrowsNullPointerException() {
        Contador contador = new Contador();

        assertThrows(NullPointerException.class, () -> contador.quantidade(null));
    }
}
