package org.beangle.commons.collection;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class Order_ArrowTest {

  @Test
  public void testDefaultConstructor() {
    Order order = new Order();
    Assert.assertNull(order.getProperty());
    Assert.assertTrue(order.isAscending());
  }

  @Test
  public void testConstructorWithPropertyAndAscending() {
    Order order = new Order("name", false);
    Assert.assertEquals(order.getProperty(), "name");
    Assert.assertFalse(order.isAscending());
  }

  @Test
  public void testConstructorWithPropertyDesc() {
    Order order = new Order("name desc");
    Assert.assertEquals(order.getProperty(), "name");
    Assert.assertFalse(order.isAscending());
  }

  @Test
  public void testConstructorWithPropertyAsc() {
    Order order = new Order("name asc");
    Assert.assertEquals(order.getProperty(), "name");
    Assert.assertTrue(order.isAscending());
  }

  @Test
  public void testConstructorWithPropertyOnly() {
    Order order = new Order("name");
    Assert.assertEquals(order.getProperty(), "name");
    Assert.assertTrue(order.isAscending());
  }

  @Test
  public void testGetProperty() {
    Order order = new Order("age", true);
    Assert.assertEquals(order.getProperty(), "age");
  }

  @Test
  public void testSetProperty() {
    Order order = new Order();
    order.setProperty("email");
    Assert.assertEquals(order.getProperty(), "email");
  }

  @Test
  public void testIsAscending() {
    Order order = new Order("score", false);
    Assert.assertFalse(order.isAscending());
  }

  @Test
  public void testSetAscending() {
    Order order = new Order("score", true);
    order.setAscending(false);
    Assert.assertFalse(order.isAscending());
  }

  @Test
  public void testIgnoreCase() {
    Order order = new Order("name", true);
    Order result = order.ignoreCase();
    Assert.assertSame(result, order);
    Assert.assertTrue(order.isAscending());
    Assert.assertEquals(order.getProperty(), "name");
  }

  @Test
  public void testAsc() {
    Order order = Order.asc("id");
    Assert.assertEquals(order.getProperty(), "id");
    Assert.assertTrue(order.isAscending());
  }

  @Test
  public void testDesc() {
    Order order = Order.desc("id");
    Assert.assertEquals(order.getProperty(), "id");
    Assert.assertFalse(order.isAscending());
  }

  @Test
  public void testToSortStringEmptyList() {
    List<Order> orders = new ArrayList<Order>();
    String result = Order.toSortString(orders);
    Assert.assertEquals(result, "");
  }

  @Test
  public void testToSortStringNullList() {
    String result = Order.toSortString(null);
    Assert.assertEquals(result, "");
  }

  @Test
  public void testToSortStringSingleAsc() {
    List<Order> orders = new ArrayList<Order>();
    orders.add(new Order("name", true));
    String result = Order.toSortString(orders);
    Assert.assertEquals(result, "order by name");
  }

  @Test
  public void testToSortStringSingleDesc() {
    List<Order> orders = new ArrayList<Order>();
    orders.add(new Order("name", false));
    String result = Order.toSortString(orders);
    Assert.assertEquals(result, "order by name desc");
  }

  @Test
  public void testToSortStringMultiple() {
    List<Order> orders = new ArrayList<Order>();
    orders.add(new Order("name", true));
    orders.add(new Order("age", false));
    String result = Order.toSortString(orders);
    Assert.assertEquals(result, "order by name,age desc");
  }

  @Test
  public void testParseNull() {
    List<Order> result = Order.parse(null);
    Assert.assertNotNull(result);
    Assert.assertTrue(result.isEmpty());
  }

  @Test
  public void testParseEmptyString() {
    List<Order> result = Order.parse("");
    Assert.assertNotNull(result);
    Assert.assertTrue(result.isEmpty());
  }

  @Test
  public void testParseBlankString() {
    List<Order> result = Order.parse("   ");
    Assert.assertNotNull(result);
    Assert.assertTrue(result.isEmpty());
  }

  @Test
  public void testParseSingleAsc() {
    List<Order> result = Order.parse("name asc");
    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 1);
    Assert.assertEquals(result.get(0).getProperty(), "name");
    Assert.assertTrue(result.get(0).isAscending());
  }

  @Test
  public void testParseSingleDesc() {
    List<Order> result = Order.parse("name desc");
    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 1);
    Assert.assertEquals(result.get(0).getProperty(), "name");
    Assert.assertFalse(result.get(0).isAscending());
  }

  @Test
  public void testParseSingleNoDirection() {
    List<Order> result = Order.parse("name");
    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 1);
    Assert.assertEquals(result.get(0).getProperty(), "name");
    Assert.assertTrue(result.get(0).isAscending());
  }

  @Test
  public void testParseMultiple() {
    List<Order> result = Order.parse("name asc, age desc, email");
    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 3);
    Assert.assertEquals(result.get(0).getProperty(), "name");
    Assert.assertTrue(result.get(0).isAscending());
    Assert.assertEquals(result.get(1).getProperty(), "age");
    Assert.assertFalse(result.get(1).isAscending());
    Assert.assertEquals(result.get(2).getProperty(), "email");
    Assert.assertTrue(result.get(2).isAscending());
  }

  @Test
  public void testToStringWithoutIgnoreCase() {
    Order order = new Order("name", true);
    String result = order.toString();
    Assert.assertEquals(result, "name asc");
  }

  @Test
  public void testToStringWithIgnoreCase() {
    Order order = new Order("name", true);
    order.ignoreCase();
    String result = order.toString();
    Assert.assertEquals(result, "lower(name) asc");
  }

  @Test
  public void testToStringDescWithoutIgnoreCase() {
    Order order = new Order("name", false);
    String result = order.toString();
    Assert.assertEquals(result, "name desc");
  }

  @Test
  public void testToStringDescWithIgnoreCase() {
    Order order = new Order("name", false);
    order.ignoreCase();
    String result = order.toString();
    Assert.assertEquals(result, "lower(name) desc");
  }
}
