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
    public void returnsResultCountForAbstractTestCaseRunner() {
        AbstractTestCaseRunner runner = mock(AbstractTestCaseRunner.class);
        when(runner.getResultCount()).thenReturn(7);
        TestRunContext context = mock(TestRunContext.class);
        when(context.getTestRunner()).thenReturn(runner);

        assertEquals("7", new CurrentRunStepIndexProvider().getValue(context));
    }

    @Test
    public void returnsNullWhenTestRunnerIsNotAbstractTestCaseRunner() {
        TestRunContext context = mock(TestRunContext.class);
        when(context.getTestRunner()).thenReturn(mock(TestRunner.class));

        assertNull(new CurrentRunStepIndexProvider().getValue(context));
    }

    @Test
    public void returnsNullForNonTestRunContext() {
        PropertyExpansionContext context = mock(PropertyExpansionContext.class);

        assertNull(new CurrentRunStepIndexProvider().getValue(context));
    }
}
