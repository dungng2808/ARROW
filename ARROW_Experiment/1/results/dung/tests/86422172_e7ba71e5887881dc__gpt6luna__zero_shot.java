package org.apache.phoenix.pig.util;

import org.apache.hadoop.conf.Configuration;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SqlQueryToColumnInfoFunction_ArrowTest {

    @Test
    public void constructorCreatesFunction() {
        SqlQueryToColumnInfoFunction function =
                new SqlQueryToColumnInfoFunction(new Configuration());
        assertNotNull(function);
    }

    @Test(expected = NullPointerException.class)
    public void applyRejectsNullQuery() {
        SqlQueryToColumnInfoFunction function =
                new SqlQueryToColumnInfoFunction(new Configuration());
        function.apply(null);
    }
}
