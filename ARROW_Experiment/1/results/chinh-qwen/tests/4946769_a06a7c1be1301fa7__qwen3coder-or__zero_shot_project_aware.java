package com.eviware.soapui.model.propertyexpansion.resolvers.providers;

import com.eviware.soapui.impl.wsdl.support.AbstractTestCaseRunner;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CurrentRunStepIndexProvider_ArrowTest {

    private CurrentRunStepIndexProvider provider;
    private PropertyExpansionContext context;
    private TestRunner testRunner;
    private AbstractTestCaseRunner abstractTestCaseRunner;

    @Before
    public void setUp() {
        provider = new CurrentRunStepIndexProvider();
        context = mock(TestRunContext.class);
        testRunner = mock(TestRunner.class);
        abstractTestCaseRunner = mock(AbstractTestCaseRunner.class);
    }

    @Test
    public void testGetValue_ReturnsNull_WhenContextIsNotTestRunContext() {
        PropertyExpansionContext nonTestRunContext = mock(PropertyExpansionContext.class);
        assertNull(provider.getValue(nonTestRunContext));
    }

    @Test
    public void testGetValue_ReturnsNull_WhenTestRunnerIsNull() {
        when(context.getTestRunner()).thenReturn(null);
        assertNull(provider.getValue(context));
    }

    @Test
    public void testGetValue_ReturnsNull_WhenTestRunnerIsNotAbstractTestCaseRunner() {
        TestRunner regularTestRunner = mock(TestRunner.class);
        when(context.getTestRunner()).thenReturn(regularTestRunner);
        assertNull(provider.getValue(context));
    }

    @Test
    public void testGetValue_ReturnsResultCountAsString_WhenContextIsTestRunContextAndRunnerIsAbstractTestCaseRunner() {
        int expectedResultCount = 5;
        when(context.getTestRunner()).thenReturn(abstractTestCaseRunner);
        when(abstractTestCaseRunner.getResultCount()).thenReturn(expectedResultCount);
        
        String result = provider.getValue(context);
        assertEquals(String.valueOf(expectedResultCount), result);
    }

    @Test
    public void testGetValue_ReturnsNull_WhenTestRunnerDoesNotImplementAbstractTestCaseRunner() {
        TestRunner regularTestRunner = mock(TestRunner.class);
        when(context.getTestRunner()).thenReturn(regularTestRunner);
        assertNull(provider.getValue(context));
    }
}
