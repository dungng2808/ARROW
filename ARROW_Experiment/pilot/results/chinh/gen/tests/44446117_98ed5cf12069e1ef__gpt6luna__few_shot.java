package br.ufc.insightlab.graphast.model;

import br.ufc.insightlab.graphast.model.components.NodeComponent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class Node_ArrowTest {

    private static class TestNodeComponent extends NodeComponent {
    }

    @Test
    public void constructorAndIdAccessors() {
        Node node = new Node(42L);
        assertEquals(42L, node.getId());

        node.setId(17L);
        assertEquals(17L, node.getId());
    }

    @Test
    public void defaultConstructorUsesZeroId() {
        Node node = new Node();
        assertEquals(0L, node.getId());
    }

    @Test
    public void equalityUsesNodeId() {
        Node first = new Node(5L);
        Node sameId = new Node(5L);
        Node differentId = new Node(6L);

        assertTrue(first.equals(sameId));
        assertFalse(first.equals(differentId));
        assertFalse(first.equals("5"));
    }

    @Test
    public void hashCodeUsesId() {
        Node node = new Node(123L);
        assertEquals(Long.valueOf(123L).hashCode(), node.hashCode());
    }

    @Test
    public void componentCanBeAddedAndRetrievedByItsClass() {
        Node node = new Node(1L);
        TestNodeComponent component = new TestNodeComponent();

        node.addComponent(component);

        assertSame(component, node.getComponent(TestNodeComponent.class));
        assertTrue(node.hasComponent(TestNodeComponent.class));
        assertSame(node, component.getNode());
    }

    @Test
    public void componentCanBeAddedWithExplicitKey() {
        Node node = new Node(2L);
        TestNodeComponent component = new TestNodeComponent();

        node.addComponent(TestNodeComponent.class, component);

        assertSame(component, node.getComponent(TestNodeComponent.class));
        assertTrue(node.hasComponent(TestNodeComponent.class));
    }

    @Test
    public void missingComponentReturnsNullAndIsNotPresent() {
        Node node = new Node(3L);

        assertNull(node.getComponent(TestNodeComponent.class));
        assertFalse(node.hasComponent(TestNodeComponent.class));
    }

    @Test
    public void allComponentClassesContainsAddedClass() {
        Node node = new Node(4L);
        node.addComponent(new TestNodeComponent());

        assertNotNull(node.getAllComponentClasses());
        assertEquals(1, node.getAllComponentClasses().size());
        assertTrue(node.getAllComponentClasses().contains(TestNodeComponent.class));
    }
}
