package br.ufc.insightlab.graphast.model;

import br.ufc.insightlab.graphast.model.components.NodeComponent;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Set;

public class Node_ArrowTest {

    @Test
    public void testNodeDefaultConstructor() {
        Node node = new Node();
        assertEquals(0L, node.getId());
    }

    @Test
    public void testNodeIdConstructor() {
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
    public void testAddComponentWithoutExistingMap() {
        Node node = new Node(1L);
        NodeComponent component = new NodeComponent() {
            @Override
            public void setNode(Node node) {
                // Empty implementation
            }
        };
        
        node.addComponent(component);
        
        NodeComponent retrieved = node.getComponent(NodeComponent.class);
        assertNotNull(retrieved);
        assertThat(retrieved, is(component));
    }

    @Test
    public void testAddComponentWithExistingMap() {
        Node node = new Node(1L);
        NodeComponent component1 = new NodeComponent() {
            @Override
            public void setNode(Node node) {
                // Empty implementation
            }
        };
        NodeComponent component2 = new NodeComponent() {
            @Override
            public void setNode(Node node) {
                // Empty implementation
            }
        };
        
        node.addComponent(component1);
        node.addComponent(component2);
        
        NodeComponent retrieved1 = node.getComponent(NodeComponent.class);
        assertNotNull(retrieved1);
        assertThat(retrieved1, is(component2));
    }

    @Test
    public void testGetComponentWhenNoComponentsExist() {
        Node node = new Node(1L);
        NodeComponent component = node.getComponent(NodeComponent.class);
        assertNull(component);
    }

    @Test
    public void testGetComponentWhenComponentExists() {
        Node node = new Node(1L);
        NodeComponent component = new NodeComponent() {
            @Override
            public void setNode(Node node) {
                // Empty implementation
            }
        };
        node.addComponent(component);
        
        NodeComponent retrieved = node.getComponent(NodeComponent.class);
        assertNotNull(retrieved);
        assertThat(retrieved, is(component));
    }

    @Test
    public void testHasComponentWhenNoComponentsExist() {
        Node node = new Node(1L);
        assertFalse(node.hasComponent(NodeComponent.class));
    }

    @Test
    public void testHasComponentWhenComponentExists() {
        Node node = new Node(1L);
        NodeComponent component = new NodeComponent() {
            @Override
            public void setNode(Node node) {
                // Empty implementation
            }
        };
        node.addComponent(component);
        
        assertTrue(node.hasComponent(NodeComponent.class));
    }

    @Test
    public void testGetAllComponentClassesWhenNoComponentsExist() {
        Node node = new Node(1L);
        Set<Class<? extends NodeComponent>> classes = node.getAllComponentClasses();
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
    }

    @Test
    public void testGetAllComponentClassesWhenComponentsExist() {
        Node node = new Node(1L);
        NodeComponent component = new NodeComponent() {
            @Override
            public void setNode(Node node) {
                // Empty implementation
            }
        };
        node.addComponent(component);
        
        Set<Class<? extends NodeComponent>> classes = node.getAllComponentClasses();
        assertNotNull(classes);
        assertEquals(1, classes.size());
        assertTrue(classes.contains(NodeComponent.class));
    }

    @Test
    public void testEqualsWhenObjectsAreNotNodes() {
        Node node = new Node(1L);
        assertFalse(node.equals("not a node"));
    }

    @Test
    public void testEqualsWhenNodesHaveSameId() {
        Node node1 = new Node(1L);
        Node node2 = new Node(1L);
        assertTrue(node1.equals(node2));
    }

    @Test
    public void testEqualsWhenNodesHaveDifferentIds() {
        Node node1 = new Node(1L);
        Node node2 = new Node(2L);
        assertFalse(node1.equals(node2));
    }

    @Test
    public void testHashCode() {
        Node node = new Node(123L);
        int expected = Long.valueOf(123L).hashCode();
        assertEquals(expected, node.hashCode());
    }
}
