package br.ufc.insightlab.graphast.model;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Set;

import org.junit.Test;

public class Node_ArrowTest {

    @Test
    public void testNodeDefaultConstructor() {
        Node node = new Node();
        assertEquals(0L, node.getId());
    }

    @Test
    public void testNodeLongConstructor() {
        Node node = new Node(123L);
        assertEquals(123L, node.getId());
    }

    @Test
    public void testSetId() {
        Node node = new Node();
        node.setId(456L);
        assertEquals(456L, node.getId());
    }

    @Test
    public void testAddComponentWithoutExistingComponents() {
        Node node = new Node(1L);
        TestNodeComponent component = new TestNodeComponent();
        
        node.addComponent(component);
        
        TestNodeComponent retrieved = node.getComponent(TestNodeComponent.class);
        assertNotNull(retrieved);
        assertEquals(component, retrieved);
        assertEquals(node, retrieved.getNode());
    }

    @Test
    public void testAddComponentWithExistingComponents() {
        Node node = new Node(1L);
        TestNodeComponent component1 = new TestNodeComponent();
        TestNodeComponent component2 = new TestNodeComponent();
        
        node.addComponent(component1);
        node.addComponent(component2);
        
        TestNodeComponent retrieved = node.getComponent(TestNodeComponent.class);
        assertNotNull(retrieved);
        assertEquals(component2, retrieved);
        assertEquals(node, retrieved.getNode());
    }

    @Test
    public void testGetComponentWhenNoComponentsExist() {
        Node node = new Node(1L);
        TestNodeComponent component = node.getComponent(TestNodeComponent.class);
        assertNull(component);
    }

    @Test
    public void testGetComponentWhenComponentExists() {
        Node node = new Node(1L);
        TestNodeComponent component = new TestNodeComponent();
        node.addComponent(component);
        
        TestNodeComponent retrieved = node.getComponent(TestNodeComponent.class);
        assertEquals(component, retrieved);
    }

    @Test
    public void testGetComponentWhenComponentDoesNotExist() {
        Node node = new Node(1L);
        TestNodeComponent component = new TestNodeComponent();
        node.addComponent(component);
        
        OtherNodeComponent otherComponent = new OtherNodeComponent();
        OtherNodeComponent retrieved = node.getComponent(OtherNodeComponent.class);
        assertNull(retrieved);
    }

    @Test
    public void testHasComponentWhenNoComponentsExist() {
        Node node = new Node(1L);
        assertFalse(node.hasComponent(TestNodeComponent.class));
    }

    @Test
    public void testHasComponentWhenComponentExists() {
        Node node = new Node(1L);
        TestNodeComponent component = new TestNodeComponent();
        node.addComponent(component);
        
        assertTrue(node.hasComponent(TestNodeComponent.class));
    }

    @Test
    public void testHasComponentWhenComponentDoesNotExist() {
        Node node = new Node(1L);
        TestNodeComponent component = new TestNodeComponent();
        node.addComponent(component);
        
        assertFalse(node.hasComponent(OtherNodeComponent.class));
    }

    @Test
    public void testGetAllComponentClassesWhenNoComponentsExist() {
        Node node = new Node(1L);
        // Ensure nodeComponents is initialized but empty
        node.addComponent(new TestNodeComponent());
        // Clear the components to test the empty case
        try {
            Field field = node.getClass().getDeclaredField("nodeComponents");
            field.setAccessible(true);
            field.set(node, new java.util.HashMap<>());
        } catch (Exception e) {
            // Ignore reflection errors
        }
        
        Set<Class<? extends br.ufc.insightlab.graphast.model.components.NodeComponent>> classes = node.getAllComponentClasses();
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
    }

    @Test
    public void testGetAllComponentClassesWhenComponentsExist() {
        Node node = new Node(1L);
        TestNodeComponent component1 = new TestNodeComponent();
        OtherNodeComponent component2 = new OtherNodeComponent();
        node.addComponent(component1);
        node.addComponent(component2);
        
        Set<Class<? extends br.ufc.insightlab.graphast.model.components.NodeComponent>> classes = node.getAllComponentClasses();
        assertNotNull(classes);
        assertTrue(classes.contains(TestNodeComponent.class));
        assertTrue(classes.contains(OtherNodeComponent.class));
        assertEquals(2, classes.size());
    }

    @Test
    public void testEqualsWhenObjectsAreEqual() {
        Node node1 = new Node(1L);
        Node node2 = new Node(1L);
        
        assertTrue(node1.equals(node2));
        assertTrue(node2.equals(node1));
    }

    @Test
    public void testEqualsWhenObjectsAreDifferent() {
        Node node1 = new Node(1L);
        Node node2 = new Node(2L);
        
        assertFalse(node1.equals(node2));
        assertFalse(node2.equals(node1));
    }

    @Test
    public void testEqualsWhenObjectIsNotNode() {
        Node node = new Node(1L);
        Object other = new Object();
        
        assertFalse(node.equals(other));
    }

    @Test
    public void testEqualsWhenComparingWithItself() {
        Node node = new Node(1L);
        
        assertTrue(node.equals(node));
    }

    @Test
    public void testHashCode() {
        Node node = new Node(123L);
        int hashCode = node.hashCode();
        assertEquals(Long.valueOf(123L).hashCode(), hashCode);
    }

    // Helper classes for testing
    private static class TestNodeComponent extends br.ufc.insightlab.graphast.model.components.NodeComponent {
        @Override
        public Node getNode() {
            return super.getNode();
        }
        
        @Override
        public void setNode(Node node) {
            super.setNode(node);
        }
    }

    private static class OtherNodeComponent extends br.ufc.insightlab.graphast.model.components.NodeComponent {
        @Override
        public Node getNode() {
            return super.getNode();
        }
        
        @Override
        public void setNode(Node node) {
            super.setNode(node);
        }
    }
}
