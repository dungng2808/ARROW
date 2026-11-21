package com.civism.rocket.delay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CivismRocketApplication_ArrowTest {

    @Test
    public void constructsApplication() {
        CivismRocketApplication application = new CivismRocketApplication();

        assertNotNull(application);
    }
}
