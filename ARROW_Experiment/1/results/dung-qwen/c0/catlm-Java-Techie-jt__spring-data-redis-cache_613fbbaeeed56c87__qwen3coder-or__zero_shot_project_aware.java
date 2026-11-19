package com.javatechie.redis;

import com.javatechie.redis.entity.Product;
import com.javatechie.redis.respository.ProductDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class SpringDataRedisExampleApplication_ArrowTest {

    @Mock
    private ProductDao productDao;

    private SpringDataRedisExampleApplication controller;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        controller = new SpringDataRedisExampleApplication();
        controller.dao = productDao;
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testSave() throws Exception {
        Product product = new Product(1, "Test Product", 100.0);
        when(productDao.save(any(Product.class))).thenReturn(product);

        mockMvc.perform(post("/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"name\":\"Test Product\",\"price\":100.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(100.0));

        verify(productDao, times(1)).save(any(Product.class));
    }

    @Test
    public void testGetAllProducts() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(new Product(1, "Product 1", 100.0));
        products.add(new Product(2, "Product 2", 200.0));
        when(productDao.findAll()).thenReturn(products);

        mockMvc.perform(get("/product")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[0].price").value(100.0))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Product 2"))
                .andExpect(jsonPath("$[1].price").value(200.0));

        verify(productDao, times(1)).findAll();
    }

    @Test
    public void testFindProduct() throws Exception {
        Product product = new Product(1, "Test Product", 100.0);
        when(productDao.findProductById(anyInt())).thenReturn(product);

        mockMvc.perform(get("/product/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(100.0));

        verify(productDao, times(1)).findProductById(1);
    }

    @Test
    public void testRemove() throws Exception {
        when(productDao.deleteProduct(anyInt())).thenReturn("Product deleted successfully");

        mockMvc.perform(delete("/product/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Product deleted successfully"));

        verify(productDao, times(1)).deleteProduct(1);
    }
}
