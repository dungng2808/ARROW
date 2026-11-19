package br.com.caelum.vraptor.console.myproducts;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Order_ArrowTest {

    @Test
    public void returnsZeroForNoProducts() {
        Order order = new Order();
        assertEquals(0.0, order.getTotal(), 0.0);
    }

    @Test
    public void returnsPriceForOneProduct() {
        Product product = new Product("Product", 12.5);
        Order order = new Order(product);
        assertEquals(12.5, order.getTotal(), 0.0);
    }

    @Test
    public void sumsPricesForMultipleProducts() {
        Product first = new Product("First", 12.5);
        Product second = new Product("Second", 7.25);
        Order order = new Order(first, second);
        assertEquals(19.75, order.getTotal(), 0.0);
    }
}
