package com.sap.espm.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class ProductCategory_ArrowTest {

    @Test
    public void testGetCategory() {
        ProductCategory productCategory = new ProductCategory();
        productCategory.setCategory("Electronics");
        assertEquals("Electronics", productCategory.getCategory());
    }

    @Test
    public void testSetCategory() {
        ProductCategory productCategory = new ProductCategory();
        productCategory.setCategory("Clothing");
        assertEquals("Clothing", productCategory.getCategory());
    }

    @Test
    public void testGetCategoryName() {
        ProductCategory productCategory = new ProductCategory();
        productCategory.setCategoryName("Smartphones");
        assertEquals("Smartphones", productCategory.getCategoryName());
    }

    @Test
    public void testSetCategoryName() {
        ProductCategory productCategory = new ProductCategory();
        productCategory.setCategoryName("Laptops");
        assertEquals("Laptops", productCategory.getCategoryName());
    }

    @Test
    public void testGetMainCategory() {
        ProductCategory productCategory = new ProductCategory();
        productCategory.setMainCategory("Tech");
        assertEquals("Tech", productCategory.getMainCategory());
    }

    @Test
    public void testSetMainCategory() {
        ProductCategory productCategory = new ProductCategory();
        productCategory.setMainCategory("Home");
        assertEquals("Home", productCategory.getMainCategory());
    }

    @Test
    public void testGetMainCategoryName() {
        ProductCategory productCategory = new ProductCategory();
        productCategory.setMainCategoryName("Electronic Devices");
        assertEquals("Electronic Devices", productCategory.getMainCategoryName());
    }

    @Test
    public void testSetMainCategoryName() {
        ProductCategory productCategory = new ProductCategory();
        productCategory.setMainCategoryName("Household Items");
        assertEquals("Household Items", productCategory.getMainCategoryName());
    }

    @Test
    public void testGetNumberOfProducts() {
        ProductCategory productCategory = new ProductCategory();
        productCategory.setNumberOfProducts(150);
        assertEquals(150L, productCategory.getNumberOfProducts());
    }

    @Test
    public void testSetNumberOfProducts() {
        ProductCategory productCategory = new ProductCategory();
        productCategory.setNumberOfProducts(200);
        assertEquals(200L, productCategory.getNumberOfProducts());
    }
}
