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
        assertEquals("name", result.getPropertyName());
        assertEquals(false, result.isAscending());
    }

    @Test
    public void orderReturnsDescendingOrderForDesc() {
        Order result = SortUtil.order(SortOrder.DESC, "name");

        assertNotNull(result);
        assertEquals("name", result.getPropertyName());
        assertEquals(true, result.isAscending());
    }

    @Test
    public void orderDefaultsToAscendingForNullOrder() {
        Order result = SortUtil.order(null, "name");

        assertNotNull(result);
        assertEquals("name", result.getPropertyName());
        assertEquals(false, result.isAscending());
    }
}
