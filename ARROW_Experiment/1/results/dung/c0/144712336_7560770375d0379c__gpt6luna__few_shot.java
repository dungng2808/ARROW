package com.alibaba.boot.nacos.actuate.endpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import org.junit.Test;

public class NacosConfigEndpoint_ArrowTest {

    @Test
    public void invokeWithoutApplicationContextReturnsEmptyResult() {
        NacosConfigEndpoint endpoint = new NacosConfigEndpoint();

        Map<String, Object> result = endpoint.invoke();

        assertTrue(result.isEmpty());
    }
}
