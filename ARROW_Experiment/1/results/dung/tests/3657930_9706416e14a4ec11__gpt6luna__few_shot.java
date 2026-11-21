package org.apache.jackrabbit.oak.plugins.migration;

import org.apache.jackrabbit.oak.plugins.memory.EmptyNodeState;
import org.apache.jackrabbit.oak.spi.state.NodeBuilder;
import org.apache.jackrabbit.oak.spi.state.NodeState;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NodeStateCopier_ArrowTest {

    @Test
    public void copiesSourceContentIntoEmptyTarget() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.child("child").setProperty("value", "source");
        NodeState source = sourceBuilder.getNodeState();
        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();

        boolean changed = NodeStateCopier.builder().copy(source, target);

        assertTrue(changed);
        assertTrue(target.hasChildNode("child"));
        assertEquals("source", target.getChildNode("child").getProperty("value").getValue(org.apache.jackrabbit.oak.api.Type.STRING));
    }

    @Test
    public void returnsFalseWhenSourceAndTargetAreEqual() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.setProperty("value", "same");
        NodeState source = sourceBuilder.getNodeState();
        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        target.setProperty("value", "same");

        boolean changed = NodeStateCopier.builder().copy(source, target);

        assertFalse(changed);
    }

    @Test
    public void removesTargetContentMissingFromSource() {
        NodeState source = EmptyNodeState.EMPTY_NODE;
        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        target.setProperty("obsolete", "value");
        target.child("obsoleteChild");

        boolean changed = NodeStateCopier.builder().copy(source, target);

        assertTrue(changed);
        assertFalse(target.hasProperty("obsolete"));
        assertFalse(target.hasChildNode("obsoleteChild"));
    }

    @Test
    public void mergePathPreservesTargetOnlyChildren() {
        NodeState source = EmptyNodeState.EMPTY_NODE;
        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        target.child("merged").child("targetOnly");

        boolean changed = NodeStateCopier.builder().merge("/merged").copy(source, target);

        assertFalse(changed);
        assertTrue(target.hasChildNode("merged"));
        assertTrue(target.getChildNode("merged").hasChildNode("targetOnly"));
    }

    @Test
    public void preserveKeepsTargetPropertiesAndChildren() {
        NodeState source = EmptyNodeState.EMPTY_NODE;
        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        target.setProperty("targetOnly", "value");
        target.child("targetOnlyChild");

        boolean changed = NodeStateCopier.builder().preserve(true).copy(source, target);

        assertFalse(changed);
        assertTrue(target.hasProperty("targetOnly"));
        assertTrue(target.hasChildNode("targetOnlyChild"));
    }

    @Test
    public void includeCopiesOnlySelectedPath() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.child("selected").setProperty("value", "included");
        sourceBuilder.child("other").setProperty("value", "excluded");
        NodeState source = sourceBuilder.getNodeState();
        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();

        boolean changed = NodeStateCopier.builder().include("/selected").copy(source, target);

        assertTrue(changed);
        assertTrue(target.hasChildNode("selected"));
        assertEquals("included", target.getChildNode("selected").getProperty("value").getValue(org.apache.jackrabbit.oak.api.Type.STRING));
        assertFalse(target.hasChildNode("other"));
    }

    @Test
    public void excludeRemovesExcludedPath() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.child("excluded").setProperty("value", "source");
        NodeState source = sourceBuilder.getNodeState();
        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        target.child("excluded").setProperty("value", "target");

        boolean changed = NodeStateCopier.builder().exclude("/excluded").copy(source, target);

        assertTrue(changed);
        assertFalse(target.hasChildNode("excluded"));
    }

    @Test
    public void reportsAddedNodePathsToConsumer() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.child("parent").child("child");
        NodeState source = sourceBuilder.getNodeState();
        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        List<String> addedPaths = new ArrayList<>();

        boolean changed = NodeStateCopier.builder().withNodeConsumer(addedPaths::add).copy(source, target);

        assertTrue(changed);
        assertEquals(Arrays.asList("/parent"), addedPaths);
    }

    @Test
    public void copyPropertiesRemovesPropertiesAbsentFromSource() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.setProperty("kept", "source");
        NodeState source = sourceBuilder.getNodeState();
        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        target.setProperty("kept", "target");
        target.setProperty("removed", "value");

        boolean changed = NodeStateCopier.copyProperties(source, target);

        assertTrue(changed);
        assertEquals("source", target.getProperty("kept").getValue(org.apache.jackrabbit.oak.api.Type.STRING));
        assertFalse(target.hasProperty("removed"));
    }

    @Test
    public void copyPropertiesPreservesTargetPropertiesWhenRequested() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.setProperty("sourceOnly", "source");
        NodeState source = sourceBuilder.getNodeState();
        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        target.setProperty("targetOnly", "target");

        boolean changed = NodeStateCopier.copyProperties(source, target, true, "/");

        assertTrue(changed);
        assertTrue(target.hasProperty("targetOnly"));
        assertEquals("source", target.getProperty("sourceOnly").getValue(org.apache.jackrabbit.oak.api.Type.STRING));
    }
}
