package org.jeasy.rules.support.composite;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class UnitRuleGroup_ArrowTest {

    @Test
    public void evaluateReturnsFalseWhenGroupIsEmpty() {
        UnitRuleGroup group = new UnitRuleGroup();
        assertFalse(group.evaluate(new Facts()));
    }

    @Test
    public void evaluateReturnsTrueWhenAllRulesEvaluateTrue() {
        UnitRuleGroup group = new UnitRuleGroup();
        Facts facts = new Facts();
        Rule first = mock(Rule.class);
        Rule second = mock(Rule.class);
        when(first.evaluate(facts)).thenReturn(true);
        when(second.evaluate(facts)).thenReturn(true);
        group.addRule(first);
        group.addRule(second);

        assertTrue(group.evaluate(facts));
    }

    @Test
    public void evaluateReturnsFalseWhenARuleEvaluatesFalse() {
        UnitRuleGroup group = new UnitRuleGroup();
        Facts facts = new Facts();
        Rule first = mock(Rule.class);
        Rule second = mock(Rule.class);
        when(first.evaluate(facts)).thenReturn(true);
        when(second.evaluate(facts)).thenReturn(false);
        group.addRule(first);
        group.addRule(second);

        assertFalse(group.evaluate(facts));
    }

    @Test
    public void executeExecutesEveryRule() throws Exception {
        UnitRuleGroup group = new UnitRuleGroup();
        Facts facts = new Facts();
        Rule first = mock(Rule.class);
        Rule second = mock(Rule.class);
        group.addRule(first);
        group.addRule(second);

        group.execute(facts);

        verify(first).execute(facts);
        verify(second).execute(facts);
    }
}
