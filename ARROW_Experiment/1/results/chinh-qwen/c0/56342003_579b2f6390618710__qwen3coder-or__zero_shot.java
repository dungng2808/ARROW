package net.krotscheck.kangaroo.authz.common.database.util;

import net.krotscheck.kangaroo.common.response.SortOrder;
import org.hibernate.criterion.Order;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SortUtil_ArrowTest {

    @Test
    public void testOrderAsc() {
        Order result = SortUtil.order(SortOrder.ASC, "name");
        assertNotNull(result);
        assertEquals("name", result.toString());
    }

    @Test
    public void testOrderDesc() {
        Order result = SortUtil.order(SortOrder.DESC, "name");
        assertNotNull(result);
        assertEquals("name desc", result.toString());
    }

    @Test
    public void testOrderNull() {
        Order result = SortUtil.order(null, "name");
        assertNotNull(result);
        assertEquals("name asc", result.toString());
    }

    @Test
    public void testOrderWithEmptyString() {
        Order result = SortUtil.order(SortOrder.DESC, "");
        assertNotNull(result);
        assertEquals(" desc", result.toString());
    }
}
