package com.monitor4all.cn.springcloud_gateway;

import org.junit.Test;
import static org.junit.Assert.*;

public class SpringcloudGatewayApplication_ArrowTest {

    @Test
    public void testMain() {
        // Testing main method execution - it should not throw any exception
        try {
            SpringcloudGatewayApplication.main(new String[]{});
        } catch (Exception e) {
            fail("main method should not throw exception");
        }
    }

    @Test
    public void testBodyPredicate() {
        // Testing the bodyPredicate bean creation
        SpringcloudGatewayApplication application = new SpringcloudGatewayApplication();
        assertNotNull("bodyPredicate should not be null", application.bodyPredicate());
        
        // Testing the predicate functionality
        java.util.function.Predicate<Object> predicate = application.bodyPredicate();
        assertTrue("predicate should return true for any input", predicate.test(new Object()));
        assertTrue("predicate should return true for null input", predicate.test(null));
    }
}
