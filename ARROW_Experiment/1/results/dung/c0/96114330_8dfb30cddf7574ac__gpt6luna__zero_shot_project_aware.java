package org.swblocks.decisiontree;

import org.junit.Test;
import org.swblocks.decisiontree.domain.DecisionTreeRuleSet;
import org.swblocks.decisiontree.tree.DecisionTreeType;

import static org.junit.Assert.assertNotNull;

public class ChangeableDecisionTree_ArrowTest {
    @Test
    public void instanceOfCreatesTreeFromLoader() {
        Loader<DecisionTreeRuleSet> loader = null;
        ChangeableDecisionTree tree = ChangeableDecisionTree.instanceOf(
                loader, DecisionTreeType.class.cast(null), null);
        assertNotNull(tree);
    }
}
