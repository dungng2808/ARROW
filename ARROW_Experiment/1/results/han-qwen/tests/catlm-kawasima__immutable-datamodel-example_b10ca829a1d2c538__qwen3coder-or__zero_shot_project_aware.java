package example.complex;

import am.ik.yavi.core.ConstraintViolationsException;
import example.shared.Address;
import example.shared.OrderConstraint;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;

import static example.shared.OrderConstraint.DELIVERY_WEEKDAY;
import static example.shared.OrderConstraint.SHIPPING_JAPAN_ONLY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class Order_ArrowTest {

    @Test
    public void testDeliverWithValidData() {
        Order order = new Order("123", EnumSet.noneOf(OrderConstraint.class));
        Address address = new Address("Japan", "Tokyo", "123-4567");
        LocalDate date = LocalDate.now();

        order.deliver(address, date);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERING);
        assertThat(order.getDeliveryAddress()).isEqualTo(address);
        assertThat(order.getDeliveryDate()).isEqualTo(date);
    }

    @Test
    public void testDeliverWithInvalidStatus() {
        Order order = new Order("123", EnumSet.noneOf(OrderConstraint.class));
        order.close(); // Set status to CLOSED

        assertThatThrownBy(() -> order.deliver(new Address("Japan", "Tokyo", "123-4567"), LocalDate.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Order status is not in progress");
    }

    @Test
    public void testDeliverWithNullAddress() {
        Order order = new Order("123", EnumSet.noneOf(OrderConstraint.class));

        assertThatThrownBy(() -> order.deliver(null, LocalDate.now()))
                .isInstanceOf(ConstraintViolationsException.class);
    }

    @Test
    public void testDeliverWithNullDate() {
        Order order = new Order("123", EnumSet.noneOf(OrderConstraint.class));
        Address address = new Address("Japan", "Tokyo", "123-4567");

        assertThatThrownBy(() -> order.deliver(address, null))
                .isInstanceOf(ConstraintViolationsException.class);
    }

    @Test
    public void testDeliverWithJapanOnlyConstraintAndNonJapaneseAddress() {
        Order order = new Order("123", EnumSet.of(SHIPPING_JAPAN_ONLY));
        Address address = new Address("USA", "New York", "12345");
        LocalDate date = LocalDate.now();

        assertThatThrownBy(() -> order.deliver(address, date))
                .isInstanceOf(ConstraintViolationsException.class);
    }

    @Test
    public void testDeliverWithJapanOnlyConstraintAndJapaneseAddress() {
        Order order = new Order("123", EnumSet.of(SHIPPING_JAPAN_ONLY));
        Address address = new Address("Japan", "Tokyo", "123-4567");
        LocalDate date = LocalDate.now();

        order.deliver(address, date);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERING);
    }

    @Test
    public void testDeliverWithWeekdayConstraintAndWeekendDate() {
        Order order = new Order("123", EnumSet.of(DELIVERY_WEEKDAY));
        Address address = new Address("Japan", "Tokyo", "123-4567");
        LocalDate date = LocalDate.of(2023, 10, 14); // Saturday

        assertThatThrownBy(() -> order.deliver(address, date))
                .isInstanceOf(ConstraintViolationsException.class);
    }

    @Test
    public void testDeliverWithWeekdayConstraintAndWeekdayDate() {
        Order order = new Order("123", EnumSet.of(DELIVERY_WEEKDAY));
        Address address = new Address("Japan", "Tokyo", "123-4567");
        LocalDate date = LocalDate.of(2023, 10, 12); // Thursday

        order.deliver(address, date);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERING);
    }

    @Test
    public void testCloseWithValidStatus() {
        Order order = new Order("123", EnumSet.noneOf(OrderConstraint.class));
        Address address = new Address("Japan", "Tokyo", "123-4567");
        LocalDate date = LocalDate.now();
        order.deliver(address, date);

        order.close();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CLOSED);
    }

    @Test
    public void testCloseWithInvalidStatus() {
        Order order = new Order("123", EnumSet.noneOf(OrderConstraint.class));

        assertThatThrownBy(() -> order.close())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Order status is not delivering");
    }

    @Test
    public void testGetters() {
        EnumSet<OrderConstraint> constraints = EnumSet.of(SHIPPING_JAPAN_ONLY);
        Order order = new Order("123", constraints);

        assertThat(order.getOrderId()).isEqualTo("123");
        assertThat(order.getConstraints()).isEqualTo(constraints);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
        assertThat(order.getDeliveryAddress()).isNull();
        assertThat(order.getDeliveryDate()).isNull();
    }
}
