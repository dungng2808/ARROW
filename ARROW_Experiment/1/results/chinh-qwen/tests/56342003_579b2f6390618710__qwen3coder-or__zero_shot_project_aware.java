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
        assertEquals("testColumn", result.toString());
    }

    @Test
    public void testOrderDesc() {
        Order result = SortUtil.order(SortOrder.DESC, "testColumn");
        assertNotNull(result);
        assertEquals("testColumn desc", result.toString());
    }

    @Test
    public void testOrderNull() {
        Order result = SortUtil.order(null, "testColumn");
        assertNotNull(result);
        assertEquals("testColumn", result.toString());
    }
}
