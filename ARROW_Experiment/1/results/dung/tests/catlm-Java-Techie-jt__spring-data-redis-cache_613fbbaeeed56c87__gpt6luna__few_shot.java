package com.javatechie.redis;

import com.javatechie.redis.entity.Product;
import com.javatechie.redis.respository.ProductDao;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class SpringDataRedisExampleApplication_ArrowTest {

    private void setDao(SpringDataRedisExampleApplication application, ProductDao dao) throws Exception {
        Field field = SpringDataRedisExampleApplication.class.getDeclaredField("dao");
        field.setAccessible(true);
        field.set(application, dao);
    }

    @Test
    public void getAllProductsReturnsDaoResults() throws Exception {
        SpringDataRedisExampleApplication application = new SpringDataRedisExampleApplication();
        ProductDao dao = new ProductDao();
        List<Product> products = new ArrayList<Product>();
        setDao(application, dao);

        assertEquals(dao.findAll(), application.getAllProducts());
    }

    @Test
    public void findProductReturnsDaoResult() throws Exception {
        SpringDataRedisExampleApplication application = new SpringDataRedisExampleApplication();
        ProductDao dao = new ProductDao();
        setDao(application, dao);

        assertEquals(dao.findProductById(7), application.findProduct(7));
    }

    @Test
    public void removeReturnsDaoResult() throws Exception {
        SpringDataRedisExampleApplication application = new SpringDataRedisExampleApplication();
        ProductDao dao = new ProductDao();
        setDao(application, dao);

        assertEquals(dao.deleteProduct(7), application.remove(7));
    }

    @Test
    public void saveReturnsDaoResult() throws Exception {
        SpringDataRedisExampleApplication application = new SpringDataRedisExampleApplication();
        ProductDao dao = new ProductDao();
        Product product = new Product();
        setDao(application, dao);

        assertSame(dao.save(product), application.save(product));
    }
}
