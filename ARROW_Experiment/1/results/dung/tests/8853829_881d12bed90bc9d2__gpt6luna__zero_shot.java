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
		Order order = new Order(new Product(10.0));
		assertEquals(10.0, order.getTotal(), 0.0);
	}

	@Test
	public void sumsPricesForMultipleProducts() {
		Order order = new Order(new Product(10.0), new Product(5.5), new Product(2.0));
		assertEquals(17.5, order.getTotal(), 0.0);
	}
}
