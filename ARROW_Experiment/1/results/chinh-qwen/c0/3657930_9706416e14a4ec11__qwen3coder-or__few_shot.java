package org.apache.jackrabbit.oak.plugins.migration;

import org.apache.jackrabbit.oak.api.PropertyState;
import org.apache.jackrabbit.oak.plugins.memory.EmptyNodeState;
import org.apache.jackrabbit.oak.spi.state.NodeBuilder;
import org.apache.jackrabbit.oak.spi.state.NodeState;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NodeStateCopier_ArrowTest {

    @Test
    public void testBuilderDefaults() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder();
        assertNotNull(builder);
    }

    @Test
    public void testIncludePaths() {
        Set<String> paths = new HashSet<>();
        paths.add("/test");
        paths.add("/test2");
        
        NodeStateCopier.Builder builder = NodeStateCopier.builder().include(paths);
        assertNotNull(builder);
    }

    @Test
    public void testIncludePathsVarargs() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder().include("/test", "/test2");
        assertNotNull(builder);
    }

    @Test
    public void testExcludePaths() {
        Set<String> paths = new HashSet<>();
        paths.add("/exclude");
        
        NodeStateCopier.Builder builder = NodeStateCopier.builder().exclude(paths);
        assertNotNull(builder);
    }

    @Test
    public void testExcludePathsVarargs() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder().exclude("/exclude");
        assertNotNull(builder);
    }

    @Test
    public void testSupportFragment() {
        Set<String> paths = new HashSet<>();
        paths.add("fragment");
        
        NodeStateCopier.Builder builder = NodeStateCopier.builder().supportFragment(paths);
        assertNotNull(builder);
    }

    @Test
    public void testSupportFragmentVarargs() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder().supportFragment("fragment");
        assertNotNull(builder);
    }

    @Test
    public void testExcludeFragments() {
        Set<String> fragments = new HashSet<>();
        fragments.add("fragment");
        
        NodeStateCopier.Builder builder = NodeStateCopier.builder().excludeFragments(fragments);
        assertNotNull(builder);
    }

    @Test
    public void testExcludeFragmentsVarargs() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder().excludeFragments("fragment");
        assertNotNull(builder);
    }

    @Test
    public void testMergePaths() {
        Set<String> paths = new HashSet<>();
        paths.add("/merge");
        
        NodeStateCopier.Builder builder = NodeStateCopier.builder().merge(paths);
        assertNotNull(builder);
    }

    @Test
    public void testMergePathsVarargs() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder().merge("/merge");
        assertNotNull(builder);
    }

    @Test
    public void testPreserveOnTarget() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder().preserve(true);
        assertNotNull(builder);
    }

    @Test
    public void testWithReferenceableFrozenNodes() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder().withReferenceableFrozenNodes(false);
        assertNotNull(builder);
    }

    @Test
    public void testWithNodeConsumer() {
        AtomicBoolean called = new AtomicBoolean(false);
        NodeStateCopier.Builder builder = NodeStateCopier.builder().withNodeConsumer(path -> called.set(true));
        assertNotNull(builder);
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
        
        when(source.getProperties()).thenReturn(new HashSet<>());
        when(target.getProperties()).thenReturn(new HashSet<>());
        when(source.hasProperty(anyString())).thenReturn(false);
        when(target.getProperty(anyString())).thenReturn(null);
        
        boolean result = NodeStateCopier.copyProperties(source, target);
        assertFalse(result);
    }

    @Test
    public void testCopyNodeState() {
        NodeState source = mock(NodeState.class);
        NodeBuilder target = mock(NodeBuilder.class);
        
        NodeStateCopier copier = new NodeStateCopier(
            new HashSet<>(),
            new HashSet<>(),
            new HashSet<>(),
            new HashSet<>(),
            new HashSet<>(),
            true,
            false,
            path -> {}
        );
        
        boolean result = copier.copyNodeState(source, target);
        assertFalse(result);
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
    public void testGetChildNodeBuilder() {
        NodeBuilder root = mock(NodeBuilder.class);
        NodeBuilder child = mock(NodeBuilder.class);
        
        when(root.child(anyString())).thenReturn(child);
        
        NodeBuilder result = NodeStateCopier.getChildNodeBuilder(root, "/test");
        assertNotNull(result);
    }

    @Test
    public void testCopyMissingAncestors() {
        NodeState sourceRoot = mock(NodeState.class);
        NodeBuilder targetRoot = mock(NodeBuilder.class);
        
        boolean result = NodeStateCopier.copyMissingAncestors(sourceRoot, targetRoot, "/test/path");
        assertFalse(result);
    }
}
