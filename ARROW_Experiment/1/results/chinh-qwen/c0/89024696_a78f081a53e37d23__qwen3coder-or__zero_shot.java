package org.appenders.log4j2.elasticsearch.failover;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FailedItemInfo_ArrowTest {

    @Test
    public void constructorSetsTargetName() {
        // given
        String expectedTargetName = "testTarget";

        // when
        FailedItemInfo failedItemInfo = new FailedItemInfo(expectedTargetName);

        // then
        assertNotNull(failedItemInfo);
        assertEquals(expectedTargetName, failedItemInfo.getTargetName());
    }

    @Test
    public void getTargetNameReturnsCorrectValue() {
        // given
        String targetName = "anotherTarget";
        FailedItemInfo failedItemInfo = new FailedItemInfo(targetName);

        // when
        String actualTargetName = failedItemInfo.getTargetName();

        // then
        assertEquals(targetName, actualTargetName);
    }

}
