package com.javatechie.redis;

import com.javatechie.redis.entity.Product;
import com.javatechie.redis.respository.ProductDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class SpringDataRedisExampleApplication_ArrowTest {

    @Mock
    private ProductDao dao;

    private SpringDataRedisExampleApplication application;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        application = new SpringDataRedisExampleApplication();
        application.dao = dao;
    }

    @Test
    public void testSave() {
        Product product = new Product(1, "Test Product", 100);
        when(dao.save(product)).thenReturn(product);

        Product result = application.save(product);

        assertEquals(product, result);
        verify(dao).save(product);
    }

    @Test
    public void testGetAllProducts() {
        Product product1 = new Product(1, "Product 1", 100);
        Product product2 = new Product(2, "Product 2", 200);
        List<Product> products = Arrays.asList(product1, product2);
        when(dao.findAll()).thenReturn(products);

        List<Product> result = application.getAllProducts();

        assertEquals(products, result);
        verify(dao).findAll();
    }

    @Test
    public void testFindProduct() {
        Product product = new Product(1, "Test Product", 100);
        when(dao.findProductById(1)).thenReturn(product);

        Product result = application.findProduct(1);

        assertEquals(product, result);
        verify(dao).findProductById(1);
    }

    @Test
    public void testRemove() {
        String expectedResult = "Product deleted successfully";
        when(dao.deleteProduct(1)).thenReturn(expectedResult);

        String result = application.remove(1);

        assertEquals(expectedResult, result);
        verify(dao).deleteProduct(1);
    }
}
