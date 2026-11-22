package br.ufc.insightlab.graphast.model;

import br.ufc.insightlab.graphast.model.components.NodeComponent;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class Node_ArrowTest {

    private static class TestNodeComponent extends NodeComponent {
    }

    @Test
    public void defaultConstructorInitializesIdToZero() {
        Node node = new Node();

        assertEquals(0L, node.getId());
    }

    @Test
    public void constructorStoresId() {
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
        Node second = new Node(12L);

        assertEquals(first, second);
    }

    @Test
    public void equalsReturnsFalseForDifferentId() {
        Node first = new Node(12L);
        Node second = new Node(13L);

        assertNotEquals(first, second);
    }

    @Test
    public void equalsReturnsFalseForNonNode() {
        Node node = new Node(12L);

        assertFalse(node.equals("12"));
    }

    @Test
    public void equalsReturnsFalseForNull() {
        Node node = new Node(12L);

        assertFalse(node.equals(null));
    }

    @Test
    public void hashCodeMatchesLongIdHashCode() {
        Node node = new Node(1234567890123L);

        assertEquals(Long.valueOf(1234567890123L).hashCode(), node.hashCode());
    }

    @Test
    public void getComponentReturnsNullWhenNoComponentsExist() {
        Node node = new Node();

        assertNull(node.getComponent(TestNodeComponent.class));
    }

    @Test
    public void hasComponentReturnsFalseWhenNoComponentsExist() {
        Node node = new Node();

        assertFalse(node.hasComponent(TestNodeComponent.class));
    }

    @Test
    public void addComponentMakesComponentAvailableAndSetsItsNode() {
        Node node = new Node(5L);
        TestNodeComponent component = new TestNodeComponent();

        node.addComponent(component);

        assertSame(component, node.getComponent(TestNodeComponent.class));
        assertTrue(node.hasComponent(TestNodeComponent.class));
        assertSame(node, component.getNode());
    }

    @Test
    public void addComponentWithExplicitKeyMakesComponentAvailable() {
        Node node = new Node(5L);
        TestNodeComponent component = new TestNodeComponent();

        node.addComponent(TestNodeComponent.class, component);

        assertSame(component, node.getComponent(TestNodeComponent.class));
        assertTrue(node.hasComponent(TestNodeComponent.class));
    }

    @Test
    public void getAllComponentClassesReturnsAddedComponentClass() {
        Node node = new Node(5L);
        TestNodeComponent component = new TestNodeComponent();
        node.addComponent(component);

        Set<Class<? extends NodeComponent>> classes = node.getAllComponentClasses();

        assertEquals(1, classes.size());
        assertTrue(classes.contains(TestNodeComponent.class));
    }
}
