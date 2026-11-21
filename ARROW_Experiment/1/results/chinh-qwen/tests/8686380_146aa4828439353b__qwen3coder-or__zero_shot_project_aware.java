package org.jeasy.rules.support.composite;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class UnitRuleGroup_ArrowTest {

    @Mock
    private Rule rule1;

    @Mock
    private Rule rule2;

    @Mock
    private Facts facts;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testEvaluate_EmptyRuleList_ReturnsFalse() {
        // Given
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();

        // When
        boolean result = unitRuleGroup.evaluate(facts);

        // Then
        assertFalse(result);
    }

    @Test
    public void testEvaluate_OneRuleEvaluatesToFalse_ReturnsFalse() {
        // Given
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();
        unitRuleGroup.addRule(rule1);
        when(rule1.evaluate(facts)).thenReturn(false);

        // When
        boolean result = unitRuleGroup.evaluate(facts);

        // Then
        assertFalse(result);
    }

    @Test
    public void testEvaluate_OneRuleEvaluatesToTrue_ReturnsTrue() {
        // Given
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();
        unitRuleGroup.addRule(rule1);
        when(rule1.evaluate(facts)).thenReturn(true);

        // When
        boolean result = unitRuleGroup.evaluate(facts);

        // Then
        assertTrue(result);
    }

    @Test
    public void testEvaluate_MultipleRulesAllEvaluateToTrue_ReturnsTrue() {
        // Given
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();
        unitRuleGroup.addRule(rule1);
        unitRuleGroup.addRule(rule2);
        when(rule1.evaluate(facts)).thenReturn(true);
        when(rule2.evaluate(facts)).thenReturn(true);

        // When
        boolean result = unitRuleGroup.evaluate(facts);

        // Then
        assertTrue(result);
    }

    @Test
    public void testEvaluate_MultipleRulesOneEvaluatesToFalse_ReturnsFalse() {
        // Given
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();
        unitRuleGroup.addRule(rule1);
        unitRuleGroup.addRule(rule2);
        when(rule1.evaluate(facts)).thenReturn(true);
        when(rule2.evaluate(facts)).thenReturn(false);

        // When
        boolean result = unitRuleGroup.evaluate(facts);

        // Then
        assertFalse(result);
    }

    @Test
    public void testExecute_NoRules_DoesNothing() throws Exception {
        // Given
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();

        // When
        unitRuleGroup.execute(facts);

        // Then
        // No exception should be thrown
        verifyZeroInteractions(facts);
    }

    @Test
    public void testExecute_OneRuleExecutesSuccessfully() throws Exception {
        // Given
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();
        unitRuleGroup.addRule(rule1);

        // When
        unitRuleGroup.execute(facts);

        // Then
        verify(rule1).execute(facts);
    }

    @Test
    public void testExecute_MultipleRulesExecuteSuccessfully() throws Exception {
        // Given
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();
        unitRuleGroup.addRule(rule1);
        unitRuleGroup.addRule(rule2);

        // When
        unitRuleGroup.execute(facts);

        // Then
        verify(rule1).execute(facts);
        verify(rule2).execute(facts);
    }

    @Test
    public void testExecute_RuleThrowsException_ThrowsException() throws Exception {
        // Given
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();
        unitRuleGroup.addRule(rule1);
        doThrow(new RuntimeException("Test exception")).when(rule1).execute(facts);

        // When & Then
        try {
            unitRuleGroup.execute(facts);
            // Should not reach here
            assertTrue(false);
        } catch (RuntimeException e) {
            assertEquals("Test exception", e.getMessage());
        }
    }

    @Test
    public void testConstructor_DefaultConstructor() {
        // When
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup();

        // Then
        assertNotNull(unitRuleGroup);
    }

    @Test
    public void testConstructor_NameConstructor() {
        // When
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup("testName");

        // Then
        assertNotNull(unitRuleGroup);
    }

    @Test
    public void testConstructor_NameAndDescriptionConstructor() {
        // When
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup("testName", "testDescription");

        // Then
        assertNotNull(unitRuleGroup);
    }

    @Test
    public void testConstructor_NameDescriptionAndPriorityConstructor() {
        // When
        UnitRuleGroup unitRuleGroup = new UnitRuleGroup("testName", "testDescription", 5);

        // Then
        assertNotNull(unitRuleGroup);
    }
}
