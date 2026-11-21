package org.apache.jackrabbit.oak.plugins.migration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.jackrabbit.oak.api.CommitFailedException;
import org.apache.jackrabbit.oak.api.PropertyState;
import org.apache.jackrabbit.oak.api.Type;
import org.apache.jackrabbit.oak.plugins.memory.EmptyNodeState;
import org.apache.jackrabbit.oak.spi.commit.CommitHook;
import org.apache.jackrabbit.oak.spi.commit.CommitInfo;
import org.apache.jackrabbit.oak.spi.commit.EmptyHook;
import org.apache.jackrabbit.oak.spi.state.NodeBuilder;
import org.apache.jackrabbit.oak.spi.state.NodeState;
import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.junit.Test;

public class NodeStateCopier_ArrowTest {

    @Test
    public void testBuilderIncludesDefaultPath() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder();
        assertNotNull(builder);
    }

    @Test
    public void testBuilderIncludePaths() {
        Set<String> paths = new HashSet<>();
        paths.add("/test");
        NodeStateCopier.Builder builder = NodeStateCopier.builder().include(paths);
        assertNotNull(builder);
    }

    @Test
    public void testBuilderIncludeVarargs() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder().include("/test", "/test2");
        assertNotNull(builder);
    }

    @Test
    public void testBuilderExcludePaths() {
        Set<String> paths = new HashSet<>();
        paths.add("/test");
        NodeStateCopier.Builder builder = NodeStateCopier.builder().exclude(paths);
        assertNotNull(builder);
    }

    @Test
    public void testBuilderExcludeVarargs() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder().exclude("/test", "/test2");
        assertNotNull(builder);
    }

    @Test
    public void testBuilderSupportFragment() {
        Set<String> paths = new HashSet<>();
        paths.add("fragment");
        NodeStateCopier.Builder builder = NodeStateCopier.builder().supportFragment(paths);
        assertNotNull(builder);
    }

    @Test
    public void testBuilderSupportFragmentVarargs() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder().supportFragment("fragment", "fragment2");
        assertNotNull(builder);
    }

    @Test
    public void testBuilderExcludeFragments() {
        Set<String> fragments = new HashSet<>();
        fragments.add("fragment");
        NodeStateCopier.Builder builder = NodeStateCopier.builder().excludeFragments(fragments);
        assertNotNull(builder);
    }

    @Test
    public void testBuilderExcludeFragmentsVarargs() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder().excludeFragments("fragment", "fragment2");
        assertNotNull(builder);
    }

    @Test
    public void testBuilderMergePaths() {
        Set<String> paths = new HashSet<>();
        paths.add("/test");
        NodeStateCopier.Builder builder = NodeStateCopier.builder().merge(paths);
        assertNotNull(builder);
    }

    @Test
    public void testBuilderMergeVarargs() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder().merge("/test", "/test2");
        assertNotNull(builder);
    }

    @Test
    public void testBuilderWithReferenceableFrozenNodes() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder().withReferenceableFrozenNodes(false);
        assertNotNull(builder);
    }

    @Test
    public void testBuilderPreserve() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder().preserve(true);
        assertNotNull(builder);
    }

    @Test
    public void testBuilderWithNodeConsumer() {
        Consumer<String> consumer = path -> {};
        NodeStateCopier.Builder builder = NodeStateCopier.builder().withNodeConsumer(consumer);
        assertNotNull(builder);
    }

    @Test
    public void testCopyNodeStore() throws CommitFailedException {
        NodeStore source = mock(NodeStore.class);
        NodeStore target = mock(NodeStore.class);
        NodeState root = mock(NodeState.class);
        NodeBuilder builder = mock(NodeBuilder.class);
        when(source.getRoot()).thenReturn(root);
        when(target.getRoot()).thenReturn(root);
        when(root.builder()).thenReturn(builder);
        when(target.merge(any(NodeBuilder.class), any(CommitHook.class), any(CommitInfo.class))).thenReturn(true);
        boolean result = NodeStateCopier.copyNodeStore(source, target);
        assertTrue(result);
    }

    @Test
    public void testCopyPropertiesNoChanges() {
        NodeState source = mock(NodeState.class);
        NodeBuilder target = mock(NodeBuilder.class);
        when(source.getProperties()).thenReturn(new HashSet<>());
        when(target.getProperties()).thenReturn(new HashSet<>());
        boolean result = NodeStateCopier.copyProperties(source, target);
        assertFalse(result);
    }

    @Test
    public void testCopyPropertiesWithChanges() {
        NodeState source = mock(NodeState.class);
        NodeBuilder target = mock(NodeBuilder.class);
        PropertyState property = mock(PropertyState.class);
        when(property.getName()).thenReturn("prop");
        when(source.getProperties()).thenReturn(new HashSet<PropertyState>() {{ add(property); }});
        when(target.getProperty("prop")).thenReturn(property);
        when(property.equals(property)).thenReturn(false);
        boolean result = NodeStateCopier.copyProperties(source, target);
        assertTrue(result);
    }

    @Test
    public void testIsMergeTrue() {
        Set<String> mergePaths = new HashSet<>();
        mergePaths.add("/test");
        boolean result = NodeStateCopier.isMerge("/test/sub", mergePaths);
        assertTrue(result);
    }

    @Test
    public void testIsMergeFalse() {
        Set<String> mergePaths = new HashSet<>();
        mergePaths.add("/test");
        boolean result = NodeStateCopier.isMerge("/other", mergePaths);
        assertFalse(result);
    }

    @Test
    public void testGetValuesEmpty() {
        NodeState nodeState = mock(NodeState.class);
        Set<String> result = NodeStateCopier.getValues(nodeState, "prop");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testIsVersionPropertyEmptyTrue() {
        NodeState source = mock(NodeState.class);
        PropertyState property = mock(PropertyState.class);
        when(property.getName()).thenReturn("jcr:uuid");
        boolean result = NodeStateCopier.isVersionPropertyEmpty(source, property, true, "/test");
        assertTrue(result);
    }

    @Test
    public void testIsVersionPropertyEmptyFalse() {
        NodeState source = mock(NodeState.class);
        PropertyState property = mock(PropertyState.class);
        when(property.getName()).thenReturn("jcr:uuid");
        when(source.hasProperty("jcr:uuid")).thenReturn(true);
        when(source.getProperty("jcr:uuid")).thenReturn(property);
        when(property.getValue(Type.STRINGS)).thenReturn(new HashSet<String>() {{ add("value"); }});
        boolean result = NodeStateCopier.isVersionPropertyEmpty(source, property, true, "/test");
        assertFalse(result);
    }

    @Test
    public void testCopyMissingAncestors() {
        NodeState sourceRoot = mock(NodeState.class);
        NodeBuilder targetRoot = mock(NodeBuilder.class);
        boolean result = NodeStateCopier.copyMissingAncestors(sourceRoot, targetRoot, "/test/path");
        assertFalse(result);
    }

    @Test
    public void testGetChildNodeBuilder() {
        NodeBuilder root = mock(NodeBuilder.class);
        NodeBuilder result = NodeStateCopier.getChildNodeBuilder(root, "/test/path");
        assertNotNull(result);
    }
}
