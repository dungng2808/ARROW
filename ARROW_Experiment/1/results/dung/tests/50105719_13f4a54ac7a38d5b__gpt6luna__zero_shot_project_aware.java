package enkan.component;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;

public class ComponentRelationship_ArrowTest {
    @Test
    public void sortMovesDependentBeforeTarget() {
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dependency");
        LinkedList<String> order = new LinkedList<>(Arrays.asList("target", "dependency"));

        relationship.sort(order);

        assertThat(order).containsExactly("dependency", "target");
    }

    @Test
    public void sortLeavesDependentAlreadyBeforeTargetInPlace() {
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dependency");
        LinkedList<String> order = new LinkedList<>(Arrays.asList("dependency", "target"));

        relationship.sort(order);

        assertThat(order).containsExactly("dependency", "target");
    }

    @Test
    public void sortMovesMultipleDependentsBeforeTarget() {
        ComponentRelationship relationship = ComponentRelationship.component("target").using("first", "second");
        LinkedList<String> order = new LinkedList<>(Arrays.asList("target", "first", "second"));

        relationship.sort(order);

        assertThat(order).containsExactly("first", "second", "target");
    }

    @Test
    public void sortDoesNothingWhenThereAreNoDependents() {
        ComponentRelationship relationship = ComponentRelationship.component("target").using();
        LinkedList<String> order = new LinkedList<>(Arrays.asList("target", "other"));

        relationship.sort(order);

        assertThat(order).containsExactly("target", "other");
    }
}
