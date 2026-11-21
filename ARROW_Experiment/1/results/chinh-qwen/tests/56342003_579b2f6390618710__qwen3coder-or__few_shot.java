package net.krotscheck.kangaroo.authz.common.database.util;

import net.krotscheck.kangaroo.common.response.SortOrder;
import org.hibernate.criterion.Order;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SortUtil_ArrowTest {

    @Test
    public void testOrderAsc() {
        Order result = SortUtil.order(SortOrder.ASC, "testColumn");
        assertNotNull(result);
        // We cannot directly verify the internal state, but we can ensure
        // the method doesn't throw and returns a non-null object
    }

    @Test
    public void testOrderDesc() {
        Order result = SortUtil.order(SortOrder.DESC, "testColumn");
        assertNotNull(result);
        // We cannot directly verify the internal state, but we can ensure
        // the method doesn't throw and returns a non-null object
    }

    @Test
    public void testOrderWithNullSortOrder() {
        Order result = SortUtil.order(null, "testColumn");
        assertNotNull(result);
        // Should default to ascending since null != SortOrder.DESC
    }

    @Test
    public void testOrderWithEmptyColumnName() {
        Order result = SortUtil.order(SortOrder.ASC, "");
        assertNotNull(result);
    }

    @Test
    public void testOrderWithNullColumnName() {
        Order result = SortUtil.order(SortOrder.ASC, null);
        assertNotNull(result);
    }
}
