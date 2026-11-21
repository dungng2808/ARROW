package org.datum.application.service.impl;

import org.datum.application.service.impl.DatabaseOLAPService;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class DatabaseOLAPService_ArrowTest {

    @Test
    public void constructorCreatesService() {
        DatabaseOLAPService service = new DatabaseOLAPService();

        assertNotNull(service);
    }
}
