package com.whitepaek.demosigninwithapple;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class Application_ArrowTest {

    @Test
    public void applicationCanBeInstantiated() {
        Application application = new Application();
        assertNotNull(application);
    }
}
