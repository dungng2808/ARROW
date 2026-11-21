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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Order_ArrowTest {

    @Test
    public void testDeliverWithValidData() {
        Order order = new Order("123", EnumSet.noneOf(OrderConstraint.class));
        Address address = new Address("JP", "Tokyo", "100-0001");
        LocalDate date = LocalDate.now();
        
        order.deliver(address, date);
        
        assertEquals(OrderStatus.DELIVERING, order.getStatus());
    }

    @Test(expected = IllegalStateException.class)
    public void testDeliverWithWrongStatus() {
        Order order = new Order("123", EnumSet.noneOf(OrderConstraint.class));
        order.close(); // Set status to CLOSED
        
        Address address = new Address("JP", "Tokyo", "100-0001");
        LocalDate date = LocalDate.now();
        
        order.deliver(address, date);
    }

    @Test
    public void testDeliverWithJapanOnlyConstraintAndValidAddress() {
        EnumSet<OrderConstraint> constraints = EnumSet.of(SHIPPING_JAPAN_ONLY);
        Order order = new Order("123", constraints);
        Address address = new Address("JP", "Tokyo", "100-0001");
        LocalDate date = LocalDate.now();
        
        order.deliver(address, date);
        
        assertEquals(OrderStatus.DELIVERING, order.getStatus());
    }

    @Test(expected = ConstraintViolationsException.class)
    public void testDeliverWithJapanOnlyConstraintAndInvalidAddress() {
        EnumSet<OrderConstraint> constraints = EnumSet.of(SHIPPING_JAPAN_ONLY);
        Order order = new Order("123", constraints);
        Address address = new Address("US", "New York", "10001");
        LocalDate date = LocalDate.now();
        
        order.deliver(address, date);
    }

    @Test
    public void testDeliverWithDeliveryWeekdayConstraintAndValidDay() {
        EnumSet<OrderConstraint> constraints = EnumSet.of(DELIVERY_WEEKDAY);
        Order order = new Order("123", constraints);
        Address address = new Address("JP", "Tokyo", "100-0001");
        LocalDate date = LocalDate.now().with(DayOfWeek.MONDAY); // Monday is valid
        
        order.deliver(address, date);
        
        assertEquals(OrderStatus.DELIVERING, order.getStatus());
    }

    @Test(expected = ConstraintViolationsException.class)
    public void testDeliverWithDeliveryWeekdayConstraintAndInvalidDay() {
        EnumSet<OrderConstraint> constraints = EnumSet.of(DELIVERY_WEEKDAY);
        Order order = new Order("123", constraints);
        Address address = new Address("JP", "Tokyo", "100-0001");
        LocalDate date = LocalDate.now().with(DayOfWeek.SATURDAY); // Saturday is invalid
        
        order.deliver(address, date);
    }

    @Test(expected = ConstraintViolationsException.class)
    public void testDeliverWithBothConstraintsAndInvalidData() {
        EnumSet<OrderConstraint> constraints = EnumSet.of(SHIPPING_JAPAN_ONLY, DELIVERY_WEEKDAY);
        Order order = new Order("123", constraints);
        Address address = new Address("US", "New York", "10001"); // Invalid country
        LocalDate date = LocalDate.now().with(DayOfWeek.SATURDAY); // Invalid day
        
        order.deliver(address, date);
    }

    @Test
    public void testCloseWithValidStatus() {
        Order order = new Order("123", EnumSet.noneOf(OrderConstraint.class));
        Address address = new Address("JP", "Tokyo", "100-0001");
        LocalDate date = LocalDate.now();
        order.deliver(address, date);
        
        order.close();
        
        assertEquals(OrderStatus.CLOSED, order.getStatus());
    }

    @Test(expected = IllegalStateException.class)
    public void testCloseWithWrongStatus() {
        Order order = new Order("123", EnumSet.noneOf(OrderConstraint.class));
        
        order.close();
    }

    @Test
    public void testGetters() {
        EnumSet<OrderConstraint> constraints = EnumSet.of(SHIPPING_JAPAN_ONLY);
        Order order = new Order("123", constraints);
        
        assertEquals("123", order.getOrderId());
        assertEquals(constraints, order.getConstraints());
        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        assertEquals(null, order.getDeliveryAddress());
        assertEquals(null, order.getDeliveryDate());
    }
}
