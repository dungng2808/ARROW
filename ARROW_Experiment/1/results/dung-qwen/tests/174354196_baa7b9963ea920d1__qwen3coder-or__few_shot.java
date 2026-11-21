package org.acelera.brasil;

import org.junit.Test;
import static org.junit.Assert.*;

public class Contador_ArrowTest {

    @Test
    public void testAddIncreasesCount() {
        Contador contador = new Contador();
        contador.add("produto1");
        assertEquals(1, contador.quantidade("produto1"));
        
        contador.add("produto1");
        assertEquals(2, contador.quantidade("produto1"));
    }

    @Test
    public void testAddWithNewProduct() {
        Contador contador = new Contador();
        contador.add("produto1");
        assertEquals(1, contador.quantidade("produto1"));
    }

    @Test
    public void testQuantityForNonExistentProduct() {
        Contador contador = new Contador();
        assertEquals(0, contador.quantidade("produto1"));
    }

    @Test(expected = NullPointerException.class)
    public void testAddWithNullProduct() {
        Contador contador = new Contador();
        contador.add(null);
    }

    @Test(expected = NullPointerException.class)
    public void testQuantityWithNullProduct() {
        Contador contador = new Contador();
        contador.quantidade(null);
    }

    @Test
    public void testMultipleProducts() {
        Contador contador = new Contador();
        contador.add("produto1");
        contador.add("produto2");
        contador.add("produto1");
        
        assertEquals(2, contador.quantidade("produto1"));
        assertEquals(1, contador.quantidade("produto2"));
        assertEquals(0, contador.quantidade("produto3"));
    }
}
