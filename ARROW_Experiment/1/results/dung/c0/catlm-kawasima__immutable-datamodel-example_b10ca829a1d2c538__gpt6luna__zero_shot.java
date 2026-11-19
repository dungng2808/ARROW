package example.complex;

import example.shared.Address;
import example.shared.OrderConstraint;
import org.junit.Test;

import java.time.LocalDate;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Order_ArrowTest {
    @Test
    public void constructorInitializesOrder() {
        EnumSet<OrderConstraint> constraints = EnumSet.noneOf(OrderConstraint.class);
        Order order = new Order("order-1", constraints);

        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        assertEquals("order-1", order.getOrderId());
        assertEquals(constraints, order.getConstraints());
        assertNull(order.getDeliveryAddress());
        assertNull(order.getDeliveryDate());
    }

    @Test
    public void deliverUpdatesDeliveryDetailsAndStatus() {
        Order order = new Order("order-2", EnumSet.noneOf(OrderConstraint.class));
        Address address = null;
        LocalDate date = LocalDate.of(2024, 1, 8);

        order.deliver(address, date);

        assertEquals(OrderStatus.DELIVERING, order.getStatus());
        assertNull(order.getDeliveryAddress());
        assertEquals(date, order.getDeliveryDate());
    }

    @Test
    public void closeUpdatesStatus() {
        Order order = new Order("order-3", EnumSet.noneOf(OrderConstraint.class));
        order.deliver(null, LocalDate.of(2024, 1, 8));

        order.close();

        assertEquals(OrderStatus.CLOSED, order.getStatus());
    }

    @Test(expected = IllegalStateException.class)
    public void deliverRejectsOrderNotInProgress() {
        Order order = new Order("order-4", EnumSet.noneOf(OrderConstraint.class));
        order.deliver(null, LocalDate.of(2024, 1, 8));
        order.deliver(null, LocalDate.of(2024, 1, 9));
    }

    @Test(expected = IllegalStateException.class)
    public void closeRejectsOrderNotDelivering() {
        Order order = new Order("order-5", EnumSet.noneOf(OrderConstraint.class));
        order.close();
    }
}
