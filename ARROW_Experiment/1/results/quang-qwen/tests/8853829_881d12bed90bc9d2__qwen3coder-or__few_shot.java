package br.com.caelum.vraptor.console.myproducts;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Order_ArrowTest {

	@Test
	public void shouldCalculateTotalForEmptyOrder() {
		Order order = new Order();
		assertEquals(0.0, order.getTotal(), 0.001);
	}

	@Test
	public void shouldCalculateTotalForSingleProduct() {
		Product product = new Product("Product 1", 100.0);
		Order order = new Order(product);
		assertEquals(100.0, order.getTotal(), 0.001);
	}

	@Test
	public void shouldCalculateTotalForMultipleProducts() {
		Product product1 = new Product("Product 1", 100.0);
		Product product2 = new Product("Product 2", 50.0);
		Product product3 = new Product("Product 3", 25.0);
		Order order = new Order(product1, product2, product3);
		assertEquals(175.0, order.getTotal(), 0.001);
	}
}
