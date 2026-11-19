package com.civism.rocket.delay;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class CivismRocketApplication_ArrowTest {

    @Test
    public void canCreateApplication() {
        CivismRocketApplication application = new CivismRocketApplication();
        assertNotNull(application);
    }
}
