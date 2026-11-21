package com.eviware.soapui.model.propertyexpansion.resolvers.providers;

import com.eviware.soapui.impl.wsdl.support.AbstractTestCaseRunner;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CurrentRunStepIndexProvider_ArrowTest {

    @Test
    public void testGetValue_WithValidTestRunContextAndAbstractTestCaseRunner_ReturnsResultCount() {
        // Given
        CurrentRunStepIndexProvider provider = new CurrentRunStepIndexProvider();
        AbstractTestCaseRunner mockRunner = mock(AbstractTestCaseRunner.class);
        when(mockRunner.getResultCount()).thenReturn(5);

        TestRunner mockTestRunner = mock(TestRunner.class);
        when(mockTestRunner instanceof AbstractTestCaseRunner).thenReturn(true);
        when((AbstractTestCaseRunner) mockTestRunner).thenReturn(mockRunner);

        TestRunContext mockContext = mock(TestRunContext.class);
        when(mockContext.getTestRunner()).thenReturn(mockTestRunner);

        // When
        String result = provider.getValue(mockContext);

        // Then
        assertEquals("5", result);
    }

    @Test
    public void testGetValue_WithNonTestRunContext_ReturnsNull() {
        // Given
        CurrentRunStepIndexProvider provider = new CurrentRunStepIndexProvider();
        PropertyExpansionContext mockContext = mock(PropertyExpansionContext.class);

        // When
        String result = provider.getValue(mockContext);

        // Then
        assertNull(result);
    }

    @Test
    public void testGetValue_WithTestRunContextButNotAbstractTestCaseRunner_ReturnsNull() {
        // Given
        CurrentRunStepIndexProvider provider = new CurrentRunStepIndexProvider();
        TestRunner mockRunner = mock(TestRunner.class);
        TestRunContext mockContext = mock(TestRunContext.class);
        when(mockContext.getTestRunner()).thenReturn(mockRunner);

        // When
        String result = provider.getValue(mockContext);

        // Then
        assertNull(result);
    }
}
