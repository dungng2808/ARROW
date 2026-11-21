package org.apache.jackrabbit.oak.plugins.migration;

import org.apache.jackrabbit.oak.plugins.memory.EmptyNodeState;
import org.apache.jackrabbit.oak.plugins.memory.MemoryNodeBuilder;
import org.apache.jackrabbit.oak.plugins.memory.PropertyStates;
import org.apache.jackrabbit.oak.spi.state.NodeBuilder;
import org.apache.jackrabbit.oak.spi.state.NodeState;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class NodeStateCopier_ArrowTest {

    @Test
    public void copyPropertiesAddsAndUpdatesProperties() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.setProperty("added", "value");
        sourceBuilder.setProperty("changed", "new");
        NodeState source = sourceBuilder.getNodeState();

        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        target.setProperty("changed", "old");

        assertTrue(NodeStateCopier.copyProperties(source, target));
        assertEquals("value", target.getProperty("added").getValue(org.apache.jackrabbit.oak.api.Type.STRING));
        assertEquals("new", target.getProperty("changed").getValue(org.apache.jackrabbit.oak.api.Type.STRING));
    }

    @Test
    public void copyPropertiesRemovesPropertiesMissingFromSource() {
        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        target.setProperty("obsolete", "value");

        assertTrue(NodeStateCopier.copyProperties(EmptyNodeState.EMPTY_NODE, target));
        assertFalse(target.hasProperty("obsolete"));
    }

    @Test
    public void copyPropertiesReturnsFalseWhenPropertiesMatch() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.setProperty("same", "value");
        NodeState source = sourceBuilder.getNodeState();

        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        target.setProperty("same", "value");

        assertFalse(NodeStateCopier.copyProperties(source, target));
    }

    @Test
    public void copyPropertiesPreservingTargetKeepsExtraProperties() {
        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        target.setProperty("targetOnly", "kept");

        assertFalse(NodeStateCopier.copyProperties(EmptyNodeState.EMPTY_NODE, target, true, "/"));
        assertEquals("kept", target.getProperty("targetOnly").getValue(org.apache.jackrabbit.oak.api.Type.STRING));
    }

    @Test
    public void copyPropertiesPreservingTargetSkipsEmptyVersionProperties() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.setProperty("jcr:uuid", "");
        NodeState source = sourceBuilder.getNodeState();

        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        assertFalse(NodeStateCopier.copyProperties(source, target, true, "/"));
        assertFalse(target.hasProperty("jcr:uuid"));
    }

    @Test
    public void copyPropertiesPreservingTargetCopiesNonEmptyVersionProperties() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.setProperty("jcr:uuid", "identifier");
        NodeState source = sourceBuilder.getNodeState();

        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        assertTrue(NodeStateCopier.copyProperties(source, target, true, "/"));
        assertEquals("identifier", target.getProperty("jcr:uuid").getValue(org.apache.jackrabbit.oak.api.Type.STRING));
    }

    @Test
    public void builderCopyCopiesTreeAndReportsAddedNodePaths() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.setProperty("rootProperty", "root");
        sourceBuilder.child("child").setProperty("childProperty", "child");
        NodeState source = sourceBuilder.getNodeState();

        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        List<String> addedPaths = new ArrayList<String>();

        boolean changed = NodeStateCopier.builder()
                .withNodeConsumer(addedPaths::add)
                .copy(source, target);

        assertTrue(changed);
        assertEquals("root", target.getProperty("rootProperty").getValue(org.apache.jackrabbit.oak.api.Type.STRING));
        assertEquals("child", target.getChildNode("child").getProperty("childProperty")
                .getValue(org.apache.jackrabbit.oak.api.Type.STRING));
        assertEquals(Collections.singletonList("/child"), addedPaths);
    }

    @Test
    public void builderCopyReturnsFalseForIdenticalTrees() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.setProperty("property", "value");
        sourceBuilder.child("child").setProperty("nested", "value");
        NodeState source = sourceBuilder.getNodeState();

        NodeBuilder target = source.builder();

        assertFalse(NodeStateCopier.builder().copy(source, target));
    }

    @Test
    public void builderCopyHonorsMergePaths() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.child("merged").setProperty("fromSource", "yes");
        NodeState source = sourceBuilder.getNodeState();

        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        target.child("merged").setProperty("targetOnly", "kept");
        target.child("removed").setProperty("value", "old");

        assertTrue(NodeStateCopier.builder().merge("/merged").copy(source, target));
        assertTrue(target.getChildNode("merged").hasProperty("targetOnly"));
        assertFalse(target.hasChildNode("removed"));
    }

    @Test
    public void builderCopyPreservesTargetWhenConfigured() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.child("present").setProperty("source", "yes");
        NodeState source = sourceBuilder.getNodeState();

        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        target.child("present").setProperty("target", "kept");
        target.child("targetOnly").setProperty("value", "kept");

        assertTrue(NodeStateCopier.builder().preserve(true).copy(source, target));
        assertTrue(target.getChildNode("present").hasProperty("target"));
        assertTrue(target.hasChildNode("targetOnly"));
    }

    @Test
    public void builderCopyHonorsIncludePaths() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.child("included").setProperty("value", "copied");
        sourceBuilder.child("excluded").setProperty("value", "not copied");
        NodeState source = sourceBuilder.getNodeState();

        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        assertTrue(NodeStateCopier.builder().include("/included").copy(source, target));
        assertEquals("copied", target.getChildNode("included").getProperty("value")
                .getValue(org.apache.jackrabbit.oak.api.Type.STRING));
        assertFalse(target.hasChildNode("excluded"));
    }

    @Test
    public void builderCopyHonorsExcludePaths() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.child("included").setProperty("value", "copied");
        sourceBuilder.child("excluded").setProperty("value", "not copied");
        NodeState source = sourceBuilder.getNodeState();

        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        assertTrue(NodeStateCopier.builder().exclude("/excluded").copy(source, target));
        assertTrue(target.hasChildNode("included"));
        assertFalse(target.hasChildNode("excluded"));
    }

    @Test
    public void builderIncludeEmptySetRetainsDefaultRootInclude() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.setProperty("value", "copied");
        NodeState source = sourceBuilder.getNodeState();

        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        assertTrue(NodeStateCopier.builder().include(Collections.<String>emptySet()).copy(source, target));
        assertEquals("copied", target.getProperty("value").getValue(org.apache.jackrabbit.oak.api.Type.STRING));
    }

    @Test
    public void builderMethodsReturnSameBuilder() {
        NodeStateCopier.Builder builder = NodeStateCopier.builder();
        assertSame(builder, builder.include("/").exclude("/none").merge("/merged")
                .supportFragment("/fragment").excludeFragments("fragment")
                .preserve(false).withReferenceableFrozenNodes(true));
    }
}
