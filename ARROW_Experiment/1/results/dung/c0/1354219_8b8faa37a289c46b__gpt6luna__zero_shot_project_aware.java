package org.beangle.commons.collection;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

public class Order_ArrowTest {

  @Test
  public void defaultConstructorAndSetters() {
    Order order = new Order();
    assertEquals(order.getProperty(), null);
    assertFalse(order.isAscending());

    order.setProperty("name");
    order.setAscending(true);
    assertEquals(order.getProperty(), "name");
    assertTrue(order.isAscending());
    assertEquals(order.toString(), "name asc");
  }

  @Test
  public void constructorWithPropertyAndDirection() {
    Order order = new Order("createdAt", false);
    assertEquals(order.getProperty(), "createdAt");
    assertFalse(order.isAscending());
    assertEquals(order.toString(), "createdAt desc");
  }

  @Test
  public void constructorParsesAscendingProperty() {
    Order order = new Order("  name asc  ");
    assertEquals(order.getProperty(), "name");
    assertTrue(order.isAscending());
  }

  @Test
  public void constructorParsesDescendingProperty() {
    Order order = new Order("  name desc  ");
    assertEquals(order.getProperty(), "name");
    assertFalse(order.isAscending());
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void constructorRejectsMultipleProperties() {
    new Order("name, age");
  }

  @Test
  public void ignoreCaseReturnsSameOrderAndChangesString() {
    Order order = new Order("name", true);
    assertSame(order.ignoreCase(), order);
    assertEquals(order.toString(), "lower(name) asc");
  }

  @Test
  public void staticFactoriesCreateOrders() {
    Order ascending = Order.asc("name");
    Order descending = Order.desc("age");

    assertEquals(ascending.getProperty(), "name");
    assertTrue(ascending.isAscending());
    assertEquals(descending.getProperty(), "age");
    assertFalse(descending.isAscending());
  }

  @Test
  public void toSortStringHandlesNullAndEmptyLists() {
    assertEquals(Order.toSortString(null), "");
    assertEquals(Order.toSortString(new ArrayList<Order>()), "");
  }

  @Test
  public void toSortStringFormatsOrders() {
    List<Order> orders = new ArrayList<Order>();
    orders.add(Order.asc("name"));
    orders.add(Order.desc("age"));

    assertEquals(Order.toSortString(orders), "order by name,age desc");
  }

  @Test
  public void parseBlankStringReturnsEmptyList() {
    assertTrue(Order.parse("  ").isEmpty());
  }

  @Test
  public void parseOrdersAndDirections() {
    List<Order> orders = Order.parse("name asc, age desc, title");

    assertEquals(orders.size(), 3);
    assertEquals(orders.get(0).getProperty(), "name");
    assertTrue(orders.get(0).isAscending());
    assertEquals(orders.get(1).getProperty(), " age");
    assertFalse(orders.get(1).isAscending());
    assertEquals(orders.get(2).getProperty(), " title");
    assertTrue(orders.get(2).isAscending());
  }

  @Test
  public void parseSkipsBlankEntries() {
    List<Order> orders = Order.parse("name,, ,age");

    assertEquals(orders.size(), 2);
    assertEquals(orders.get(0).getProperty(), "name");
    assertEquals(orders.get(1).getProperty(), "age");
  }
}
