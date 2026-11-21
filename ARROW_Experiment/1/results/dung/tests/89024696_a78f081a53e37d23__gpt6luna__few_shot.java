package org.appenders.log4j2.elasticsearch.failover;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FailedItemInfo_ArrowTest {

    @Test
    public void returnsTargetName() {
        FailedItemInfo info = new FailedItemInfo("index-name");

        assertEquals("index-name", info.getTargetName());
    }

    @Test
    public void returnsNullTargetName() {
        FailedItemInfo info = new FailedItemInfo(null);

        assertNull(info.getTargetName());
    }
}
