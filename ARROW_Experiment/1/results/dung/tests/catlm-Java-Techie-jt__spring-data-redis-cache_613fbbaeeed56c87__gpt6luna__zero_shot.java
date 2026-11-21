package com.javatechie.redis;

import com.javatechie.redis.entity.Product;
import com.javatechie.redis.respository.ProductDao;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SpringDataRedisExampleApplication_ArrowTest {

    @Test
    public void saveReturnsProductFromDao() throws Exception {
        SpringDataRedisExampleApplication application = new SpringDataRedisExampleApplication();
        ProductDao dao = mock(ProductDao.class);
        Product product = mock(Product.class);
        when(dao.save(product)).thenReturn(product);
        setDao(application, dao);

        assertSame(product, application.save(product));
        verify(dao).save(product);
    }

    @Test
    public void getAllProductsReturnsDaoResults() throws Exception {
        SpringDataRedisExampleApplication application = new SpringDataRedisExampleApplication();
        ProductDao dao = mock(ProductDao.class);
        List<Product> products = new ArrayList<Product>();
        when(dao.findAll()).thenReturn(products);
        setDao(application, dao);

        assertSame(products, application.getAllProducts());
        verify(dao).findAll();
    }

    @Test
    public void findProductReturnsDaoResult() throws Exception {
        SpringDataRedisExampleApplication application = new SpringDataRedisExampleApplication();
        ProductDao dao = mock(ProductDao.class);
        Product product = mock(Product.class);
        when(dao.findProductById(7)).thenReturn(product);
        setDao(application, dao);

        assertSame(product, application.findProduct(7));
        verify(dao).findProductById(7);
    }

    @Test
    public void removeReturnsDaoResult() throws Exception {
        SpringDataRedisExampleApplication application = new SpringDataRedisExampleApplication();
        ProductDao dao = mock(ProductDao.class);
        when(dao.deleteProduct(7)).thenReturn("deleted");
        setDao(application, dao);

        assertEquals("deleted", application.remove(7));
        verify(dao).deleteProduct(7);
    }

    private void setDao(SpringDataRedisExampleApplication application, ProductDao dao) throws Exception {
        Field field = SpringDataRedisExampleApplication.class.getDeclaredField("dao");
        field.setAccessible(true);
        field.set(application, dao);
    }
}
