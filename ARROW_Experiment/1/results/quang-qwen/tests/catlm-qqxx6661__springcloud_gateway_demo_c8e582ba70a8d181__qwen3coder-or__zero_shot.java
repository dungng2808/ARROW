package com.monitor4all.cn.springcloud_gateway;

import org.junit.Test;
import static org.junit.Assert.*;

public class SpringcloudGatewayApplication_ArrowTest {

    @Test
    public void testMain() {
        // Testing main method execution - no assertions needed as it just runs the application
        // This test ensures the main method can be called without exception
        try {
            SpringcloudGatewayApplication.main(new String[]{});
        } catch (Exception e) {
            fail("Main method should not throw exception when called with empty args");
        }
    }

    @Test
    public void testBodyPredicate() {
        SpringcloudGatewayApplication application = new SpringcloudGatewayApplication();
        assertNotNull("bodyPredicate should not return null", application.bodyPredicate());
        
        // Test that the predicate returns true for any input (as defined in the source)
        assertTrue("bodyPredicate should return true for any input", 
                  application.bodyPredicate().test("any_input"));
    }
}
