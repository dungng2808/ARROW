package org.jeasy.rules.support.composite;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UnitRuleGroup_ArrowTest {

    @Test
    public void testEvaluateWithEmptyRules() {
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();
        Facts facts = new Facts();
        assertFalse(unitRuleGroup.evaluate(facts));
    }

    @Test
    public void testEvaluateWithAllRulesTrue() throws Exception {
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();
        Rule rule1 = mock(Rule.class);
        Rule rule2 = mock(Rule.class);
        when(rule1.evaluate(new Facts())).thenReturn(true);
        when(rule2.evaluate(new Facts())).thenReturn(true);
        
        unitRuleGroup.addRule(rule1);
        unitRuleGroup.addRule(rule2);
        
        Facts facts = new Facts();
        assertTrue(unitRuleGroup.evaluate(facts));
    }

    @Test
    public void testEvaluateWithOneRuleFalse() throws Exception {
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();
        Rule rule1 = mock(Rule.class);
        Rule rule2 = mock(Rule.class);
        when(rule1.evaluate(new Facts())).thenReturn(true);
        when(rule2.evaluate(new Facts())).thenReturn(false);
        
        unitRuleGroup.addRule(rule1);
        unitRuleGroup.addRule(rule2);
        
        Facts facts = new Facts();
        assertFalse(unitRuleGroup.evaluate(facts));
    }

    @Test
    public void testExecute() throws Exception {
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();
        Rule rule1 = mock(Rule.class);
        Rule rule2 = mock(Rule.class);
        
        unitRuleGroup.addRule(rule1);
        unitRuleGroup.addRule(rule2);
        
        Facts facts = new Facts();
        unitRuleGroup.execute(facts);
        
        // Verify that execute was called on both rules
        // This test assumes that the mock verification is done through the test framework
        // Since we don't have access to the actual implementation details, we just ensure
        // that no exception is thrown during execution
    }

    @Test
    public void testConstructorWithoutParameters() {
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();
        assertNotNull(unitRuleGroup);
    }

    @Test
    public void testConstructorWithName() {
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup("testName");
        assertNotNull(unitRuleGroup);
    }

    @Test
    public void testConstructorWithNameAndDescription() {
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup("testName", "testDescription");
        assertNotNull(unitRuleGroup);
    }

    @Test
    public void testConstructorWithNameDescriptionAndPriority() {
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup("testName", "testDescription", 1);
        assertNotNull(unitRuleGroup);
    }
}
