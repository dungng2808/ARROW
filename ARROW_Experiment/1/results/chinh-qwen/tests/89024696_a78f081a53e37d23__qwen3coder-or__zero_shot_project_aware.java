package org.appenders.log4j2.elasticsearch.failover;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FailedItemInfo_ArrowTest {

    @Test
    public void constructorSetsTargetName() {
        // Given
        String targetName = "testTarget";
        
        // When
        FailedItemInfo failedItemInfo = new FailedItemInfo(targetName);
        
        // Then
        assertEquals(targetName, failedItemInfo.getTargetName());
    }
    
    @Test
    public void getTargetNameReturnsCorrectValue() {
        // Given
        String targetName = "anotherTarget";
        FailedItemInfo failedItemInfo = new FailedItemInfo(targetName);
        
        // When
        String result = failedItemInfo.getTargetName();
        
        // Then
        assertEquals(targetName, result);
    }
}
