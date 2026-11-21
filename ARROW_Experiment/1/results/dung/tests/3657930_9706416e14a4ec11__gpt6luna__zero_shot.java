package org.apache.jackrabbit.oak.plugins.migration;

import org.apache.jackrabbit.oak.plugins.memory.EmptyNodeState;
import org.apache.jackrabbit.oak.plugins.memory.PropertyStates;
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
    public void copiesPropertiesAndChildren() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.setProperty(PropertyStates.createProperty("title", "source"));
        sourceBuilder.child("child").setProperty(PropertyStates.createProperty("value", "copied"));
        NodeState source = sourceBuilder.getNodeState();

        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        assertTrue(NodeStateCopier.builder().copy(source, target));
        assertEquals("source", target.getProperty("title").getValue(org.apache.jackrabbit.oak.api.Type.STRING));
        assertEquals("copied", target.getChildNode("child").getProperty("value")
                .getValue(org.apache.jackrabbit.oak.api.Type.STRING));
    }

    @Test
    public void identicalTreesDoNotReportChanges() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.setProperty(PropertyStates.createProperty("title", "same"));
        NodeState source = sourceBuilder.getNodeState();

        NodeBuilder target = source.builder();
        assertFalse(NodeStateCopier.builder().copy(source, target));
    }

    @Test
    public void removesTargetContentMissingFromSource() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.setProperty(PropertyStates.createProperty("kept", "yes"));
        NodeState source = sourceBuilder.getNodeState();

        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        target.setProperty(PropertyStates.createProperty("removed", "no"));
        target.child("old");

        assertTrue(NodeStateCopier.builder().copy(source, target));
        assertFalse(target.hasProperty("removed"));
        assertFalse(target.hasChildNode("old"));
        assertEquals("yes", target.getProperty("kept").getValue(org.apache.jackrabbit.oak.api.Type.STRING));
    }

    @Test
    public void preserveKeepsTargetOnlyContent() {
        NodeState source = EmptyNodeState.EMPTY_NODE;

        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        target.setProperty(PropertyStates.createProperty("existing", "value"));
        target.child("existingChild");

        assertFalse(NodeStateCopier.builder().preserve(true).copy(source, target));
        assertTrue(target.hasProperty("existing"));
        assertTrue(target.hasChildNode("existingChild"));
    }

    @Test
    public void mergeKeepsTargetOnlyDescendants() {
        NodeState source = EmptyNodeState.EMPTY_NODE;

        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        target.child("merged").child("targetOnly");

        assertFalse(NodeStateCopier.builder().merge("/merged").copy(source, target));
        assertTrue(target.hasChildNode("merged"));
        assertTrue(target.getChildNode("merged").hasChildNode("targetOnly"));
    }

    @Test
    public void includeCopiesOnlySelectedPathAndItsAncestors() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.child("a").child("selected")
                .setProperty(PropertyStates.createProperty("value", "included"));
        sourceBuilder.child("other").setProperty(PropertyStates.createProperty("value", "excluded"));
        NodeState source = sourceBuilder.getNodeState();

        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        assertTrue(NodeStateCopier.builder().include("/a/selected").copy(source, target));
        assertTrue(target.hasChildNode("a"));
        assertTrue(target.getChildNode("a").hasChildNode("selected"));
        assertEquals("included", target.getChildNode("a").getChildNode("selected")
                .getProperty("value").getValue(org.apache.jackrabbit.oak.api.Type.STRING));
        assertFalse(target.hasChildNode("other"));
    }

    @Test
    public void excludeOmitsSelectedSubtree() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.child("included").setProperty(PropertyStates.createProperty("value", "yes"));
        sourceBuilder.child("excluded").setProperty(PropertyStates.createProperty("value", "no"));
        NodeState source = sourceBuilder.getNodeState();

        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        assertTrue(NodeStateCopier.builder().exclude("/excluded").copy(source, target));
        assertTrue(target.hasChildNode("included"));
        assertFalse(target.hasChildNode("excluded"));
    }

    @Test
    public void reportsAddedNodePathsToConsumer() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.child("parent").child("child");
        NodeState source = sourceBuilder.getNodeState();

        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        List<String> addedPaths = new ArrayList<String>();

        assertTrue(NodeStateCopier.builder().withNodeConsumer(addedPaths::add).copy(source, target));
        assertEquals(Arrays.asList("/parent"), addedPaths);
    }

    @Test
    public void copyPropertiesRemovesMissingProperties() {
        NodeBuilder sourceBuilder = EmptyNodeState.EMPTY_NODE.builder();
        sourceBuilder.setProperty(PropertyStates.createProperty("new", "value"));
        NodeState source = sourceBuilder.getNodeState();

        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        target.setProperty(PropertyStates.createProperty("old", "value"));

        assertTrue(NodeStateCopier.copyProperties(source, target));
        assertFalse(target.hasProperty("old"));
        assertEquals("value", target.getProperty("new").getValue(org.apache.jackrabbit.oak.api.Type.STRING));
    }

    @Test
    public void copyPropertiesCanPreserveTargetProperties() {
        NodeState source = EmptyNodeState.EMPTY_NODE;

        NodeBuilder target = EmptyNodeState.EMPTY_NODE.builder();
        target.setProperty(PropertyStates.createProperty("existing", "value"));

        assertFalse(NodeStateCopier.copyProperties(source, target, true, "/"));
        assertTrue(target.hasProperty("existing"));
    }
}
