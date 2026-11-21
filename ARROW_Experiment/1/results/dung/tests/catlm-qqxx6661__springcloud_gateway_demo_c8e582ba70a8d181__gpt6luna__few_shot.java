package com.monitor4all.cn.springcloud_gateway;

import static org.junit.Assert.assertTrue;

import java.util.function.Predicate;
import org.junit.Test;

public class SpringcloudGatewayApplication_ArrowTest {

    @Test
    public void bodyPredicateReturnsTrueForAnyInput() {
        SpringcloudGatewayApplication application = new SpringcloudGatewayApplication();
        Predicate predicate = application.bodyPredicate();

        assertTrue(predicate.test(new Object()));
    }

    @Test
    public void bodyPredicateReturnsTrueForNullInput() {
        SpringcloudGatewayApplication application = new SpringcloudGatewayApplication();
        Predicate predicate = application.bodyPredicate();

        assertTrue(predicate.test(null));
    }
}
