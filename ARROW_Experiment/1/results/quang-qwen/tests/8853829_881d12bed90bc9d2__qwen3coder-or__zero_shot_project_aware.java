package br.com.caelum.vraptor.console.myproducts;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class Order_ArrowTest {

	@Test
	public void shouldCalculateTotalPriceOfProducts() {
		Product product1 = mock(Product.class);
		Product product2 = mock(Product.class);
		
		when(product1.getPrice()).thenReturn(10.0);
		when(product2.getPrice()).thenReturn(20.0);
		
		Order order = new Order(product1, product2);
		
		assertThat(order.getTotal(), is(30.0));
	}
	
	@Test
	public void shouldReturnZeroWhenNoProducts() {
		Order order = new Order();
		
		assertThat(order.getTotal(), is(0.0));
	}
	
	@Test
	public void shouldHandleSingleProduct() {
		Product product = mock(Product.class);
		when(product.getPrice()).thenReturn(15.5);
		
		Order order = new Order(product);
		
		assertThat(order.getTotal(), is(15.5));
	}
}
