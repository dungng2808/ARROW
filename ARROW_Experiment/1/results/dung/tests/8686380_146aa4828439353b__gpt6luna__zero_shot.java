package org.jeasy.rules.support.composite;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UnitRuleGroup_ArrowTest {

    @Test
    public void evaluateReturnsFalseWhenGroupIsEmpty() {
        UnitRuleGroup group = new UnitRuleGroup();
        assertFalse(group.evaluate(new Facts()));
    }

    @Test
    public void evaluateReturnsTrueWhenEveryRuleEvaluatesTrue() {
        UnitRuleGroup group = new UnitRuleGroup();
        group.addRule(new Rule() {
            @Override
            public boolean evaluate(Facts facts) {
                return true;
            }

            @Override
            public void execute(Facts facts) {
            }

            @Override
            public String getName() {
                return "first";
            }

            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public int getPriority() {
                return 0;
            }
        });
        group.addRule(new Rule() {
            @Override
            public boolean evaluate(Facts facts) {
                return true;
            }

            @Override
            public void execute(Facts facts) {
            }

            @Override
            public String getName() {
                return "second";
            }

            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public int getPriority() {
                return 0;
            }
        });

        assertTrue(group.evaluate(new Facts()));
    }

    @Test
    public void evaluateReturnsFalseWhenAnyRuleEvaluatesFalse() {
        UnitRuleGroup group = new UnitRuleGroup();
        group.addRule(new Rule() {
            @Override
            public boolean evaluate(Facts facts) {
                return true;
            }

            @Override
            public void execute(Facts facts) {
            }

            @Override
            public String getName() {
                return "first";
            }

            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public int getPriority() {
                return 0;
            }
        });
        group.addRule(new Rule() {
            @Override
            public boolean evaluate(Facts facts) {
                return false;
            }

            @Override
            public void execute(Facts facts) {
            }

            @Override
            public String getName() {
                return "second";
            }

            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public int getPriority() {
                return 0;
            }
        });

        assertFalse(group.evaluate(new Facts()));
    }

    @Test
    public void executeRunsEveryRule() throws Exception {
        final int[] executions = {0};
        UnitRuleGroup group = new UnitRuleGroup();
        group.addRule(new Rule() {
            @Override
            public boolean evaluate(Facts facts) {
                return true;
            }

            @Override
            public void execute(Facts facts) {
                executions[0]++;
            }

            @Override
            public String getName() {
                return "first";
            }

            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public int getPriority() {
                return 0;
            }
        });
        group.addRule(new Rule() {
            @Override
            public boolean evaluate(Facts facts) {
                return true;
            }

            @Override
            public void execute(Facts facts) {
                executions[0]++;
            }

            @Override
            public String getName() {
                return "second";
            }

            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public int getPriority() {
                return 0;
            }
        });

        group.execute(new Facts());

        org.junit.Assert.assertEquals(2, executions[0]);
    }
}
