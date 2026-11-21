package org.beangle.commons.collection;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

public class Order_ArrowTest {

  @Test
  public void defaultConstructorAndSetters() {
    Order order = new Order();
    assertNull(order.getProperty());
    assertFalse(order.isAscending());

    order.setProperty("name");
    order.setAscending(true);
    assertEquals("name", order.getProperty());
    assertTrue(order.isAscending());
    assertEquals("name asc", order.toString());
  }

  @Test
  public void constructorWithPropertyAndDirection() {
    Order order = new Order("age", false);
    assertEquals("age", order.getProperty());
    assertFalse(order.isAscending());
    assertEquals("age desc", order.toString());
  }

  @Test
  public void constructorParsesAscendingProperty() {
    Order order = new Order("  name asc  ");
    assertEquals("name", order.getProperty());
    assertTrue(order.isAscending());
  }

  @Test
  public void constructorParsesDescendingProperty() {
    Order order = new Order("  age desc  ");
    assertEquals("age", order.getProperty());
    assertFalse(order.isAscending());
  }

  @Test(expected = RuntimeException.class)
  public void constructorRejectsMultipleProperties() {
    new Order("name, age");
  }

  @Test
  public void ignoreCaseReturnsSameOrderAndChangesString() {
    Order order = Order.asc("name");
    assertSame(order, order.ignoreCase());
    assertEquals("lower(name) asc", order.toString());
  }

  @Test
  public void staticFactoriesCreateOrders() {
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
    List<Order> orders = new ArrayList<Order>();
    orders.add(Order.asc("name"));
    orders.add(Order.desc("age"));

    assertEquals("order by name,age desc", Order.toSortString(orders));
  }

  @Test
  public void parseBlankStringReturnsEmptyList() {
    assertTrue(Order.parse("  ").isEmpty());
  }

  @Test
  public void parseOrdersWithDirections() {
    List<Order> orders = Order.parse("name asc, age desc, title");

    assertEquals(3, orders.size());
    assertEquals("name", orders.get(0).getProperty());
    assertTrue(orders.get(0).isAscending());
    assertEquals(" age", orders.get(1).getProperty());
    assertFalse(orders.get(1).isAscending());
    assertEquals(" title", orders.get(2).getProperty());
    assertTrue(orders.get(2).isAscending());
  }

  @Test
  public void parseSkipsBlankEntries() {
    List<Order> orders = Order.parse("name,, age");

    assertEquals(2, orders.size());
    assertEquals("name", orders.get(0).getProperty());
    assertEquals(" age", orders.get(1).getProperty());
  }
}
