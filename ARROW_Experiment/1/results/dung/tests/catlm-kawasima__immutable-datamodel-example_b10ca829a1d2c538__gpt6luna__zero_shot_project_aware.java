package example.complex;

import am.ik.yavi.core.ConstraintViolationsException;
import example.shared.Address;
import example.shared.OrderConstraint;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Order_ArrowTest {

    @Test
    public void createsOrderInProgressWithGivenIdAndConstraints() {
        EnumSet<OrderConstraint> constraints = EnumSet.noneOf(OrderConstraint.class);
        Order order = new Order("order-1", constraints);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
        assertThat(order.getOrderId()).isEqualTo("order-1");
        assertThat(order.getConstraints()).isSameAs(constraints);
        assertThat(order.getDeliveryAddress()).isNull();
        assertThat(order.getDeliveryDate()).isNull();
    }

    @Test
    public void deliversWithoutConstraintsAndStoresDeliveryDetails() {
        Order order = new Order("order-2", EnumSet.noneOf(OrderConstraint.class));
        Address address = new Address("JP");

        order.deliver(address, LocalDate.of(2024, 1, 8));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERING);
        assertThat(order.getDeliveryAddress()).isSameAs(address);
        assertThat(order.getDeliveryDate()).isEqualTo(LocalDate.of(2024, 1, 8));
    }

    @Test
    public void closesDeliveringOrder() {
        Order order = new Order("order-3", EnumSet.noneOf(OrderConstraint.class));
        order.deliver(new Address("JP"), LocalDate.of(2024, 1, 8));

        order.close();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CLOSED);
    }

    @Test
    public void rejectsDeliveryWhenOrderIsNotInProgress() {
        Order order = new Order("order-4", EnumSet.noneOf(OrderConstraint.class));
        order.deliver(new Address("JP"), LocalDate.of(2024, 1, 8));

        assertThrows(IllegalStateException.class,
                () -> order.deliver(new Address("JP"), LocalDate.of(2024, 1, 9)));
    }

    @Test
    public void rejectsClosingOrderThatIsNotDelivering() {
        Order order = new Order("order-5", EnumSet.noneOf(OrderConstraint.class));

        assertThrows(IllegalStateException.class, order::close);
    }

    @Test
    public void rejectsDeliveryThatViolatesShippingJapanOnlyConstraint() {
        EnumSet<OrderConstraint> constraints = EnumSet.of(OrderConstraint.SHIPPING_JAPAN_ONLY);
        Order order = new Order("order-6", constraints);

        assertThrows(ConstraintViolationsException.class,
                () -> order.deliver(new Address("US"), LocalDate.of(2024, 1, 8)));
    }

    @Test
    public void rejectsDeliveryOnWeekendWhenWeekdayConstraintApplies() {
        EnumSet<OrderConstraint> constraints = EnumSet.of(OrderConstraint.DELIVERY_WEEKDAY);
        Order order = new Order("order-7", constraints);

        assertThrows(ConstraintViolationsException.class,
                () -> order.deliver(new Address("JP"), LocalDate.of(2024, 1, 6)));
    }
}
