package net.krotscheck.kangaroo.authz.common.database.util;

import net.krotscheck.kangaroo.common.response.SortOrder;
import org.hibernate.criterion.Order;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SortUtil_ArrowTest {

    @Test
    public void orderReturnsAscendingOrderForAscendingSort() {
        Order result = SortUtil.order(SortOrder.ASC, "name");
        assertEquals(Order.asc("name").toString(), result.toString());
    }

    @Test
    public void orderReturnsDescendingOrderForDescendingSort() {
        Order result = SortUtil.order(SortOrder.DESC, "name");
        assertEquals(Order.desc("name").toString(), result.toString());
    }

    @Test
    public void orderReturnsAscendingOrderForNullSort() {
        Order result = SortUtil.order(null, "name");
        assertEquals(Order.asc("name").toString(), result.toString());
    }
}
