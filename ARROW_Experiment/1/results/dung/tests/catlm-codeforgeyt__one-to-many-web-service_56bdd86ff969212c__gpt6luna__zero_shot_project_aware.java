package com.codeforgeyt.onetomanywebservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OneToManyWebServiceApplication_ArrowTest {

    @Test
    public void applicationCanBeInstantiated() {
        assertNotNull(new OneToManyWebServiceApplication());
    }

    @Test
    public void mainStartsApplication() {
        OneToManyWebServiceApplication.main(new String[0]);
        assertNotNull(SpringApplication.class);
    }
}
