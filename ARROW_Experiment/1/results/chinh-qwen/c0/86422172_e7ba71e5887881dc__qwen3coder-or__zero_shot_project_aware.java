package org.apache.phoenix.pig.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.logging.Log;
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

import com.google.common.base.Function;
import com.google.common.collect.Lists;

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

    @Mock
    private org.apache.phoenix.schema.types.PhoenixType dataType;

    private SqlQueryToColumnInfoFunction function;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        function = new SqlQueryToColumnInfoFunction(configuration);
    }

    @Test
    public void testApply_NormalCase() throws Exception {
        String sqlQuery = "SELECT * FROM TEST_TABLE";
        
        // Setup mocks
        when(ConnectionUtil.getInputConnection(configuration)).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.unwrap(PhoenixStatement.class)).thenReturn(phoenixStatement);
        when(phoenixStatement.compileQuery(sqlQuery)).thenReturn(queryPlan);
        
        // Mock projector and expression
        List<ColumnProjector> projectors = Lists.newArrayList(columnProjector);
        when(queryPlan.getProjector().getColumnProjectors()).thenReturn(projectors);
        when(columnProjector.getName()).thenReturn("COLUMN_NAME");
        when(columnProjector.getExpression()).thenReturn(expression);
        when(expression.getDataType()).thenReturn(dataType);
        when(dataType.getSqlType()).thenReturn(1);
        
        // Execute
        List<ColumnInfo> result = function.apply(sqlQuery);
        
        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("COLUMN_NAME", result.get(0).getName());
        assertEquals(1, result.get(0).getSqlType());
    }

    @Test(expected = NullPointerException.class)
    public void testApply_NullQuery() {
        function.apply(null);
    }

    @Test
    public void testApply_WithSQLException() throws Exception {
        String sqlQuery = "SELECT * FROM TEST_TABLE";
        
        // Setup mocks to throw SQLException
        when(ConnectionUtil.getInputConnection(configuration)).thenThrow(new SQLException("Connection error"));
        
        try {
            function.apply(sqlQuery);
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            // Expected
        }
    }

    @Test
    public void testApply_ConnectionCloseException() throws Exception {
        String sqlQuery = "SELECT * FROM TEST_TABLE";
        
        // Setup mocks
        when(ConnectionUtil.getInputConnection(configuration)).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.unwrap(PhoenixStatement.class)).thenReturn(phoenixStatement);
        when(phoenixStatement.compileQuery(sqlQuery)).thenReturn(queryPlan);
        
        // Mock projector and expression
        List<ColumnProjector> projectors = Lists.newArrayList(columnProjector);
        when(queryPlan.getProjector().getColumnProjectors()).thenReturn(projectors);
        when(columnProjector.getName()).thenReturn("COLUMN_NAME");
        when(columnProjector.getExpression()).thenReturn(expression);
        when(expression.getDataType()).thenReturn(dataType);
        when(dataType.getSqlType()).thenReturn(1);
        
        // Make connection close throw exception
        doThrow(new SQLException("Close error")).when(connection).close();
        
        try {
            function.apply(sqlQuery);
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            // Expected
        }
    }
}
