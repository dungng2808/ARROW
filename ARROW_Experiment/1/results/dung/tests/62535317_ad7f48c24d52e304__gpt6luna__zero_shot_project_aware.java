package com.sap.espm.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ProductCategory_ArrowTest {

    @Test
    public void categoryCanBeSetAndRetrieved() {
        ProductCategory productCategory = new ProductCategory();

        productCategory.setCategory("CAT-1");

        assertEquals("CAT-1", productCategory.getCategory());
    }

    @Test
    public void categoryIsNullByDefault() {
        ProductCategory productCategory = new ProductCategory();

        assertNull(productCategory.getCategory());
    }

    @Test
    public void categoryNameCanBeSetAndRetrieved() {
        ProductCategory productCategory = new ProductCategory();

        productCategory.setCategoryName("Accessories");

        assertEquals("Accessories", productCategory.getCategoryName());
    }

    @Test
    public void categoryNameIsNullByDefault() {
        ProductCategory productCategory = new ProductCategory();

        assertNull(productCategory.getCategoryName());
    }

    @Test
    public void mainCategoryCanBeSetAndRetrieved() {
        ProductCategory productCategory = new ProductCategory();

        productCategory.setMainCategory("MAIN-1");

        assertEquals("MAIN-1", productCategory.getMainCategory());
    }

    @Test
    public void mainCategoryIsNullByDefault() {
        ProductCategory productCategory = new ProductCategory();

        assertNull(productCategory.getMainCategory());
    }

    @Test
    public void mainCategoryNameCanBeSetAndRetrieved() {
        ProductCategory productCategory = new ProductCategory();

        productCategory.setMainCategoryName("Electronics");

        assertEquals("Electronics", productCategory.getMainCategoryName());
    }

    @Test
    public void mainCategoryNameIsNullByDefault() {
        ProductCategory productCategory = new ProductCategory();

        assertNull(productCategory.getMainCategoryName());
    }

    @Test
    public void numberOfProductsCanBeSetAndRetrieved() {
        ProductCategory productCategory = new ProductCategory();

        productCategory.setNumberOfProducts(12L);

        assertEquals(12L, productCategory.getNumberOfProducts());
    }

    @Test
    public void numberOfProductsIsZeroByDefault() {
        ProductCategory productCategory = new ProductCategory();

        assertEquals(0L, productCategory.getNumberOfProducts());
    }
}
