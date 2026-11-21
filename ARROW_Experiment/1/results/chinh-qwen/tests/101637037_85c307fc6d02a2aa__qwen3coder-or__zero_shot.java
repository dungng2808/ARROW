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
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("testName", true, false);
        assertNotNull(rule);
    }

    @Test
    public void testConnectionFactory() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, false, false);
        try {
            rule.connectionFactory();
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("Can not create ConnectionFactory before the broker has started", e.getMessage());
        }
    }

    @Test
    public void testActiveMqConnectionFactory() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, false, false);
        try {
            rule.activeMqConnectionFactory();
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("Can not create ConnectionFactory before the broker has started", e.getMessage());
        }
    }

    @Test
    public void testBrokerUri() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, false, false);
        try {
            rule.brokerUri();
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("Can not create broker URI before the broker has started", e.getMessage());
        }
    }

    @Test
    public void testApply() throws Throwable {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, false, false);
        Description description = Description.createTestDescription("TestClass", "testMethod");
        Statement baseStatement = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                // Empty statement for testing
            }
        };

        Statement result = rule.apply(baseStatement, description);
        assertNotNull(result);
    }

    @Test
    public void testGetBrokerNameWithPredefinedName() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl("predefinedName", false, false);
        Description description = Description.createTestDescription("TestClass", "testMethod");
        String result = rule.getBrokerName(description);
        assertEquals("predefinedName", result);
    }

    @Test
    public void testGetBrokerNameWithoutPredefinedName() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, false, false);
        Description description = Description.createTestDescription("TestClass", "testMethod");
        String result = rule.getBrokerName(description);
        assertEquals("TestClass", result);
    }

    @Test
    public void testExtractNameFromDescriptionWithClass() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, false, false);
        Description description = Description.createTestDescription("TestClass", "testMethod");
        String result = rule.extractNameFromDescription(description);
        assertEquals("TestClass", result);
    }

    @Test
    public void testExtractNameFromDescriptionWithoutClass() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, false, false);
        Description description = Description.createSuiteDescription("suiteName");
        String result = rule.extractNameFromDescription(description);
        assertEquals("suiteName", result);
    }

    @Test
    public void testCreateVmTransportUri() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, true, false);
        URI result = rule.createVmTransportUri("testName");
        assertNotNull(result);
        assertTrue(result.toString().contains("vm://testName?marshal=true"));
    }

    @Test
    public void testCreateBrokerService() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, false, true);
        BrokerService result = rule.createBrokerService("testBroker");
        assertNotNull(result);
        assertEquals("testBroker", result.getBrokerName());
        assertTrue(result.isPersistent());
    }

    @Test
    public void testCreateVmTransportServer() {
        EmbeddedJmsRuleImpl rule = new EmbeddedJmsRuleImpl(null, false, false);
        URI uri = rule.createVmTransportUri("testName");
        assertNotNull(uri);
        // This will not throw an exception if the URI is valid
    }
}
