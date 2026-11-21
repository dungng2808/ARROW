package org.apache.phoenix.pig.util;

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

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SqlQueryToColumnInfoFunction_ArrowTest {

    private Configuration configuration;
    private SqlQueryToColumnInfoFunction function;
    private Connection connection;
    private Statement statement;
    private PhoenixStatement phoenixStatement;
    private QueryPlan queryPlan;
    private ColumnProjector columnProjector;
    private ColumnInfo columnInfo;

    @Before
    public void setUp() throws Exception {
        configuration = mock(Configuration.class);
        function = new SqlQueryToColumnInfoFunction(configuration);
        
        connection = mock(Connection.class);
        statement = mock(Statement.class);
        phoenixStatement = mock(PhoenixStatement.class);
        queryPlan = mock(QueryPlan.class);
        columnProjector = mock(ColumnProjector.class);
        columnInfo = mock(ColumnInfo.class);
    }

    @Test
    public void testApply_NormalCase() throws Exception {
        String sqlQuery = "SELECT * FROM table";
        
        // Mock the connection and related objects
        when(ConnectionUtil.getInputConnection(configuration)).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.unwrap(PhoenixStatement.class)).thenReturn(phoenixStatement);
        when(phoenixStatement.compileQuery(sqlQuery)).thenReturn(queryPlan);
        
        // Mock the projector and its behavior
        List<ColumnProjector> projectors = Lists.newArrayList(columnProjector);
        when(queryPlan.getProjector().getColumnProjectors()).thenReturn(projectors);
        
        // Mock column projector behavior
        when(columnProjector.getName()).thenReturn("column1");
        when(columnProjector.getExpression().getDataType().getSqlType()).thenReturn(1);
        
        // Execute the function
        List<ColumnInfo> result = function.apply(sqlQuery);
        
        // Verify results
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(connection).close();
    }

    @Test(expected = RuntimeException.class)
    public void testApply_NullSqlQuery() {
        function.apply(null);
    }

    @Test
    public void testApply_SqlException() throws Exception {
        String sqlQuery = "SELECT * FROM table";
        
        // Mock the connection to throw SQLException
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
        String sqlQuery = "SELECT * FROM table";
        
        // Mock the connection to throw SQLException on close
        when(ConnectionUtil.getInputConnection(configuration)).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.unwrap(PhoenixStatement.class)).thenReturn(phoenixStatement);
        when(phoenixStatement.compileQuery(sqlQuery)).thenReturn(queryPlan);
        
        // Mock the projector and its behavior
        List<ColumnProjector> projectors = Lists.newArrayList(columnProjector);
        when(queryPlan.getProjector().getColumnProjectors()).thenReturn(projectors);
        
        // Mock column projector behavior
        when(columnProjector.getName()).thenReturn("column1");
        when(columnProjector.getExpression().getDataType().getSqlType()).thenReturn(1);
        
        // Mock connection close to throw exception
        doThrow(new SQLException("Close error")).when(connection).close();
        
        try {
            function.apply(sqlQuery);
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            // Expected
        }
    }
}
