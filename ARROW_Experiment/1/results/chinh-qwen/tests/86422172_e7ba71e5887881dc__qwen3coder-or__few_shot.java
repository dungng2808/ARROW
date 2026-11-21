package org.apache.phoenix.pig.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.phoenix.compile.ColumnProjector;
import org.apache.phoenix.compile.QueryPlan;
import org.apache.phoenix.jdbc.PhoenixStatement;
import org.apache.phoenix.mapreduce.util.ConnectionUtil;
import org.apache.phoenix.util.ColumnInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class SqlQueryToColumnInfoFunction_ArrowTest {

    @Mock
    private Configuration configuration;

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private PhoenixStatement phoenixStatement;

    @Mock
    private QueryPlan queryPlan;

    @Mock
    private ColumnProjector columnProjector;

    @Mock
    private org.apache.phoenix.expression.Expression expression;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testApply_NullSqlQuery() {
        SqlQueryToColumnInfoFunction function = new SqlQueryToColumnInfoFunction(configuration);
        try {
            function.apply(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testApply_SuccessfulExecution() throws Exception {
        String sqlQuery = "SELECT * FROM TEST_TABLE";
        
        when(ConnectionUtil.getInputConnection(any(Configuration.class))).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.unwrap(PhoenixStatement.class)).thenReturn(phoenixStatement);
        when(phoenixStatement.compileQuery(sqlQuery)).thenReturn(queryPlan);
        
        List<ColumnProjector> columnProjectors = Lists.newArrayList(columnProjector);
        when(queryPlan.getProjector().getColumnProjectors()).thenReturn(columnProjectors);
        
        when(columnProjector.getName()).thenReturn("COLUMN_NAME");
        when(columnProjector.getExpression()).thenReturn(expression);
        when(expression.getDataType().getSqlType()).thenReturn(1);
        
        SqlQueryToColumnInfoFunction function = new SqlQueryToColumnInfoFunction(configuration);
        List<ColumnInfo> result = function.apply(sqlQuery);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("COLUMN_NAME", result.get(0).getName());
        assertEquals(1, result.get(0).getSqlType());
        
        verify(connection).close();
    }

    @Test
    public void testApply_SQLExceptionThrown() throws Exception {
        String sqlQuery = "SELECT * FROM TEST_TABLE";
        
        when(ConnectionUtil.getInputConnection(any(Configuration.class))).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.unwrap(PhoenixStatement.class)).thenReturn(phoenixStatement);
        when(phoenixStatement.compileQuery(sqlQuery)).thenThrow(new SQLException("Test SQL Exception"));
        
        SqlQueryToColumnInfoFunction function = new SqlQueryToColumnInfoFunction(configuration);
        
        try {
            function.apply(sqlQuery);
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof SQLException);
        }
    }

    @Test
    public void testApply_ConnectionCloseException() throws Exception {
        String sqlQuery = "SELECT * FROM TEST_TABLE";
        
        when(ConnectionUtil.getInputConnection(any(Configuration.class))).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.unwrap(PhoenixStatement.class)).thenReturn(phoenixStatement);
        when(phoenixStatement.compileQuery(sqlQuery)).thenReturn(queryPlan);
        
        List<ColumnProjector> columnProjectors = Lists.newArrayList(columnProjector);
        when(queryPlan.getProjector().getColumnProjectors()).thenReturn(columnProjectors);
        
        when(columnProjector.getName()).thenReturn("COLUMN_NAME");
        when(columnProjector.getExpression()).thenReturn(expression);
        when(expression.getDataType().getSqlType()).thenReturn(1);
        
        doThrow(new SQLException("Close error")).when(connection).close();
        
        SqlQueryToColumnInfoFunction function = new SqlQueryToColumnInfoFunction(configuration);
        
        try {
            function.apply(sqlQuery);
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof SQLException);
        }
    }
}
