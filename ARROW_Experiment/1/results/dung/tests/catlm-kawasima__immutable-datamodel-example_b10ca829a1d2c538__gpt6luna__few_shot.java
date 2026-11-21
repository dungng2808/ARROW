package example.complex;

import example.shared.Address;
import example.shared.OrderConstraint;
import java.time.LocalDate;
import java.util.EnumSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

public class Order_ArrowTest {

    @Test
    public void constructorInitializesOrder() {
        EnumSet<OrderConstraint> constraints = EnumSet.noneOf(OrderConstraint.class);
        Order order = new Order("order-1", constraints);

        assertEquals("order-1", order.getOrderId());
        assertSame(constraints, order.getConstraints());
        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        assertNull(order.getDeliveryAddress());
        assertNull(order.getDeliveryDate());
    }

    @Test
    public void deliverWithoutConstraintsUpdatesDeliveryDetailsAndStatus() {
        Order order = new Order("order-2", EnumSet.noneOf(OrderConstraint.class));
        Address address = null;
        LocalDate date = LocalDate.of(2024, 1, 6);

        order.deliver(address, date);

        assertEquals(OrderStatus.DELIVERING, order.getStatus());
        assertNull(order.getDeliveryAddress());
        assertEquals(date, order.getDeliveryDate());
    }

    @Test
    public void closeAfterDeliverySetsClosedStatus() {
        Order order = new Order("order-3", EnumSet.noneOf(OrderConstraint.class));
        order.deliver(null, LocalDate.of(2024, 1, 6));

        order.close();

        assertEquals(OrderStatus.CLOSED, order.getStatus());
    }

    @Test
    public void deliverWhenNotInProgressThrowsIllegalStateException() {
        Order order = new Order("order-4", EnumSet.noneOf(OrderConstraint.class));
        order.deliver(null, LocalDate.of(2024, 1, 6));

        assertThrows(IllegalStateException.class,
                () -> order.deliver(null, LocalDate.of(2024, 1, 7)));
    }

    @Test
    public void closeWhenNotDeliveringThrowsIllegalStateException() {
        Order order = new Order("order-5", EnumSet.noneOf(OrderConstraint.class));

        assertThrows(IllegalStateException.class, order::close);
    }
}
