package org.apache.jackrabbit.oak.plugins.migration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.jackrabbit.oak.api.PropertyState;
import org.apache.jackrabbit.oak.api.Type;
import org.apache.jackrabbit.oak.plugins.memory.EmptyNodeState;
import org.apache.jackrabbit.oak.spi.state.NodeBuilder;
import org.apache.jackrabbit.oak.spi.state.NodeState;
import org.apache.jackrabbit.oak.spi.state.NodeStateUtils;
import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.junit.Test;

public class NodeStateCopier_ArrowTest {

    @Test
    public void testBuilderIncludesDefaultPath() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder();
        assertNotNull(builder);
    }

    @Test
    public void testCopyPropertiesWithNoChanges() {
        NodeState source = mock(NodeState.class);
        NodeBuilder target = mock(NodeBuilder.class);
        
        when(source.getProperties()).thenReturn(new HashSet<PropertyState>());
        when(target.getProperties()).thenReturn(new HashSet<PropertyState>());
        
        boolean result = NodeStateCopier.copyProperties(source, target);
        assertFalse(result);
    }

    @Test
    public void testCopyPropertiesWithNewProperty() {
        NodeState source = mock(NodeState.class);
        NodeBuilder target = mock(NodeBuilder.class);
        PropertyState property = mock(PropertyState.class);
        
        when(source.getProperties()).thenReturn(new HashSet<PropertyState>() {{ add(property); }});
        when(target.getProperties()).thenReturn(new HashSet<PropertyState>());
        when(property.getName()).thenReturn("prop1");
        when(target.getProperty("prop1")).thenReturn(null);
        
        boolean result = NodeStateCopier.copyProperties(source, target);
        assertTrue(result);
    }

    @Test
    public void testCopyPropertiesWithChangedProperty() {
        NodeState source = mock(NodeState.class);
        NodeBuilder target = mock(NodeBuilder.class);
        PropertyState sourceProperty = mock(PropertyState.class);
        PropertyState targetProperty = mock(PropertyState.class);
        
        when(source.getProperties()).thenReturn(new HashSet<PropertyState>() {{ add(sourceProperty); }});
        when(target.getProperties()).thenReturn(new HashSet<PropertyState>() {{ add(targetProperty); }});
        when(sourceProperty.getName()).thenReturn("prop1");
        when(target.getProperty("prop1")).thenReturn(targetProperty);
        when(sourceProperty.equals(targetProperty)).thenReturn(false);
        
        boolean result = NodeStateCopier.copyProperties(source, target);
        assertTrue(result);
    }

    @Test
    public void testIsMergeTrue() {
        Set<String> mergePaths = new HashSet<String>() {{ add("/content"); }};
        boolean result = NodeStateCopier.isMerge("/content/test", mergePaths);
        assertTrue(result);
    }

    @Test
    public void testIsMergeFalse() {
        Set<String> mergePaths = new HashSet<String>() {{ add("/content"); }};
        boolean result = NodeStateCopier.isMerge("/other/test", mergePaths);
        assertFalse(result);
    }

    @Test
    public void testGetValuesWithEmptyProperty() {
        NodeState nodeState = mock(NodeState.class);
        when(nodeState.hasProperty("test")).thenReturn(true);
        when(nodeState.getProperty("test")).thenReturn(mock(PropertyState.class));
        
        Set<String> result = NodeStateCopier.getValues(nodeState, "test");
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetValuesWithNonEmptyProperty() {
        NodeState nodeState = mock(NodeState.class);
        PropertyState propertyState = mock(PropertyState.class);
        when(nodeState.hasProperty("test")).thenReturn(true);
        when(nodeState.getProperty("test")).thenReturn(propertyState);
        when(propertyState.getValue(Type.STRINGS)).thenReturn(new String[]{"value1", "value2"});
        
        Set<String> result = NodeStateCopier.getValues(nodeState, "test");
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testCopyNodeStore() throws Exception {
        NodeStore source = mock(NodeStore.class);
        NodeStore target = mock(NodeStore.class);
        NodeState rootNode = mock(NodeState.class);
        NodeBuilder targetRoot = mock(NodeBuilder.class);
        
        when(source.getRoot()).thenReturn(rootNode);
        when(target.getRoot()).thenReturn(rootNode);
        when(targetRoot.builder()).thenReturn(targetRoot);
        
        boolean result = NodeStateCopier.copyNodeStore(source, target);
        assertFalse(result);
    }

    @Test
    public void testBuilderIncludePaths() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder();
        Set<String> paths = new HashSet<String>() {{ add("/content"); }};
        builder.include(paths);
        
        assertNotNull(builder);
    }

    @Test
    public void testBuilderExcludePaths() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder();
        Set<String> paths = new HashSet<String>() {{ add("/content"); }};
        builder.exclude(paths);
        
        assertNotNull(builder);
    }

    @Test
    public void testBuilderMergePaths() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder();
        Set<String> paths = new HashSet<String>() {{ add("/content"); }};
        builder.merge(paths);
        
        assertNotNull(builder);
    }

    @Test
    public void testBuilderPreserve() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder();
        builder.preserve(true);
        
        assertNotNull(builder);
    }

    @Test
    public void testBuilderWithReferenceableFrozenNodes() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder();
        builder.withReferenceableFrozenNodes(false);
        
        assertNotNull(builder);
    }

    @Test
    public void testBuilderWithNodeConsumer() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder();
        Consumer<String> consumer = mock(Consumer.class);
        builder.withNodeConsumer(consumer);
        
        assertNotNull(builder);
    }

    @Test
    public void testCopyNodeStateWithEmptySource() {
        NodeState source = EmptyNodeState.MISSING_NODE;
        NodeBuilder target = mock(NodeBuilder.class);
        
        boolean result = NodeStateCopier.copyNodeState(source, target, "/", new HashSet<String>(), false, null);
        assertFalse(result);
    }

    @Test
    public void testCopyMissingAncestors() {
        NodeState sourceRoot = mock(NodeState.class);
        NodeBuilder targetRoot = mock(NodeBuilder.class);
        String path = "/content/test";
        
        boolean result = NodeStateCopier.copyMissingAncestors(sourceRoot, targetRoot, path);
        assertFalse(result);
    }

    @Test
    public void testGetChildNodeBuilder() {
        NodeBuilder root = mock(NodeBuilder.class);
        String path = "/content/test";
        
        NodeBuilder result = NodeStateCopier.getChildNodeBuilder(root, path);
        assertNotNull(result);
    }
}
