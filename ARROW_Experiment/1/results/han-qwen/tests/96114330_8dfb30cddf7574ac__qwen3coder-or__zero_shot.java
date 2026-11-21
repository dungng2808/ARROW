package org.swblocks.decisiontree;

import org.junit.Test;
import org.swblocks.decisiontree.change.domain.Audit;
import org.swblocks.decisiontree.change.domain.Change;
import org.swblocks.decisiontree.change.domain.builder.ChangeBuilder;
import org.swblocks.decisiontree.domain.DecisionTreeRuleSet;
import org.swblocks.decisiontree.tree.DecisionTreeType;
import org.swblocks.jbl.builders.Builder;
import org.swblocks.jbl.util.DateRange;

import java.time.Instant;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ChangeableDecisionTree_ArrowTest {

    @Test
    public void testInstanceOf() {
        Loader<DecisionTreeRuleSet> loader = mock(Loader.class);
        DecisionTreeType type = DecisionTreeType.BASIC;
        Persister persister = mock(Persister.class);
        
        DecisionTreeRuleSet ruleSet = mock(DecisionTreeRuleSet.class);
        when(loader.load()).thenReturn(ruleSet);
        
        ChangeableDecisionTree tree = ChangeableDecisionTree.instanceOf(loader, type, persister);
        
        assertNotNull(tree);
        assertEquals(type, tree.getType());
    }

    @Test
    public void testCreateChange() {
        Loader<DecisionTreeRuleSet> loader = mock(Loader.class);
        DecisionTreeType type = DecisionTreeType.BASIC;
        Persister persister = mock(Persister.class);
        
        DecisionTreeRuleSet ruleSet = mock(DecisionTreeRuleSet.class);
        when(loader.load()).thenReturn(ruleSet);
        
        ChangeableDecisionTree tree = ChangeableDecisionTree.instanceOf(loader, type, persister);
        
        DateRange range = mock(DateRange.class);
        Builder<ChangeBuilder, Change> builder = tree.createChange("testUser", range);
        
        assertNotNull(builder);
    }

    @Test
    public void testApplyChange() {
        Loader<DecisionTreeRuleSet> loader = mock(Loader.class);
        DecisionTreeType type = DecisionTreeType.BASIC;
        Persister persister = mock(Persister.class);
        
        DecisionTreeRuleSet ruleSet = mock(DecisionTreeRuleSet.class);
        when(loader.load()).thenReturn(ruleSet);
        
        ChangeableDecisionTree tree = ChangeableDecisionTree.instanceOf(loader, type, persister);
        
        DateRange range = mock(DateRange.class);
        Builder<ChangeBuilder, Change> builder = tree.createChange("testUser", range);
        
        Change change = mock(Change.class);
        when(builder.build()).thenReturn(change);
        
        Change result = tree.applyChange(builder, "approver");
        
        assertNotNull(result);
        verify(persister).put(ruleSet);
    }

    @Test
    public void testApplyChangeWithNullPersister() {
        Loader<DecisionTreeRuleSet> loader = mock(Loader.class);
        DecisionTreeType type = DecisionTreeType.BASIC;
        Persister persister = null;
        
        DecisionTreeRuleSet ruleSet = mock(DecisionTreeRuleSet.class);
        when(loader.load()).thenReturn(ruleSet);
        
        ChangeableDecisionTree tree = ChangeableDecisionTree.instanceOf(loader, type, persister);
        
        DateRange range = mock(DateRange.class);
        Builder<ChangeBuilder, Change> builder = tree.createChange("testUser", range);
        
        Change change = mock(Change.class);
        when(builder.build()).thenReturn(change);
        
        Change result = tree.applyChange(builder, "approver");
        
        assertNotNull(result);
    }
}
