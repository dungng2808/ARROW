package org.zapodot.junit.jms.impl;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.zapodot.junit.jms.EmbeddedJmsRule;

import javax.jms.ConnectionFactory;
import java.io.File;
import java.net.URI;

import static org.junit.Assert.*;

public class EmbeddedJmsRuleImpl_ArrowTest {

    @Test
    public void testConstructor() {
        EmbeddedJmsRule rule = new EmbeddedJmsRuleImpl("test", true, true);
        assertNotNull(rule);
    }

    @Test
    public void testConnectionFactory() {
        EmbeddedJmsRule rule = new EmbeddedJmsRuleImpl("test", true, true);
        try {
            rule.connectionFactory();
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("Can not create ConnectionFactory before the broker has started", e.getMessage());
        }
    }

    @Test
    public void testActiveMqConnectionFactory() {
        EmbeddedJmsRule rule = new EmbeddedJmsRuleImpl("test", true, true);
        try {
            rule.activeMqConnectionFactory();
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("Can not create ConnectionFactory before the broker has started", e.getMessage());
        }
    }

    @Test
    public void testBrokerUri() {
        EmbeddedJmsRule rule = new EmbeddedJmsRuleImpl("test", true, true);
        try {
            rule.brokerUri();
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("Can not create broker URI before the broker has started", e.getMessage());
        }
    }

    @Test
    public void testApply() throws Throwable {
        EmbeddedJmsRule rule = new EmbeddedJmsRuleImpl("test", true, true);
        Description description = Description.createTestDescription(this.getClass(), "testMethod");
        Statement statement = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                // Empty statement for testing
            }
        };

        Statement appliedStatement = rule.apply(statement, description);
        assertNotNull(appliedStatement);
    }

    @Test
    public void testGetBrokerNameWithPredefinedName() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("predefined", true, true);
        Description description = Description.createTestDescription(this.getClass(), "testMethod");
        String brokerName = rule.getBrokerName(description);
        assertEquals("predefined", brokerName);
    }

    @Test
    public void testGetBrokerNameWithoutPredefinedName() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, true, true);
        Description description = Description.createTestDescription(this.getClass(), "testMethod");
        String brokerName = rule.getBrokerName(description);
        assertEquals("EmbeddedJmsRuleImpl_ArrowTest", brokerName);
    }

    @Test
    public void testExtractNameFromDescriptionWithClass() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, true, true);
        Description description = Description.createTestDescription(this.getClass(), "testMethod");
        String name = rule.extractNameFromDescription(description);
        assertEquals("EmbeddedJmsRuleImpl_ArrowTest", name);
    }

    @Test
    public void testExtractNameFromDescriptionWithoutClass() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, true, true);
        Description description = Description.createSuiteDescription("suiteName");
        String name = rule.extractNameFromDescription(description);
        assertEquals("suiteName", name);
    }

    @Test
    public void testCreateVmTransportUri() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, true, true);
        URI uri = rule.createVmTransportUri("test");
        assertNotNull(uri);
        assertTrue(uri.toString().contains("vm://test?marshal=true"));
    }

    @Test
    public void testCreateBrokerService() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, true, true);
        BrokerService broker = rule.createBrokerService("test");
        assertNotNull(broker);
        assertEquals("test", broker.getBrokerName());
        assertTrue(broker.isPersistent());
    }
}
