package org.apache.phoenix.pig.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.hadoop.conf.Configuration;
import org.junit.Test;

public class SqlQueryToColumnInfoFunction_ArrowTest {

    @Test(expected = NullPointerException.class)
    public void applyRejectsNullQuery() {
        SqlQueryToColumnInfoFunction function =
                new SqlQueryToColumnInfoFunction(new Configuration());
        function.apply(null);
    }

    @Test
    public void applyWithUnavailableConnectionThrowsRuntimeException() {
        SqlQueryToColumnInfoFunction function =
                new SqlQueryToColumnInfoFunction(new Configuration());
        try {
            function.apply("SELECT 1");
        } catch (RuntimeException expected) {
            assertNotNull(expected.getCause());
            return;
        }
        assertTrue(false);
    }
}
