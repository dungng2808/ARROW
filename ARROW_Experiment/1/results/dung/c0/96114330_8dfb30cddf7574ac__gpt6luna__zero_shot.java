package org.swblocks.decisiontree;

import org.junit.Test;
import org.swblocks.decisiontree.change.domain.Change;
import org.swblocks.decisiontree.change.domain.builder.ChangeBuilder;
import org.swblocks.decisiontree.domain.DecisionTreeRuleSet;
import org.swblocks.decisiontree.tree.DecisionTreeType;
import org.swblocks.jbl.builders.Builder;
import org.swblocks.jbl.util.DateRange;

import static org.junit.Assert.assertNotNull;

public class ChangeableDecisionTree_ArrowTest {
    @Test
    public void instanceOfCreatesTree() {
        ChangeableDecisionTree tree = ChangeableDecisionTree.instanceOf(
                null, null, null);
        assertNotNull(tree);
    }

    @Test
    public void createChangeCreatesBuilder() {
        ChangeableDecisionTree tree = ChangeableDecisionTree.instanceOf(
                null, null, null);
        Builder<ChangeBuilder, Change> builder = tree.createChange("user", null);
        assertNotNull(builder);
    }
}
