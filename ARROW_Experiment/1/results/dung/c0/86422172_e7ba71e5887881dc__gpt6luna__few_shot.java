package org.apache.phoenix.pig.util;

import org.apache.hadoop.conf.Configuration;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SqlQueryToColumnInfoFunction_ArrowTest {

    @Test
    public void applyRejectsNullQuery() {
        SqlQueryToColumnInfoFunction function =
                new SqlQueryToColumnInfoFunction(new Configuration());

        try {
            function.apply(null);
            throw new AssertionError("Expected a null query to be rejected");
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void applyRejectsNullConfigurationWhenOpeningConnection() {
        SqlQueryToColumnInfoFunction function = new SqlQueryToColumnInfoFunction(null);

        try {
            function.apply("SELECT 1");
            throw new AssertionError("Expected connection setup to fail");
        } catch (RuntimeException expected) {
            assertTrue(expected.getCause() != null || expected.getMessage() != null);
        }
    }
}
