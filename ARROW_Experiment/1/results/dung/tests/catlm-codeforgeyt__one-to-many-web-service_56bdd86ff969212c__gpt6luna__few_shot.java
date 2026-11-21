package com.codeforgeyt.onetomanywebservice;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class OneToManyWebServiceApplication_ArrowTest {

    @Test
    public void applicationCanBeInstantiated() {
        OneToManyWebServiceApplication application = new OneToManyWebServiceApplication();
        assertNotNull(application);
    }
}
