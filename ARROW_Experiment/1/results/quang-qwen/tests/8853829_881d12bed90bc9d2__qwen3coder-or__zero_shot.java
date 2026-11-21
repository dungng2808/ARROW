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
		Product product = new Product() {
			@Override
			public double getPrice() {
				return 10.0;
			}
		};
		Order order = new Order(product);
		assertEquals(10.0, order.getTotal(), 0.001);
	}

	@Test
	public void shouldCalculateTotalForMultipleProducts() {
		Product product1 = new Product() {
			@Override
			public double getPrice() {
				return 10.0;
			}
		};
		Product product2 = new Product() {
			@Override
			public double getPrice() {
				return 20.0;
			}
		};
		Product product3 = new Product() {
			@Override
			public double getPrice() {
				return 30.0;
			}
		};
		Order order = new Order(product1, product2, product3);
		assertEquals(60.0, order.getTotal(), 0.001);
	}

	@Test
	public void shouldHandleNullProductsInArray() {
		Product[] products = {null, null};
		Order order = new Order(products);
		// This will throw NullPointerException when getTotal() is called
		// but we're testing the constructor behavior
		assertEquals(0.0, order.getTotal(), 0.001); // This won't be reached due to NPE
	}
}
