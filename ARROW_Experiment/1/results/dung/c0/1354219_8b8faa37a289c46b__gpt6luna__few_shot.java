package org.beangle.commons.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class Order_ArrowTest {

  @Test
  public void defaultConstructorInitializesFields() {
    Order order = new Order();

    assertEquals(null, order.getProperty());
    assertFalse(order.isAscending());
    assertEquals("null desc", order.toString());
  }

  @Test
  public void constructorWithPropertyAndDirection() {
    Order order = new Order("name", true);

    assertEquals("name", order.getProperty());
    assertTrue(order.isAscending());
    assertEquals("name asc", order.toString());
  }

  @Test
  public void constructorParsesAscendingProperty() {
    Order order = new Order("  name asc");

    assertEquals("name", order.getProperty());
    assertTrue(order.isAscending());
    assertEquals("name asc", order.toString());
  }

  @Test
  public void constructorParsesDescendingProperty() {
    Order order = new Order("name desc");

    assertEquals("name", order.getProperty());
    assertFalse(order.isAscending());
    assertEquals("name desc", order.toString());
  }

  @Test(expected = RuntimeException.class)
  public void constructorRejectsMultipleProperties() {
    new Order("name, age");
  }

  @Test
  public void settersUpdatePropertyAndDirection() {
    Order order = new Order();
    order.setProperty("age");
    order.setAscending(true);

    assertEquals("age", order.getProperty());
    assertTrue(order.isAscending());
    assertEquals("age asc", order.toString());
  }

  @Test
  public void ignoreCaseReturnsSameOrderAndChangesString() {
    Order order = new Order("name", true);

    assertSame(order, order.ignoreCase());
    assertEquals("lower(name) asc", order.toString());
  }

  @Test
  public void staticFactoriesCreateAscendingAndDescendingOrders() {
    Order ascending = Order.asc("name");
    Order descending = Order.desc("age");

    assertEquals("name", ascending.getProperty());
    assertTrue(ascending.isAscending());
    assertEquals("age", descending.getProperty());
    assertFalse(descending.isAscending());
  }

  @Test
  public void toSortStringHandlesNullAndEmptyLists() {
    assertEquals("", Order.toSortString(null));
    assertEquals("", Order.toSortString(new ArrayList<Order>()));
  }

  @Test
  public void toSortStringFormatsOrders() {
    List<Order> orders = Arrays.asList(Order.asc("name"), Order.desc("age"));

    assertEquals("order by name,age desc", Order.toSortString(orders));
  }

  @Test
  public void parseReturnsEmptyListForBlankInput() {
    assertTrue(Order.parse("  ").isEmpty());
  }

  @Test
  public void parseHandlesDirectionsAndSkipsBlankEntries() {
    List<Order> orders = Order.parse("name asc, age desc, status, ");

    assertEquals(3, orders.size());
    assertEquals("name", orders.get(0).getProperty());
    assertTrue(orders.get(0).isAscending());
    assertEquals("age", orders.get(1).getProperty());
    assertFalse(orders.get(1).isAscending());
    assertEquals("status", orders.get(2).getProperty());
    assertTrue(orders.get(2).isAscending());
  }
}
