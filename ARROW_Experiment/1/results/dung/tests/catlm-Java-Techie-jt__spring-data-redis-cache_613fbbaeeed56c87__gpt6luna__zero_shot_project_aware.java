package com.javatechie.redis;

import com.javatechie.redis.entity.Product;
import com.javatechie.redis.respository.ProductDao;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SpringDataRedisExampleApplication_ArrowTest {

    @Test
    public void getAllProductsReturnsDaoResults() throws Exception {
        ProductDao dao = mock(ProductDao.class);
        List<Product> products = new ArrayList<Product>();
        when(dao.findAll()).thenReturn(products);

        SpringDataRedisExampleApplication application = new SpringDataRedisExampleApplication();
        setDao(application, dao);

        assertEquals(products, application.getAllProducts());
        verify(dao).findAll();
    }

    @Test
    public void findProductReturnsDaoResult() throws Exception {
        ProductDao dao = mock(ProductDao.class);
        Product product = new Product();
        when(dao.findProductById(7)).thenReturn(product);

        SpringDataRedisExampleApplication application = new SpringDataRedisExampleApplication();
        setDao(application, dao);

        assertEquals(product, application.findProduct(7));
        verify(dao).findProductById(7);
    }

    @Test
    public void removeReturnsDaoResult() throws Exception {
        ProductDao dao = mock(ProductDao.class);
        when(dao.deleteProduct(4)).thenReturn("deleted");

        SpringDataRedisExampleApplication application = new SpringDataRedisExampleApplication();
        setDao(application, dao);

        assertEquals("deleted", application.remove(4));
        verify(dao).deleteProduct(4);
    }

    @Test
    public void saveReturnsDaoResult() throws Exception {
        ProductDao dao = mock(ProductDao.class);
        Product product = new Product();
        when(dao.save(product)).thenReturn(product);

        SpringDataRedisExampleApplication application = new SpringDataRedisExampleApplication();
        setDao(application, dao);

        assertEquals(product, application.save(product));
        verify(dao).save(product);
    }

    private static void setDao(SpringDataRedisExampleApplication application, ProductDao dao) throws Exception {
        Field field = SpringDataRedisExampleApplication.class.getDeclaredField("dao");
        field.setAccessible(true);
        field.set(application, dao);
    }
}
