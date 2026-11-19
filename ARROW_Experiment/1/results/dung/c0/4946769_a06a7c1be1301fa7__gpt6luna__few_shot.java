package com.eviware.soapui.model.propertyexpansion.resolvers.providers;

import com.eviware.soapui.impl.wsdl.support.AbstractTestCaseRunner;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CurrentRunStepIndexProvider_ArrowTest {

    @Test
    public void returnsNullForNonTestRunContext() {
        CurrentRunStepIndexProvider provider = new CurrentRunStepIndexProvider();
        PropertyExpansionContext context = new PropertyExpansionContext() {
        };

        assertNull(provider.getValue(context));
    }

    @Test
    public void returnsNullWhenTestRunnerIsNotAbstractTestCaseRunner() {
        CurrentRunStepIndexProvider provider = new CurrentRunStepIndexProvider();
        TestRunContext context = new TestRunContext() {
            @Override
            public TestRunner getTestRunner() {
                return null;
            }
        };

        assertNull(provider.getValue(context));
    }
}
