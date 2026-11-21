package net.krotscheck.kangaroo.authz.common.database.util;

import net.krotscheck.kangaroo.common.response.SortOrder;
import org.hibernate.criterion.Order;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SortUtil_ArrowTest {

    @Test
    public void orderReturnsAscendingOrderForAsc() {
        Order result = SortUtil.order(SortOrder.ASC, "name");

        assertNotNull(result);
        assertEquals(Order.asc("name").toString(), result.toString());
    }

    @Test
    public void orderReturnsDescendingOrderForDesc() {
        Order result = SortUtil.order(SortOrder.DESC, "name");

        assertNotNull(result);
        assertEquals(Order.desc("name").toString(), result.toString());
    }

    @Test
    public void orderReturnsAscendingOrderForNullOrder() {
        Order result = SortUtil.order(null, "name");

        assertNotNull(result);
        assertEquals(Order.asc("name").toString(), result.toString());
    }
}
