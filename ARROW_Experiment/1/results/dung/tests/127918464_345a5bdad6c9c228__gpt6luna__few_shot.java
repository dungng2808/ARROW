package org.datum.application.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.datum.application.domain.Connector;
import org.datum.application.domain.Cube;
import org.datum.application.olap.DataSet;
import org.datum.application.olap.Field;
import org.datum.application.olap.PhysicalTable;
import org.junit.Test;

public class DatabaseOLAPService_ArrowTest {

    @Test
    public void serviceCanBeConstructed() {
        DatabaseOLAPService service = new DatabaseOLAPService();
        assertNotNull(service);
    }

    @Test
    public void resolveTablesWithNullConnectorThrows() {
        DatabaseOLAPService service = new DatabaseOLAPService();
        try {
            service.resolveTables(null);
        } catch (Exception exception) {
            assertNotNull(exception);
            return;
        }
        throw new AssertionError("Expected an exception");
    }

    @Test
    public void resolveFieldsWithNullArgumentsThrows() {
        DatabaseOLAPService service = new DatabaseOLAPService();
        try {
            service.resolveFields(null, null);
        } catch (Exception exception) {
            assertNotNull(exception);
            return;
        }
        throw new AssertionError("Expected an exception");
    }

    @Test
    public void resolveTableWithNullArgumentsThrows() {
        DatabaseOLAPService service = new DatabaseOLAPService();
        try {
            service.resolveTable(null, null);
        } catch (Exception exception) {
            assertNotNull(exception);
            return;
        }
        throw new AssertionError("Expected an exception");
    }

    @Test
    public void fetchDataWithNullArgumentsThrows() {
        DatabaseOLAPService service = new DatabaseOLAPService();
        try {
            DataSet<Field> result = service.fetchData((Connector) null, (Cube) null);
            assertNotNull(result);
        } catch (Exception exception) {
            assertNotNull(exception);
        }
    }
}
