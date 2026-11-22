package br.ufc.insightlab.graphast.model;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Set;

public class Node_ArrowTest {

    @Test
    public void testNodeDefaultConstructor() {
        Node node = new Node();
        assertEquals(0L, node.getId());
    }

    @Test
    public void testNodeIdConstructor() {
        Node node = new Node(42L);
        assertEquals(42L, node.getId());
    }

    @Test
    public void testGetId() {
        Node node = new Node(123L);
        assertEquals(123L, node.getId());
    }

    @Test
    public void testSetId() {
        Node node = new Node(1L);
        node.setId(456L);
        assertEquals(456L, node.getId());
    }

    @Test
    public void testAddComponent() {
        Node node = new Node(1L);
        TestComponent component = new TestComponent();
        node.addComponent(component);
        
        TestComponent retrieved = node.getComponent(TestComponent.class);
        assertNotNull(retrieved);
        assertEquals(component, retrieved);
    }

    @Test
    public void testAddComponentWithKey() {
        Node node = new Node(1L);
        TestComponent component = new TestComponent();
        node.addComponent(TestComponent.class, component);
        
        TestComponent retrieved = node.getComponent(TestComponent.class);
        assertNotNull(retrieved);
        assertEquals(component, retrieved);
    }

    @Test
    public void testGetComponentNull() {
        Node node = new Node(1L);
        assertNull(node.getComponent(TestComponent.class));
    }

    @Test
    public void testGetComponentWithComponent() {
        Node node = new Node(1L);
        TestComponent component = new TestComponent();
        node.addComponent(component);
        
        TestComponent retrieved = node.getComponent(TestComponent.class);
        assertNotNull(retrieved);
        assertEquals(component, retrieved);
    }

    @Test
    public void testHasComponentFalse() {
        Node node = new Node(1L);
        assertFalse(node.hasComponent(TestComponent.class));
    }

    @Test
    public void testHasComponentTrue() {
        Node node = new Node(1L);
        TestComponent component = new TestComponent();
        node.addComponent(component);
        assertTrue(node.hasComponent(TestComponent.class));
    }

    @Test
    public void testGetAllComponentClassesEmpty() {
        Node node = new Node(1L);
        // Initialize nodeComponents to avoid NPE
        node.addComponent(new TestComponent());
        // Remove the component to test empty case
        nodeComponents = null;
        Set<Class<? extends br.ufc.insightlab.graphast.model.components.NodeComponent>> classes = node.getAllComponentClasses();
        assertNotNull(classes);
        assertEquals(0, classes.size());
    }

    @Test
    public void testGetAllComponentClassesWithComponents() {
        Node node = new Node(1L);
        TestComponent component = new TestComponent();
        node.addComponent(component);
        
        Set<Class<? extends br.ufc.insightlab.graphast.model.components.NodeComponent>> classes = node.getAllComponentClasses();
        assertNotNull(classes);
        assertEquals(1, classes.size());
        assertTrue(classes.contains(TestComponent.class));
    }

    @Test
    public void testEqualsSameId() {
        Node node1 = new Node(1L);
        Node node2 = new Node(1L);
        assertTrue(node1.equals(node2));
    }

    @Test
    public void testEqualsDifferentId() {
        Node node1 = new Node(1L);
        Node node2 = new Node(2L);
        assertFalse(node1.equals(node2));
    }

    @Test
    public void testEqualsNotANode() {
        Node node = new Node(1L);
        Object obj = new Object();
        assertFalse(node.equals(obj));
    }

    @Test
    public void testHashCode() {
        Node node = new Node(123L);
        int expected = Long.valueOf(123L).hashCode();
        assertEquals(expected, node.hashCode());
    }

    // Helper class for testing components
    private static class TestComponent extends br.ufc.insightlab.graphast.model.components.NodeComponent {
        @Override
        public void setNode(Node node) {
            // Implementation not needed for testing
        }
    }
    
    // Temporary field to access nodeComponents for testing
    private Map<Class<? extends NodeComponent>, NodeComponent> nodeComponents;
}
