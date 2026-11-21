package org.acelera.brasil;

import org.junit.Test;
import static org.junit.Assert.*;

public class Contador_ArrowTest {

    @Test
    public void testAddNullProduto() {
        Contador contador = new Contador();
        try {
            contador.add(null);
            fail("Deveria ter lancado NullPointerException");
        } catch (NullPointerException e) {
            assertEquals("produto eh obrigatorio", e.getMessage());
        }
    }

    @Test
    public void testQuantidadeNullProduto() {
        Contador contador = new Contador();
        try {
            contador.quantidade(null);
            fail("Deveria ter lancado NullPointerException");
        } catch (NullPointerException e) {
            assertEquals("produto eh obrigatorio", e.getMessage());
        }
    }

    @Test
    public void testAddPrimeiraVez() {
        Contador contador = new Contador();
        contador.add("arroz");
        assertEquals(1, contador.quantidade("arroz"));
    }

    @Test
    public void testAddSegundaVez() {
        Contador contador = new Contador();
        contador.add("feijao");
        contador.add("feijao");
        assertEquals(2, contador.quantidade("feijao"));
    }

    @Test
    public void testQuantidadeNaoExistente() {
        Contador contador = new Contador();
        assertEquals(0, contador.quantidade("nao_existe"));
    }

    @Test
    public void testAddVariosProdutos() {
        Contador contador = new Contador();
        contador.add("arroz");
        contador.add("feijao");
        contador.add("arroz");
        contador.add("macarrao");
        contador.add("feijao");
        assertEquals(2, contador.quantidade("arroz"));
        assertEquals(2, contador.quantidade("feijao"));
        assertEquals(1, contador.quantidade("macarrao"));
        assertEquals(0, contador.quantidade("nao_existe"));
    }
}
