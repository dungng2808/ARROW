package org.apache.nemo.common.ir;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import org.apache.nemo.common.Pair;
import org.apache.nemo.common.dag.DAG;
import org.apache.nemo.common.dag.DAGBuilder;
import org.apache.nemo.common.ir.edge.IREdge;
import org.apache.nemo.common.ir.edge.executionproperty.*;
import org.apache.nemo.common.ir.vertex.*;
import org.apache.nemo.common.ir.vertex.executionproperty.MessageIdVertexProperty;
import org.apache.nemo.common.ir.vertex.executionproperty.ParallelismProperty;
import org.apache.nemo.common.ir.vertex.utility.TaskSizeSplitterVertex;
import org.apache.nemo.common.ir.vertex.utility.runtimepass.MessageAggregatorVertex;
import org.apache.nemo.common.ir.vertex.utility.runtimepass.MessageGeneratorVertex;
import org.apache.nemo.common.ir.vertex.utility.runtimepass.SignalVertex;
import org.apache.nemo.common.ir.vertex.utility.SamplingVertex;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class IRDAG_ArrowTest {

  @Test
  public void testConstructor() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    assertNotNull(irDAG);
    assertEquals(dag, irDAG.getVertices());
    assertEquals(dag, irDAG.getEdges());
  }

  @Test
  public void testIrDAGSummary() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    builder.connectVertices(new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map));
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    String summary = irDAG.irDAGSummary();
    assertNotNull(summary);
    assertTrue(summary.startsWith("rv"));
  }

  @Test
  public void testGetInputSize() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    source.setEstimatedSizeBytes(1024L);
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    builder.connectVertices(new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map));
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    Long inputSize = irDAG.getInputSize();
    assertEquals(Long.valueOf(1024L), inputSize);
  }

  @Test
  public void testRecordAndGetExecutorInfo() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<Pair<Integer, ResourceSpecification>> executorInfo = new ArrayList<>();
    Pair<Integer, ResourceSpecification> pair = new Pair<>(1, ResourceSpecification.of(1024));
    executorInfo.add(pair);
    
    irDAG.recordExecutorInfo(executorInfo);
    List<Pair<Integer, ResourceSpecification>> retrievedInfo = irDAG.getExecutorInfo();
    
    assertEquals(1, retrievedInfo.size());
    assertEquals(Integer.valueOf(1), retrievedInfo.get(0).left());
  }

  @Test
  public void testAdvanceDAGSnapshot() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    boolean result = irDAG.advanceDAGSnapshot((oldDAG, newDAG) -> true);
    assertTrue(result);
  }

  @Test
  public void testCheckIntegrity() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    IRDAGChecker.CheckerResult result = irDAG.checkIntegrity();
    assertNotNull(result);
  }

  @Test
  public void testInsertRelayVertex() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    RelayVertex relay = new RelayVertex();
    irDAG.insert(relay, edge);
    
    assertNotNull(irDAG.getVertices());
    assertTrue(irDAG.getVertices().contains(relay));
  }

  @Test
  public void testInsertMessageVertices() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    MessageGeneratorVertex<String> messageGen = new MessageGeneratorVertex<>(s -> s);
    MessageAggregatorVertex<String> messageAgg = new MessageAggregatorVertex<>(s -> s);
    Set<IREdge> edgesToGetStats = Sets.newHashSet(edge);
    Set<IREdge> edgesToOptimize = Sets.newHashSet(edge);
    
    irDAG.insert(messageGen, messageAgg, 
                 EncoderProperty.of(BytesEncoderFactory.of()),
                 DecoderProperty.of(BytesDecoderFactory.of()),
                 edgesToGetStats, edgesToOptimize);
    
    assertNotNull(irDAG.getVertices());
    assertTrue(irDAG.getVertices().contains(messageGen));
    assertTrue(irDAG.getVertices().contains(messageAgg));
  }

  @Test
  public void testInsertSignalVertex() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    SignalVertex signal = new SignalVertex();
    irDAG.insert(signal, edge);
    
    assertNotNull(irDAG.getVertices());
    assertTrue(irDAG.getVertices().contains(signal));
  }

  @Test
  public void testInsertSamplingVertices() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    Set<SamplingVertex> samplingVertices = Sets.newHashSet(new SamplingVertex(new MapVertex<>("map2"), 0.5));
    Set<IRVertex> executeAfter = Sets.newHashSet(map);
    
    irDAG.insert(samplingVertices, executeAfter);
    
    assertNotNull(irDAG.getVertices());
    assertTrue(irDAG.getVertices().containsAll(samplingVertices));
  }

  @Test
  public void testInsertTaskSizeSplitterVertex() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    TaskSizeSplitterVertex splitter = new TaskSizeSplitterVertex();
    splitter.setOriginalVertices(Sets.newHashSet(map));
    
    irDAG.insert(splitter);
    
    assertNotNull(irDAG.getVertices());
    assertTrue(irDAG.getVertices().contains(splitter));
  }

  @Test
  public void testDeleteVertex() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    RelayVertex relay = new RelayVertex();
    irDAG.insert(relay, edge);
    
    int originalSize = irDAG.getVertices().size();
    irDAG.delete(relay);
    
    assertTrue(irDAG.getVertices().size() < originalSize);
  }

  @Test
  public void testGetVertexById() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    builder.addVertex(source);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    IRVertex vertex = irDAG.getVertexById("source");
    assertNotNull(vertex);
    assertEquals("source", vertex.getId());
  }

  @Test
  public void testGetEdgeById() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    IREdge retrievedEdge = irDAG.getEdgeById(edge.getId());
    assertNotNull(retrievedEdge);
    assertEquals(edge.getId(), retrievedEdge.getId());
  }

  @Test
  public void testGetRootVertices() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IRVertex> rootVertices = irDAG.getRootVertices();
    assertNotNull(rootVertices);
    assertEquals(1, rootVertices.size());
    assertEquals("source", rootVertices.get(0).getId());
  }

  @Test
  public void testGetIncomingEdgesOf() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IREdge> incomingEdges = irDAG.getIncomingEdgesOf(map);
    assertNotNull(incomingEdges);
    assertEquals(1, incomingEdges.size());
    assertEquals(edge.getId(), incomingEdges.get(0).getId());
  }

  @Test
  public void testGetOutgoingEdgesOf() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IREdge> outgoingEdges = irDAG.getOutgoingEdgesOf(source);
    assertNotNull(outgoingEdges);
    assertEquals(1, outgoingEdges.size());
    assertEquals(edge.getId(), outgoingEdges.get(0).getId());
  }

  @Test
  public void testAsJsonNode() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    ObjectNode jsonNode = irDAG.asJsonNode();
    assertNotNull(jsonNode);
  }

  @Test
  public void testFilterVertices() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IRVertex> filteredVertices = irDAG.filterVertices(v -> v instanceof SourceVertex);
    assertNotNull(filteredVertices);
    assertEquals(1, filteredVertices.size());
    assertTrue(filteredVertices.get(0) instanceof SourceVertex);
  }

  @Test
  public void testGetTopologicalSort() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IRVertex> topologicalSort = irDAG.getTopologicalSort();
    assertNotNull(topologicalSort);
    assertEquals(2, topologicalSort.size());
  }

  @Test
  public void testPathExistsBetween() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    Boolean exists = irDAG.pathExistsBetween(source, map);
    assertNotNull(exists);
    assertTrue(exists);
  }

  @Test
  public void testIsCompositeVertex() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    Boolean isComposite = irDAG.isCompositeVertex(map);
    assertNotNull(isComposite);
    assertFalse(isComposite);
  }

  @Test
  public void testGetLoopStackDepthOf() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    Integer depth = irDAG.getLoopStackDepthOf(map);
    assertNotNull(depth);
    assertEquals(Integer.valueOf(0), depth);
  }

  @Test
  public void testGetAssignedLoopVertexOf() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    LoopVertex loopVertex = irDAG.getAssignedLoopVertexOf(map);
    assertNull(loopVertex);
  }

  @Test
  public void testGetChildren() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IRVertex> children = irDAG.getChildren("source");
    assertNotNull(children);
    assertEquals(1, children.size());
    assertEquals("map", children.get(0).getId());
  }

  @Test
  public void testGetParents() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IRVertex> parents = irDAG.getParents("map");
    assertNotNull(parents);
    assertEquals(1, parents.size());
    assertEquals("source", parents.get(0).getId());
  }

  @Test
  public void testGetEdgeBetween() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    IREdge retrievedEdge = irDAG.getEdgeBetween("source", "map");
    assertNotNull(retrievedEdge);
    assertEquals(edge.getId(), retrievedEdge.getId());
  }

  @Test
  public void testGetAncestors() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IRVertex> ancestors = irDAG.getAncestors("map");
    assertNotNull(ancestors);
    assertEquals(1, ancestors.size());
    assertEquals("source", ancestors.get(0).getId());
  }

  @Test
  public void testGetDescendants() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IRVertex> descendants = irDAG.getDescendants("source");
    assertNotNull(descendants);
    assertEquals(1, descendants.size());
    assertEquals("map", descendants.get(0).getId());
  }

  @Test
  public void testToString() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex map = new MapVertex<>("map");
    builder.addVertex(source);
    builder.addVertex(map);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, map);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    String str = irDAG.toString();
    assertNotNull(str);
  }
}
