package br.com.caelum.vraptor.console.myproducts;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class Order_ArrowTest {

    @Test
    public void totalIsZeroWhenThereAreNoProducts() {
        Order order = new Order();
        assertEquals(0.0, order.getTotal(), 0.0);
    }

    @Test
    public void totalIsTheSumOfProductPrices() {
        Product first = new Product();
        first.setPrice(12.5);
        Product second = new Product();
        second.setPrice(7.5);

        Order order = new Order(first, second);

        assertEquals(20.0, order.getTotal(), 0.0);
    }
}
