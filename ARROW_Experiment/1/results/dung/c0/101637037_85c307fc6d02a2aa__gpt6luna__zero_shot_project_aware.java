package org.zapodot.junit.jms.impl;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class EmbeddedJmsRuleImpl_ArrowTest {

    @Test
    public void connectionFactoryCannotBeCreatedBeforeBrokerStarts() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("test-broker", false, false);
        try {
            rule.connectionFactory();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals("Can not create ConnectionFactory before the broker has started", expected.getMessage());
        }
    }

    @Test
    public void activeMqConnectionFactoryCannotBeCreatedBeforeBrokerStarts() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("test-broker", false, false);
        try {
            rule.activeMqConnectionFactory();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals("Can not create ConnectionFactory before the broker has started", expected.getMessage());
        }
    }

    @Test
    public void brokerUriCannotBeReadBeforeBrokerStarts() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("test-broker", false, false);
        try {
            rule.brokerUri();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals("Can not create broker URI before the broker has started", expected.getMessage());
        }
    }

    @Test
    public void applyStartsBrokerForBaseStatementAndStopsItAfterward() throws Throwable {
        final EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("test-broker", false, false);
        final URI[] uriDuringEvaluation = new URI[1];
        final ActiveMQConnectionFactory[] factoryDuringEvaluation = new ActiveMQConnectionFactory[1];
        Statement base = new Statement() {
            @Override
            public void evaluate() {
                uriDuringEvaluation[0] = rule.brokerUri();
                factoryDuringEvaluation[0] = rule.activeMqConnectionFactory();
            }
        };

        Statement wrapped = rule.apply(base, Description.createTestDescription(getClass(), "test"));
        wrapped.evaluate();

        assertNotNull(uriDuringEvaluation[0]);
        assertEquals("vm://test-broker?marshal=false", uriDuringEvaluation[0].toString());
        assertNotNull(factoryDuringEvaluation[0]);
        try {
            rule.brokerUri();
            fail("Expected broker to be stopped");
        } catch (IllegalStateException expected) {
            assertEquals("Can not create broker URI before the broker has started", expected.getMessage());
        }
    }

    @Test
    public void applyUsesPredefinedBrokerName() throws Throwable {
        final EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("chosen-name", true, false);
        final URI[] uriDuringEvaluation = new URI[1];
        Statement base = new Statement() {
            @Override
            public void evaluate() {
                uriDuringEvaluation[0] = rule.brokerUri();
            }
        };

        rule.apply(base, Description.createTestDescription(getClass(), "test")).evaluate();

        assertNotNull(uriDuringEvaluation[0]);
        assertEquals("vm://chosen-name?marshal=true", uriDuringEvaluation[0].toString());
    }

    @Test
    public void applyStopsBrokerWhenBaseStatementThrows() throws Throwable {
        final EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("test-broker", false, false);
        final RuntimeException failure = new RuntimeException("base failure");
        Statement base = new Statement() {
            @Override
            public void evaluate() {
                throw failure;
            }
        };

        try {
            rule.apply(base, Description.createTestDescription(getClass(), "test")).evaluate();
            fail("Expected base failure");
        } catch (RuntimeException expected) {
            assertSame(failure, expected);
        }

        try {
            rule.brokerUri();
            fail("Expected broker to be stopped");
        } catch (IllegalStateException expected) {
            assertEquals("Can not create broker URI before the broker has started", expected.getMessage());
        }
    }
}
