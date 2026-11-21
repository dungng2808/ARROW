package com.eviware.soapui.model.propertyexpansion.resolvers.providers;

import com.eviware.soapui.impl.wsdl.support.AbstractTestCaseRunner;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CurrentRunStepIndexProvider_ArrowTest {

    @Test
    public void testGetValue_WithNullContext() {
        CurrentRunStepIndexProvider provider = new CurrentRunStepIndexProvider();
        String result = provider.getValue(null);
        assertNull(result);
    }

    @Test
    public void testGetValue_WithNonTestRunContext() {
        CurrentRunStepIndexProvider provider = new CurrentRunStepIndexProvider();
        PropertyExpansionContext context = mock(PropertyExpansionContext.class);
        String result = provider.getValue(context);
        assertNull(result);
    }

    @Test
    public void testGetValue_WithTestRunContextButNotAbstractTestCaseRunner() {
        CurrentRunStepIndexProvider provider = new CurrentRunStepIndexProvider();
        TestRunner testRunner = mock(TestRunner.class);
        TestRunContext context = mock(TestRunContext.class);
        when(context.getTestRunner()).thenReturn(testRunner);
        
        String result = provider.getValue(context);
        assertNull(result);
    }

    @Test
    public void testGetValue_WithValidContextAndRunner() {
        CurrentRunStepIndexProvider provider = new CurrentRunStepIndexProvider();
        AbstractTestCaseRunner abstractTestCaseRunner = mock(AbstractTestCaseRunner.class);
        TestRunContext context = mock(TestRunContext.class);
        when(context.getTestRunner()).thenReturn(abstractTestCaseRunner);
        when(abstractTestCaseRunner.getResultCount()).thenReturn(5);
        
        String result = provider.getValue(context);
        assertEquals("5", result);
    }
}
