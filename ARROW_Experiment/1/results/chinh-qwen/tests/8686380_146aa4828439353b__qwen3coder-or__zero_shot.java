package org.jeasy.rules.support.composite;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class UnitRuleGroup_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();
        assertNotNull(unitRuleGroup);
    }

    @Test
    public void testNameConstructor() {
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup("test");
        assertNotNull(unitRuleGroup);
        assertEquals("test", unitRuleGroup.getName());
    }

    @Test
    public void testNameAndDescriptionConstructor() {
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup("test", "description");
        assertNotNull(unitRuleGroup);
        assertEquals("test", unitRuleGroup.getName());
        assertEquals("description", unitRuleGroup.getDescription());
    }

    @Test
    public void testFullConstructor() {
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup("test", "description", 5);
        assertNotNull(unitRuleGroup);
        assertEquals("test", unitRuleGroup.getName());
        assertEquals("description", unitRuleGroup.getDescription());
        assertEquals(5, unitRuleGroup.getPriority());
    }

    @Test
    public void testEvaluateEmptyRules() {
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();
        Facts facts = new Facts();
        assertFalse(unitRuleGroup.evaluate(facts));
    }

    @Test
    public void testEvaluateAllRulesTrue() throws Exception {
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();
        Rule rule1 = new MockRule(true);
        Rule rule2 = new MockRule(true);
        unitRuleGroup.addRule(rule1);
        unitRuleGroup.addRule(rule2);
        Facts facts = new Facts();
        assertTrue(unitRuleGroup.evaluate(facts));
    }

    @Test
    public void testEvaluateOneRuleFalse() throws Exception {
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();
        Rule rule1 = new MockRule(true);
        Rule rule2 = new MockRule(false);
        unitRuleGroup.addRule(rule1);
        unitRuleGroup.addRule(rule2);
        Facts facts = new Facts();
        assertFalse(unitRuleGroup.evaluate(facts));
    }

    @Test
    public void testExecute() throws Exception {
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();
        Rule rule1 = new MockRule(true);
        Rule rule2 = new MockRule(true);
        unitRuleGroup.addRule(rule1);
        unitRuleGroup.addRule(rule2);
        Facts facts = new Facts();
        unitRuleGroup.execute(facts);
        assertTrue(((MockRule) rule1).isExecuted());
        assertTrue(((MockRule) rule2).isExecuted());
    }

    private static class MockRule implements Rule {
        private final boolean evaluateResult;
        private boolean executed = false;

        public MockRule(boolean evaluateResult) {
            this.evaluateResult = evaluateResult;
        }

        @Override
        public String getName() {
            return "mock";
        }

        @Override
        public String getDescription() {
            return "mock description";
        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public boolean evaluate(Facts facts) {
            return evaluateResult;
        }

        @Override
        public void execute(Facts facts) throws Exception {
            executed = true;
        }

        public boolean isExecuted() {
            return executed;
        }
    }
}
