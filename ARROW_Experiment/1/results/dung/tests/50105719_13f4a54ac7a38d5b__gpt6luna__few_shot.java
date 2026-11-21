package enkan.component;

import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;

public class ComponentRelationship_ArrowTest {

    @Test
    public void componentCreatesRelationshipThatSortsDependentBeforeTarget() {
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dependency");
        LinkedList<String> order = new LinkedList<>();
        order.add("target");
        order.add("dependency");

        relationship.sort(order);

        assertEquals(java.util.Arrays.asList("dependency", "target"), order);
    }

    @Test
    public void sortLeavesOrderUnchangedWhenDependentIsAlreadyBeforeTarget() {
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dependency");
        LinkedList<String> order = new LinkedList<>();
        order.add("dependency");
        order.add("target");

        relationship.sort(order);

        assertEquals(java.util.Arrays.asList("dependency", "target"), order);
    }

    @Test
    public void sortLeavesOrderUnchangedWhenDependentIsNotInOrder() {
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dependency");
        LinkedList<String> order = new LinkedList<>();
        order.add("target");

        relationship.sort(order);

        assertEquals(java.util.Arrays.asList("target"), order);
    }
}
