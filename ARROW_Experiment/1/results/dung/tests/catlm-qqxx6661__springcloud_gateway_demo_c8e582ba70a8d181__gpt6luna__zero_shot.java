package com.monitor4all.cn.springcloud_gateway;

import org.junit.Test;

import java.util.function.Predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SpringcloudGatewayApplication_ArrowTest {

    @Test
    public void bodyPredicateReturnsTrueForNonNullInput() {
        SpringcloudGatewayApplication application = new SpringcloudGatewayApplication();
        Predicate predicate = application.bodyPredicate();

        assertNotNull(predicate);
        assertTrue(predicate.test("body"));
    }

    @Test
    public void bodyPredicateReturnsTrueForNullInput() {
        SpringcloudGatewayApplication application = new SpringcloudGatewayApplication();
        Predicate predicate = application.bodyPredicate();

        assertTrue(predicate.test(null));
    }

    @Test
    public void bodyPredicateReturnsTrueForFalseInput() {
        SpringcloudGatewayApplication application = new SpringcloudGatewayApplication();
        Predicate predicate = application.bodyPredicate();

        assertTrue(predicate.test(Boolean.FALSE));
        assertFalse(Boolean.TRUE.equals(Boolean.FALSE));
    }
}
