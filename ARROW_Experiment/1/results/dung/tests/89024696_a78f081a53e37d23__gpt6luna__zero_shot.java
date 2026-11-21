package org.appenders.log4j2.elasticsearch.failover;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FailedItemInfo_ArrowTest {

    @Test
    public void getTargetNameReturnsConstructorValue() {
        FailedItemInfo info = new FailedItemInfo("target");

        assertEquals("target", info.getTargetName());
    }

    @Test
    public void getTargetNameReturnsNullWhenConstructedWithNull() {
        FailedItemInfo info = new FailedItemInfo(null);

        assertNull(info.getTargetName());
    }

    @Test
    public void getTargetNameReturnsEmptyStringWhenConstructedWithEmptyString() {
        FailedItemInfo info = new FailedItemInfo("");

        assertEquals("", info.getTargetName());
    }
}
