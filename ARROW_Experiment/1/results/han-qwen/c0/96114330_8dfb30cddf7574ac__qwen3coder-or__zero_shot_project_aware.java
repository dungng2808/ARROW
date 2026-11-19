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
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ChangeableDecisionTree_ArrowTest {

    @Test
    public void testInstanceOf() {
        Loader<DecisionTreeRuleSet> loader = mock(Loader.class);
        DecisionTreeType type = DecisionTreeType.BINARY;
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
        DecisionTreeType type = DecisionTreeType.BINARY;
        Persister persister = mock(Persister.class);
        
        DecisionTreeRuleSet ruleSet = mock(DecisionTreeRuleSet.class);
        when(loader.load()).thenReturn(ruleSet);
        
        ChangeableDecisionTree tree = ChangeableDecisionTree.instanceOf(loader, type, persister);
        
        String user = "testUser";
        DateRange range = new DateRange(new Date(), new Date());
        
        Builder<ChangeBuilder, Change> builder = tree.createChange(user, range);
        
        assertNotNull(builder);
    }

    @Test
    public void testApplyChange() {
        Loader<DecisionTreeRuleSet> loader = mock(Loader.class);
        DecisionTreeType type = DecisionTreeType.BINARY;
        Persister persister = mock(Persister.class);
        
        DecisionTreeRuleSet ruleSet = mock(DecisionTreeRuleSet.class);
        when(loader.load()).thenReturn(ruleSet);
        
        ChangeableDecisionTree tree = ChangeableDecisionTree.instanceOf(loader, type, persister);
        
        String user = "testUser";
        DateRange range = new DateRange(new Date(), new Date());
        Builder<ChangeBuilder, Change> builder = tree.createChange(user, range);
        
        String approver = "approverUser";
        Change change = tree.applyChange(builder, approver);
        
        assertNotNull(change);
        verify(persister, times(1)).put(ruleSet);
    }

    @Test
    public void testApplyChangeWithNullPersister() {
        Loader<DecisionTreeRuleSet> loader = mock(Loader.class);
        DecisionTreeType type = DecisionTreeType.BINARY;
        Persister persister = null;
        
        DecisionTreeRuleSet ruleSet = mock(DecisionTreeRuleSet.class);
        when(loader.load()).thenReturn(ruleSet);
        
        ChangeableDecisionTree tree = ChangeableDecisionTree.instanceOf(loader, type, persister);
        
        String user = "testUser";
        DateRange range = new DateRange(new Date(), new Date());
        Builder<ChangeBuilder, Change> builder = tree.createChange(user, range);
        
        String approver = "approverUser";
        Change change = tree.applyChange(builder, approver);
        
        assertNotNull(change);
        verify(persister, never()).put(ruleSet);
    }
}
