package example.complex;

import am.ik.yavi.core.ConstraintViolationsException;
import example.shared.Address;
import example.shared.OrderConstraint;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;

import static example.shared.OrderConstraint.DELIVERY_WEEKDAY;
import static example.shared.OrderConstraint.SHIPPING_JAPAN_ONLY;
import static org.junit.Assert.*;

public class Order_ArrowTest {

    @Test
    public void testConstructor() {
        EnumSet<OrderConstraint> constraints = EnumSet.of(SHIPPING_JAPAN_ONLY);
        Order order = new Order("ORDER123", constraints);
        
        assertEquals("ORDER123", order.getOrderId());
        assertEquals(constraints, order.getConstraints());
        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
    }

    @Test
    public void testDeliverWithValidData() throws ConstraintViolationsException {
        EnumSet<OrderConstraint> constraints = EnumSet.of(SHIPPING_JAPAN_ONLY, DELIVERY_WEEKDAY);
        Order order = new Order("ORDER123", constraints);
        
        Address address = new Address("JP", "Tokyo", "123-4567");
        LocalDate date = LocalDate.of(2023, 10, 10); // Tuesday
        
        order.deliver(address, date);
        
        assertEquals(OrderStatus.DELIVERING, order.getStatus());
        assertEquals(address, order.getDeliveryAddress());
        assertEquals(date, order.getDeliveryDate());
    }

    @Test(expected = IllegalStateException.class)
    public void testDeliverWithInvalidStatus() {
        EnumSet<OrderConstraint> constraints = EnumSet.of(SHIPPING_JAPAN_ONLY);
        Order order = new Order("ORDER123", constraints);
        order.close(); // Change status to CLOSED
        
        Address address = new Address("JP", "Tokyo", "123-4567");
        LocalDate date = LocalDate.of(2023, 10, 10);
        
        order.deliver(address, date); // Should throw IllegalStateException
    }

    @Test(expected = ConstraintViolationsException.class)
    public void testDeliverWithInvalidJapanShipping() throws ConstraintViolationsException {
        EnumSet<OrderConstraint> constraints = EnumSet.of(SHIPPING_JAPAN_ONLY);
        Order order = new Order("ORDER123", constraints);
        
        Address address = new Address("US", "New York", "12345"); // Not Japan
        LocalDate date = LocalDate.of(2023, 10, 10);
        
        order.deliver(address, date); // Should throw ConstraintViolationsException
    }

    @Test(expected = ConstraintViolationsException.class)
    public void testDeliverWithInvalidWeekday() throws ConstraintViolationsException {
        EnumSet<OrderConstraint> constraints = EnumSet.of(DELIVERY_WEEKDAY);
        Order order = new Order("ORDER123", constraints);
        
        Address address = new Address("JP", "Tokyo", "123-4567");
        LocalDate date = LocalDate.of(2023, 10, 14); // Saturday
        
        order.deliver(address, date); // Should throw ConstraintViolationsException
    }

    @Test
    public void testCloseWithValidStatus() {
        EnumSet<OrderConstraint> constraints = EnumSet.of(SHIPPING_JAPAN_ONLY);
        Order order = new Order("ORDER123", constraints);
        
        Address address = new Address("JP", "Tokyo", "123-4567");
        LocalDate date = LocalDate.of(2023, 10, 10);
        order.deliver(address, date);
        
        order.close();
        
        assertEquals(OrderStatus.CLOSED, order.getStatus());
    }

    @Test(expected = IllegalStateException.class)
    public void testCloseWithInvalidStatus() {
        EnumSet<OrderConstraint> constraints = EnumSet.of(SHIPPING_JAPAN_ONLY);
        Order order = new Order("ORDER123", constraints);
        
        order.close(); // Should throw IllegalStateException
    }

    @Test
    public void testGetters() {
        EnumSet<OrderConstraint> constraints = EnumSet.of(SHIPPING_JAPAN_ONLY, DELIVERY_WEEKDAY);
        Order order = new Order("ORDER123", constraints);
        
        assertEquals("ORDER123", order.getOrderId());
        assertEquals(constraints, order.getConstraints());
        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        assertNull(order.getDeliveryAddress());
        assertNull(order.getDeliveryDate());
    }
}
