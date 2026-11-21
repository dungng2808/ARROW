package enkan.component;

import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;

public class ComponentRelationship_ArrowTest {
    @Test
    public void sortMovesDependentBeforeTarget() {
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dependency");
        LinkedList<String> order = new LinkedList<>(Arrays.asList("target", "dependency", "other"));

        relationship.sort(order);

        assertEquals(Arrays.asList("dependency", "target", "other"), order);
    }

    @Test
    public void sortLeavesDependentAlreadyBeforeTarget() {
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dependency");
        LinkedList<String> order = new LinkedList<>(Arrays.asList("dependency", "target", "other"));

        relationship.sort(order);

        assertEquals(Arrays.asList("dependency", "target", "other"), order);
    }

    @Test
    public void sortMovesMultipleDependentsBeforeTarget() {
        ComponentRelationship relationship = ComponentRelationship.component("target").using("first", "second");
        LinkedList<String> order = new LinkedList<>(Arrays.asList("target", "first", "second", "other"));

        relationship.sort(order);

        assertEquals(Arrays.asList("first", "second", "target", "other"), order);
    }

    @Test
    public void sortWithNoDependentsLeavesOrderUnchanged() {
        ComponentRelationship relationship = ComponentRelationship.component("target").using();
        LinkedList<String> order = new LinkedList<>(Arrays.asList("target", "other"));

        relationship.sort(order);

        assertEquals(Arrays.asList("target", "other"), order);
    }
}
