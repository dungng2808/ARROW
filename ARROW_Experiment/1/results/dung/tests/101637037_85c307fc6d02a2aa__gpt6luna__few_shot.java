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
    public void connectionFactoryRequiresStartedBroker() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("broker", false, false);
        try {
            rule.connectionFactory();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals("Can not create ConnectionFactory before the broker has started", expected.getMessage());
        }
    }

    @Test
    public void activeMqConnectionFactoryRequiresStartedBroker() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("broker", false, false);
        try {
            rule.activeMqConnectionFactory();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals("Can not create ConnectionFactory before the broker has started", expected.getMessage());
        }
    }

    @Test
    public void brokerUriRequiresStartedBroker() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("broker", false, false);
        try {
            rule.brokerUri();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals("Can not create broker URI before the broker has started", expected.getMessage());
        }
    }

    @Test
    public void applyStartsBrokerForEvaluationAndStopsItAfterward() throws Throwable {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("arrow-test-broker", false, false);
        final boolean[] evaluated = {false};
        Statement base = new Statement() {
            @Override
            public void evaluate() {
                evaluated[0] = true;
            }
        };

        Statement wrapped = rule.apply(base, Description.createTestDescription("ExampleTest", "test"));
        wrapped.evaluate();

        assertTrue(evaluated[0]);
        try {
            rule.brokerUri();
            fail("Expected broker to be stopped");
        } catch (IllegalStateException expected) {
            assertEquals("Can not create broker URI before the broker has started", expected.getMessage());
        }
    }

    @Test
    public void brokerUriAndConnectionFactoriesAreAvailableDuringEvaluation() throws Throwable {
        final EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("arrow-test-broker", true, false);
        final URI[] uri = new URI[1];
        final ActiveMQConnectionFactory[] activeFactory = new ActiveMQConnectionFactory[1];
        final ConnectionFactory[] factory = new ConnectionFactory[1];

        Statement base = new Statement() {
            @Override
            public void evaluate() {
                uri[0] = rule.brokerUri();
                activeFactory[0] = rule.activeMqConnectionFactory();
                factory[0] = rule.connectionFactory();
            }
        };

        rule.apply(base, Description.createTestDescription("ExampleTest", "test")).evaluate();

        assertNotNull(uri[0]);
        assertEquals("vm", uri[0].getScheme());
        assertEquals("arrow-test-broker", uri[0].getHost());
        assertSame(activeFactory[0], factory[0]);
    }

    @Test
    public void brokerIsStoppedWhenBaseStatementThrows() throws Throwable {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("arrow-test-broker", false, false);
        Statement base = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                throw new Exception("base failure");
            }
        };

        try {
            rule.apply(base, Description.createTestDescription("ExampleTest", "test")).evaluate();
            fail("Expected base exception");
        } catch (Exception expected) {
            assertEquals("base failure", expected.getMessage());
        }

        try {
            rule.brokerUri();
            fail("Expected broker to be stopped");
        } catch (IllegalStateException expected) {
            assertEquals("Can not create broker URI before the broker has started", expected.getMessage());
        }
    }
}
