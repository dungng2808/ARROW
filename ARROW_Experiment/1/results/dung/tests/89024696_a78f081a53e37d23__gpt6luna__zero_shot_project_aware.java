package org.appenders.log4j2.elasticsearch.failover;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FailedItemInfo_ArrowTest {

    @Test
    public void returnsTargetName() {
        FailedItemInfo info = new FailedItemInfo("target");
        assertEquals("target", info.getTargetName());
    }

    @Test
    public void returnsNullTargetName() {
        FailedItemInfo info = new FailedItemInfo(null);
        assertNull(info.getTargetName());
    }

    @Test
    public void returnsEmptyTargetName() {
        FailedItemInfo info = new FailedItemInfo("");
        assertEquals("", info.getTargetName());
    }
}
