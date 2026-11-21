package org.zapodot.junit.jms.impl;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.jms.ConnectionFactory;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class EmbeddedJmsRuleImpl_ArrowTest {

    @Test
    public void connectionFactoryCannotBeCreatedBeforeBrokerStarts() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("broker", false, false);
        try {
            rule.connectionFactory();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals("Can not create ConnectionFactory before the broker has started", expected.getMessage());
        }
    }

    @Test
    public void activeMqConnectionFactoryCannotBeCreatedBeforeBrokerStarts() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("broker", false, false);
        try {
            rule.activeMqConnectionFactory();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals("Can not create ConnectionFactory before the broker has started", expected.getMessage());
        }
    }

    @Test
    public void brokerUriCannotBeReadBeforeBrokerStarts() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("broker", false, false);
        try {
            rule.brokerUri();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals("Can not create broker URI before the broker has started", expected.getMessage());
        }
    }

    @Test
    public void applyStartsBrokerForBaseStatementAndStopsItAfterward() throws Throwable {
        final EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("arrow-test-broker", false, false);
        final URI[] observedUri = new URI[1];
        final ConnectionFactory[] observedFactory = new ConnectionFactory[1];

        Statement base = new Statement() {
            @Override
            public void evaluate() {
                observedUri[0] = rule.brokerUri();
                observedFactory[0] = rule.connectionFactory();
            }
        };

        Statement wrapped = rule.apply(base, Description.createTestDescription("ExampleTest", "test"));
        assertNotNull(wrapped);
        wrapped.evaluate();

        assertEquals(URI.create("vm://arrow-test-broker?marshal=false"), observedUri[0]);
        assertTrue(observedFactory[0] instanceof ActiveMQConnectionFactory);

        try {
            rule.brokerUri();
            fail("Expected broker to be stopped");
        } catch (IllegalStateException expected) {
            assertEquals("Can not create broker URI before the broker has started", expected.getMessage());
        }
    }

    @Test
    public void applyStopsBrokerWhenBaseStatementThrows() throws Throwable {
        final EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("arrow-failure-broker", true, false);
        final RuntimeException failure = new RuntimeException("base failure");

        Statement base = new Statement() {
            @Override
            public void evaluate() {
                assertEquals(URI.create("vm://arrow-failure-broker?marshal=true"), rule.brokerUri());
                throw failure;
            }
        };

        Statement wrapped = rule.apply(base, Description.createTestDescription("ExampleTest", "test"));
        try {
            wrapped.evaluate();
            fail("Expected base statement failure");
        } catch (RuntimeException expected) {
            assertSame(failure, expected);
        }

        try {
            rule.activeMqConnectionFactory();
            fail("Expected broker to be stopped");
        } catch (IllegalStateException expected) {
            assertEquals("Can not create ConnectionFactory before the broker has started", expected.getMessage());
        }
    }

    @Test
    public void applyUsesDescriptionClassNameWhenPredefinedNameIsNull() throws Throwable {
        final EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, false, false);
        final URI[] observedUri = new URI[1];

        Statement base = new Statement() {
            @Override
            public void evaluate() {
                observedUri[0] = rule.brokerUri();
            }
        };

        rule.apply(base, Description.createTestDescription("DescriptionClass", "test")).evaluate();

        assertEquals(URI.create("vm://DescriptionClass?marshal=false"), observedUri[0]);
    }
}
