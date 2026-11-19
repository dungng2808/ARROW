package org.zapodot.junit.jms.impl;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.jms.ConnectionFactory;
import java.io.File;
import java.net.URI;

import static org.junit.Assert.*;

public class EmbeddedJmsRuleImpl_ArrowTest {

    @Test
    public void testConstructor() {
        final EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("test", true, false);
        assertNotNull(rule);
    }

    @Test
    public void testConnectionFactory() {
        final EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("test", true, false);
        try {
            rule.connectionFactory();
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("Can not create ConnectionFactory before the broker has started", e.getMessage());
        }
    }

    @Test
    public void testActiveMqConnectionFactory() {
        final EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("test", true, false);
        try {
            rule.activeMqConnectionFactory();
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("Can not create ConnectionFactory before the broker has started", e.getMessage());
        }
    }

    @Test
    public void testBrokerUri() {
        final EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("test", true, false);
        try {
            rule.brokerUri();
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("Can not create broker URI before the broker has started", e.getMessage());
        }
    }

    @Test
    public void testApply() throws Throwable {
        final EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("test", true, false);
        final Description description = Description.createTestDescription("TestClass", "testMethod");
        final Statement baseStatement = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                // Empty statement for testing
            }
        };

        final Statement appliedStatement = rule.apply(baseStatement, description);
        assertNotNull(appliedStatement);
        
        // This will start and stop the broker, but we're just checking that it doesn't throw
        appliedStatement.evaluate();
    }

    @Test
    public void testGetBrokerNameWithPredefinedName() {
        final EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("predefined", true, false);
        final Description description = Description.createTestDescription("TestClass", "testMethod");
        final String brokerName = rule.getBrokerName(description);
        assertEquals("predefined", brokerName);
    }

    @Test
    public void testGetBrokerNameWithoutPredefinedName() {
        final EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, true, false);
        final Description description = Description.createTestDescription("TestClass", "testMethod");
        final String brokerName = rule.getBrokerName(description);
        assertEquals("TestClass", brokerName);
    }

    @Test
    public void testExtractNameFromDescriptionWithClass() {
        final EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, true, false);
        final Description description = Description.createTestDescription("com.example.TestClass", "testMethod");
        final String name = rule.extractNameFromDescription(description);
        assertEquals("TestClass", name);
    }

    @Test
    public void testExtractNameFromDescriptionWithoutClass() {
        final EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, true, false);
        final Description description = Description.createTestDescription((Class<?>) null, "testMethod");
        final String name = rule.extractNameFromDescription(description);
        assertEquals("testMethod", name);
    }

    @Test
    public void testCreateVmTransportUri() {
        final EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, true, false);
        final URI uri = rule.createVmTransportUri("test");
        assertNotNull(uri);
        assertTrue(uri.toString().contains("vm://test?marshal=true"));
    }

    @Test
    public void testCreateBrokerService() {
        final EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, true, false);
        final BrokerService broker = rule.createBrokerService("test");
        assertNotNull(broker);
        assertEquals("test", broker.getBrokerName());
        assertTrue(broker.isPersistent());
        assertFalse(broker.isUseJmx());
    }

    @Test
    public void testCreateVmTransportServer() {
        final EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, true, false);
        final URI uri = rule.createVmTransportUri("test");
        assertNotNull(uri);
        // This should not throw
        rule.createVmTransportServer(uri);
    }
}
