package org.beangle.commons.collection;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

public class Order_ArrowTest {

  @Test
  public void testDefaultConstructor() {
    Order order = new Order();
    assertEquals(order.getProperty(), null);
    assertTrue(order.isAscending());
  }

  @Test
  public void testConstructorWithPropertyAndAscending() {
    Order order = new Order("name", false);
    assertEquals(order.getProperty(), "name");
    assertFalse(order.isAscending());
  }

  @Test
  public void testConstructorWithPropertyOnlyAsc() {
    Order order = new Order("name asc");
    assertEquals(order.getProperty(), "name");
    assertTrue(order.isAscending());
  }

  @Test
  public void testConstructorWithPropertyOnlyDesc() {
    Order order = new Order("name desc");
    assertEquals(order.getProperty(), "name");
    assertFalse(order.isAscending());
  }

  @Test
  public void testConstructorWithPropertyOnlyNoDirection() {
    Order order = new Order("name");
    assertEquals(order.getProperty(), "name");
    assertTrue(order.isAscending());
  }

  @Test
  public void testGetProperty() {
    Order order = new Order("age", true);
    assertEquals(order.getProperty(), "age");
  }

  @Test
  public void testSetProperty() {
    Order order = new Order();
    order.setProperty("height");
    assertEquals(order.getProperty(), "height");
  }

  @Test
  public void testIsAscending() {
    Order order = new Order("name", false);
    assertFalse(order.isAscending());
  }

  @Test
  public void testSetAscending() {
    Order order = new Order("name", true);
    order.setAscending(false);
    assertFalse(order.isAscending());
  }

  @Test
  public void testIgnoreCase() {
    Order order = new Order("name", true);
    Order result = order.ignoreCase();
    assertEquals(result, order);
    assertTrue(order.ignoreCase != false);
  }

  @Test
  public void testAsc() {
    Order order = Order.asc("name");
    assertEquals(order.getProperty(), "name");
    assertTrue(order.isAscending());
  }

  @Test
  public void testDesc() {
    Order order = Order.desc("name");
    assertEquals(order.getProperty(), "name");
    assertFalse(order.isAscending());
  }

  @Test
  public void testToSortStringEmptyList() {
    List<Order> orders = new ArrayList<Order>();
    String result = Order.toSortString(orders);
    assertEquals(result, "");
  }

  @Test
  public void testToSortStringNullList() {
    String result = Order.toSortString(null);
    assertEquals(result, "");
  }

  @Test
  public void testToSortStringSingleAsc() {
    List<Order> orders = new ArrayList<Order>();
    orders.add(new Order("name", true));
    String result = Order.toSortString(orders);
    assertEquals(result, "order by name");
  }

  @Test
  public void testToSortStringSingleDesc() {
    List<Order> orders = new ArrayList<Order>();
    orders.add(new Order("name", false));
    String result = Order.toSortString(orders);
    assertEquals(result, "order by name desc");
  }

  @Test
  public void testToSortStringMultiple() {
    List<Order> orders = new ArrayList<Order>();
    orders.add(new Order("name", true));
    orders.add(new Order("age", false));
    String result = Order.toSortString(orders);
    assertEquals(result, "order by name,age desc");
  }

  @Test
  public void testParseNull() {
    List<Order> result = Order.parse(null);
    assertEquals(result.size(), 0);
  }

  @Test
  public void testParseEmptyString() {
    List<Order> result = Order.parse("");
    assertEquals(result.size(), 0);
  }

  @Test
  public void testParseBlankString() {
    List<Order> result = Order.parse("   ");
    assertEquals(result.size(), 0);
  }

  @Test
  public void testParseSingleAsc() {
    List<Order> result = Order.parse("name asc");
    assertEquals(result.size(), 1);
    assertEquals(result.get(0).getProperty(), "name");
    assertTrue(result.get(0).isAscending());
  }

  @Test
  public void testParseSingleDesc() {
    List<Order> result = Order.parse("name desc");
    assertEquals(result.size(), 1);
    assertEquals(result.get(0).getProperty(), "name");
    assertFalse(result.get(0).isAscending());
  }

  @Test
  public void testParseSingleNoDirection() {
    List<Order> result = Order.parse("name");
    assertEquals(result.size(), 1);
    assertEquals(result.get(0).getProperty(), "name");
    assertTrue(result.get(0).isAscending());
  }

  @Test
  public void testParseMultiple() {
    List<Order> result = Order.parse("name asc, age desc");
    assertEquals(result.size(), 2);
    assertEquals(result.get(0).getProperty(), "name");
    assertTrue(result.get(0).isAscending());
    assertEquals(result.get(1).getProperty(), "age");
    assertFalse(result.get(1).isAscending());
  }

  @Test
  public void testToStringWithoutIgnoreCase() {
    Order order = new Order("name", true);
    String result = order.toString();
    assertEquals(result, "name asc");
  }

  @Test
  public void testToStringWithIgnoreCase() {
    Order order = new Order("name", true);
    order.ignoreCase();
    String result = order.toString();
    assertEquals(result, "lower(name) asc");
  }

  @Test
  public void testToStringDescWithoutIgnoreCase() {
    Order order = new Order("name", false);
    String result = order.toString();
    assertEquals(result, "name desc");
  }

  @Test
  public void testToStringDescWithIgnoreCase() {
    Order order = new Order("name", false);
    order.ignoreCase();
    String result = order.toString();
    assertEquals(result, "lower(name) desc");
  }
}
