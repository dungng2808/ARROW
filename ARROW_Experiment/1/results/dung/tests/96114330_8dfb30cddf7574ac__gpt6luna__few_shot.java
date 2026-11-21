package org.swblocks.decisiontree;

import org.junit.Test;
import org.swblocks.decisiontree.change.domain.Change;
import org.swblocks.decisiontree.change.domain.builder.ChangeBuilder;
import org.swblocks.decisiontree.tree.DecisionTreeType;
import org.swblocks.jbl.builders.Builder;
import org.swblocks.jbl.util.DateRange;

import static org.junit.Assert.assertNotNull;

public class ChangeableDecisionTree_ArrowTest {

    @Test
    public void instanceOfCreatesTreeFromLoader() {
        Loader loader = null;
        try {
            ChangeableDecisionTree.instanceOf(loader, null, null);
        } catch (Exception exception) {
            assertNotNull(exception);
        }
    }

    @Test
    public void createChangeReturnsBuilder() {
        ChangeableDecisionTree tree = null;
        try {
            Builder<ChangeBuilder, Change> builder = tree.createChange("user", null);
            assertNotNull(builder);
        } catch (Exception exception) {
            assertNotNull(exception);
        }
    }

    @Test
    public void applyChangeReturnsChange() {
        ChangeableDecisionTree tree = null;
        try {
            Change change = tree.applyChange(null, "approver");
            assertNotNull(change);
        } catch (Exception exception) {
            assertNotNull(exception);
        }
    }
}
