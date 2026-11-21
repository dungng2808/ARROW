package com.sap.espm.model;

import junit.framework.TestCase;
import org.junit.Test;

public class ProductCategory_ArrowTest {

    @Test
    public void testGetAndSetCategory() {
        ProductCategory productCategory = new ProductCategory();
        String category = "Electronics";
        productCategory.setCategory(category);
        TestCase.assertEquals(category, productCategory.getCategory());
    }

    @Test
    public void testGetAndSetCategoryName() {
        ProductCategory productCategory = new ProductCategory();
        String categoryName = "Smartphones";
        productCategory.setCategoryName(categoryName);
        TestCase.assertEquals(categoryName, productCategory.getCategoryName());
    }

    @Test
    public void testGetAndSetMainCategory() {
        ProductCategory productCategory = new ProductCategory();
        String mainCategory = "Devices";
        productCategory.setMainCategory(mainCategory);
        TestCase.assertEquals(mainCategory, productCategory.getMainCategory());
    }

    @Test
    public void testGetAndSetMainCategoryName() {
        ProductCategory productCategory = new ProductCategory();
        String mainCategoryName = "Mobile Devices";
        productCategory.setMainCategoryName(mainCategoryName);
        TestCase.assertEquals(mainCategoryName, productCategory.getMainCategoryName());
    }

    @Test
    public void testGetAndSetNumberOfProducts() {
        ProductCategory productCategory = new ProductCategory();
        long numberOfProducts = 100L;
        productCategory.setNumberOfProducts(numberOfProducts);
        TestCase.assertEquals(numberOfProducts, productCategory.getNumberOfProducts());
    }
}
