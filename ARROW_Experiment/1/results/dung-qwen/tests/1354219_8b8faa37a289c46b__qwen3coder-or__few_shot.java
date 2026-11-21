package org.beangle.commons.collection;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.beangle.commons.lang.Strings;
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
  public void testConstructorWithPropertyDesc() {
    Order order = new Order("name desc");
    assertEquals(order.getProperty(), "name");
    assertFalse(order.isAscending());
  }

  @Test
  public void testConstructorWithPropertyAsc() {
    Order order = new Order("name asc");
    assertEquals(order.getProperty(), "name");
    assertTrue(order.isAscending());
  }

  @Test
  public void testConstructorWithPropertyOnly() {
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
    order.setProperty("email");
    assertEquals(order.getProperty(), "email");
  }

  @Test
  public void testIsAscending() {
    Order order = new Order("score", false);
    assertFalse(order.isAscending());
  }

  @Test
  public void testSetAscending() {
    Order order = new Order("score", true);
    order.setAscending(false);
    assertFalse(order.isAscending());
  }

  @Test
  public void testIgnoreCase() {
    Order order = new Order("name", true);
    Order result = order.ignoreCase();
    assertEquals(result, order);
    // Note: We can't directly test ignoreCase flag since it's not exposed publicly
  }

  @Test
  public void testAsc() {
    Order order = Order.asc("id");
    assertEquals(order.getProperty(), "id");
    assertTrue(order.isAscending());
  }

  @Test
  public void testDesc() {
    Order order = Order.desc("id");
    assertEquals(order.getProperty(), "id");
    assertFalse(order.isAscending());
  }

  @Test
  public void testToSortStringEmptyList() {
    List<Order> orders = new ArrayList<Order>();
    String result = Order.toSortString(orders);
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
  public void testParseEmptyString() {
    List<Order> orders = Order.parse("");
    assertEquals(orders.size(), 0);
  }

  @Test
  public void testParseNullString() {
    List<Order> orders = Order.parse(null);
    assertEquals(orders.size(), 0);
  }

  @Test
  public void testParseSingleAsc() {
    List<Order> orders = Order.parse("name asc");
    assertEquals(orders.size(), 1);
    assertEquals(orders.get(0).getProperty(), "name");
    assertTrue(orders.get(0).isAscending());
  }

  @Test
  public void testParseSingleDesc() {
    List<Order> orders = Order.parse("name desc");
    assertEquals(orders.size(), 1);
    assertEquals(orders.get(0).getProperty(), "name");
    assertFalse(orders.get(0).isAscending());
  }

  @Test
  public void testParseSingleNoDirection() {
    List<Order> orders = Order.parse("name");
    assertEquals(orders.size(), 1);
    assertEquals(orders.get(0).getProperty(), "name");
    assertTrue(orders.get(0).isAscending());
  }

  @Test
  public void testParseMultiple() {
    List<Order> orders = Order.parse("name asc, age desc");
    assertEquals(orders.size(), 2);
    assertEquals(orders.get(0).getProperty(), "name");
    assertTrue(orders.get(0).isAscending());
    assertEquals(orders.get(1).getProperty(), "age");
    assertFalse(orders.get(1).isAscending());
  }

  @Test
  public void testToStringWithoutIgnoreCase() {
    Order order = new Order("name", true);
    assertEquals(order.toString(), "name asc");
  }

  @Test
  public void testToStringWithIgnoreCase() {
    Order order = new Order("name", true);
    order.ignoreCase();
    assertEquals(order.toString(), "lower(name) asc");
  }

  @Test
  public void testToStringDescWithoutIgnoreCase() {
    Order order = new Order("name", false);
    assertEquals(order.toString(), "name desc");
  }

  @Test
  public void testToStringDescWithIgnoreCase() {
    Order order = new Order("name", false);
    order.ignoreCase();
    assertEquals(order.toString(), "lower(name) desc");
  }
}
