package com.monitor4all.cn.springcloud_gateway;

import org.junit.Test;
import static org.junit.Assert.*;

public class SpringcloudGatewayApplication_ArrowTest {

    @Test
    public void testMain() {
        // Testing main method execution - should not throw any exception
        try {
            SpringcloudGatewayApplication.main(new String[]{});
        } catch (Exception e) {
            fail("main method should not throw exception");
        }
    }

    @Test
    public void testBodyPredicate() {
        SpringcloudGatewayApplication application = new SpringcloudGatewayApplication();
        assertNotNull("bodyPredicate should not be null", application.bodyPredicate());
        assertTrue("bodyPredicate should return true for any input", 
                  application.bodyPredicate().test("any_input"));
    }
}
