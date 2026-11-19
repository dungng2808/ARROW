package org.acelera.brasil;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import java.util.HashMap;

public class Contador_ArrowTest {

    @Test
    public void testAddWhenProdutoIsNullShouldThrowNullPointerException() {
        Contador contador = new Contador();
        assertThrows(NullPointerException.class, () -> {
            contador.add(null);
        });
    }

    @Test
    public void testAddWhenProdutoIsNotNullShouldIncrementCount() {
        Contador contador = new Contador();
        contador.add("produto1");
        assertEquals(1, contador.quantidade("produto1"));
        
        contador.add("produto1");
        assertEquals(2, contador.quantidade("produto1"));
    }

    @Test
    public void testAddWhenProdutoIsNewShouldInitializeCountToOne() {
        Contador contador = new Contador();
        contador.add("novoProduto");
        assertEquals(1, contador.quantidade("novoProduto"));
    }

    @Test
    public void testQuantidadeWhenProdutoIsNullShouldThrowNullPointerException() {
        Contador contador = new Contador();
        assertThrows(NullPointerException.class, () -> {
            contador.quantidade(null);
        });
    }

    @Test
    public void testQuantidadeWhenProdutoDoesNotExistShouldReturnZero() {
        Contador contador = new Contador();
        assertEquals(0, contador.quantidade("inexistente"));
    }

    @Test
    public void testQuantidadeWhenProdutoExistsShouldReturnCorrectCount() {
        Contador contador = new Contador();
        contador.add("produto1");
        contador.add("produto1");
        contador.add("produto2");
        assertEquals(2, contador.quantidade("produto1"));
        assertEquals(1, contador.quantidade("produto2"));
        assertEquals(0, contador.quantidade("outroProduto"));
    }
}
