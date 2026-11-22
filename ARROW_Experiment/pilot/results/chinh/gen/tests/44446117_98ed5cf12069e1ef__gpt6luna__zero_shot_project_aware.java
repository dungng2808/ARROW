package br.ufc.insightlab.graphast.model;

import br.ufc.insightlab.graphast.model.components.NodeComponent;
import org.junit.Test;

import static org.junit.Assert.*;

public class Node_ArrowTest {

    public static class TestNodeComponent extends NodeComponent {
    }

    @Test
    public void defaultConstructorInitializesIdToZero() {
        Node node = new Node();

        assertEquals(0L, node.getId());
    }

    @Test
    public void constructorSetsId() {
        Node node = new Node(42L);

        assertEquals(42L, node.getId());
    }

    @Test
    public void setIdUpdatesId() {
        Node node = new Node(1L);

        node.setId(-7L);

        assertEquals(-7L, node.getId());
    }

    @Test
    public void equalsUsesNodeId() {
        Node first = new Node(12L);
        Node sameId = new Node(12L);
        Node differentId = new Node(13L);

        assertTrue(first.equals(sameId));
        assertFalse(first.equals(differentId));
        assertFalse(first.equals("12"));
    }

    @Test
    public void hashCodeMatchesLongIdHashCode() {
        Node node = new Node(1234567890123L);

        assertEquals(Long.valueOf(1234567890123L).hashCode(), node.hashCode());
    }

    @Test
    public void addComponentStoresComponentAndSetsItsNode() {
        Node node = new Node(5L);
        TestNodeComponent component = new TestNodeComponent();

        node.addComponent(component);

        assertSame(component, node.getComponent(TestNodeComponent.class));
        assertTrue(node.hasComponent(TestNodeComponent.class));
        assertSame(node, component.getNode());
    }

    @Test
    public void addComponentWithExplicitKeyStoresComponent() {
        Node node = new Node(5L);
        TestNodeComponent component = new TestNodeComponent();

        node.addComponent(TestNodeComponent.class, component);

        assertSame(component, node.getComponent(TestNodeComponent.class));
        assertTrue(node.hasComponent(TestNodeComponent.class));
    }

    @Test
    public void getComponentReturnsNullWhenComponentIsAbsent() {
        Node node = new Node(5L);

        assertNull(node.getComponent(TestNodeComponent.class));
        assertFalse(node.hasComponent(TestNodeComponent.class));
    }

    @Test
    public void getAllComponentClassesContainsAddedComponentClass() {
        Node node = new Node(5L);
        TestNodeComponent component = new TestNodeComponent();

        node.addComponent(component);

        assertEquals(1, node.getAllComponentClasses().size());
        assertTrue(node.getAllComponentClasses().contains(TestNodeComponent.class));
    }
}
