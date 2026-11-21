package org.apache.nemo.common.ir;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import org.apache.nemo.common.Pair;
import org.apache.nemo.common.Util;
import org.apache.nemo.common.coder.BytesDecoderFactory;
import org.apache.nemo.common.coder.BytesEncoderFactory;
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
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
  public void testCheckIntegrity() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    IRDAGChecker.CheckerResult result = irDAG.checkIntegrity();
    
    assertNotNull(result);
  }

  @Test
  public void testAdvanceDAGSnapshot() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    BiFunction<IRDAG, IRDAG, Boolean> checker = (a, b) -> true;
    boolean result = irDAG.advanceDAGSnapshot(checker);
    
    assertTrue(result);
  }

  @Test
  public void testIrDAGSummary() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    String summary = irDAG.irDAGSummary();
    
    assertNotNull(summary);
    assertTrue(summary.contains("rv"));
    assertTrue(summary.contains("_v"));
    assertTrue(summary.contains("_e"));
  }

  @Test
  public void testGetInputSize() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    source.setEstimatedSizeBytes(1024L);
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    Long inputSize = irDAG.getInputSize();
    
    assertEquals(Long.valueOf(1024L), inputSize);
  }

  @Test
  public void testRecordExecutorInfo() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<Pair<Integer, ResourceSpecification>> info = new ArrayList<>();
    Pair<Integer, ResourceSpecification> pair = new Pair<>(1, ResourceSpecification.of(1024));
    info.add(pair);
    
    irDAG.recordExecutorInfo(info);
    
    assertEquals(1, irDAG.getExecutorInfo().size());
  }

  @Test
  public void testGetExecutorInfo() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<Pair<Integer, ResourceSpecification>> info = new ArrayList<>();
    Pair<Integer, ResourceSpecification> pair = new Pair<>(1, ResourceSpecification.of(1024));
    info.add(pair);
    
    irDAG.recordExecutorInfo(info);
    
    List<Pair<Integer, ResourceSpecification>> retrieved = irDAG.getExecutorInfo();
    
    assertEquals(1, retrieved.size());
    assertEquals(Integer.valueOf(1), retrieved.get(0).left());
  }

  @Test
  public void testDeleteRelayVertex() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    RelayVertex relay = new RelayVertex("relay");
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, relay);
    IREdge edge2 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, relay, vertex);
    builder.addVertex(source);
    builder.addVertex(relay);
    builder.addVertex(vertex);
    builder.connectVertices(edge1);
    builder.connectVertices(edge2);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    irDAG.delete(relay);
    
    assertEquals(2, irDAG.getVertices().size());
    assertFalse(irDAG.getVertices().contains(relay));
  }

  @Test
  public void testInsertRelayVertex() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    RelayVertex relay = new RelayVertex("relay");
    irDAG.insert(relay, edge);
    
    assertEquals(3, irDAG.getVertices().size());
    assertTrue(irDAG.getVertices().contains(relay));
  }

  @Test
  public void testInsertMessageGeneratorAndAggregator() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    builder.addVertex(source);
    builder.addVertex(vertex);
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
    
    assertEquals(4, irDAG.getVertices().size());
    assertTrue(irDAG.getVertices().contains(messageGen));
    assertTrue(irDAG.getVertices().contains(messageAgg));
  }

  @Test
  public void testInsertSignalVertex() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    SignalVertex signal = new SignalVertex("signal");
    irDAG.insert(signal, edge);
    
    assertEquals(3, irDAG.getVertices().size());
    assertTrue(irDAG.getVertices().contains(signal));
  }

  @Test
  public void testInsertSamplingVertices() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    Set<SamplingVertex> samplingVertices = Sets.newHashSet(new SamplingVertex(new IRVertex("sampling"), 0.5));
    Set<IRVertex> executeAfter = Sets.newHashSet(vertex);
    
    irDAG.insert(samplingVertices, executeAfter);
    
    assertEquals(4, irDAG.getVertices().size());
    assertTrue(irDAG.getVertices().containsAll(samplingVertices));
  }

  @Test
  public void testInsertTaskSizeSplitterVertex() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    TaskSizeSplitterVertex splitter = Mockito.mock(TaskSizeSplitterVertex.class);
    when(splitter.getOriginalVertices()).thenReturn(Sets.newHashSet(vertex));
    
    irDAG.insert(splitter);
    
    assertEquals(3, irDAG.getVertices().size());
  }

  @Test
  public void testReshapeUnsafely() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    Function<DAG<IRVertex, IREdge>, DAG<IRVertex, IREdge>> reshaper = d -> d;
    irDAG.reshapeUnsafely(reshaper);
    
    assertNotNull(irDAG);
  }

  @Test
  public void testGetSinksWithinVertexSet() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    IRVertex vertex2 = new IRVertex("vertex2");
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    IREdge edge2 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, vertex1, vertex2);
    builder.addVertex(source);
    builder.addVertex(vertex1);
    builder.addVertex(vertex2);
    builder.connectVertices(edge1);
    builder.connectVertices(edge2);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    Set<IRVertex> vertexSet = Sets.newHashSet(vertex1, vertex2);
    Set<IRVertex> sinks = irDAG.getSinksWithinVertexSet(dag, vertexSet);
    
    assertEquals(1, sinks.size());
    assertTrue(sinks.contains(vertex2));
  }

  @Test
  public void testWrapSamplingVertexIfNeeded() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    builder.addVertex(source);
    builder.addVertex(vertex);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    SamplingVertex sampling = new SamplingVertex(vertex, 0.5);
    IRVertex result = irDAG.wrapSamplingVertexIfNeeded(vertex, sampling);
    
    assertTrue(result instanceof SamplingVertex);
  }

  @Test
  public void testEdgeToMessageAggregator() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    builder.addVertex(source);
    builder.addVertex(vertex);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    IREdge edge = irDAG.edgeToMessageAggregator(source, vertex,
                                                 EncoderProperty.of(BytesEncoderFactory.of()),
                                                 DecoderProperty.of(BytesDecoderFactory.of()));
    
    assertNotNull(edge);
    assertEquals(CommunicationPatternProperty.Value.SHUFFLE, 
                 edge.getPropertyValue(CommunicationPatternProperty.class).get());
  }

  @Test
  public void testTopologicalDo() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    final List<IRVertex> visited = new ArrayList<>();
    Consumer<IRVertex> consumer = visited::add;
    
    irDAG.topologicalDo(consumer);
    
    assertEquals(2, visited.size());
  }

  @Test
  public void testDFS() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    final List<IRVertex> visited = new ArrayList<>();
    Consumer<IRVertex> consumer = visited::add;
    
    irDAG.dfsTraverse(consumer, DAGInterface.TraversalOrder.TOP_DOWN);
    
    assertEquals(2, visited.size());
  }

  @Test
  public void testPathExistsBetween() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    Boolean result = irDAG.pathExistsBetween(source, vertex);
    
    assertTrue(result);
  }

  @Test
  public void testGetVertexById() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    builder.addVertex(source);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    IRVertex result = irDAG.getVertexById("source");
    
    assertEquals(source, result);
  }

  @Test
  public void testGetEdgeById() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    IREdge result = irDAG.getEdgeById(edge.getId());
    
    assertEquals(edge, result);
  }

  @Test
  public void testGetVertices() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    builder.addVertex(source);
    builder.addVertex(vertex);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IRVertex> vertices = irDAG.getVertices();
    
    assertEquals(2, vertices.size());
  }

  @Test
  public void testGetEdges() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IREdge> edges = irDAG.getEdges();
    
    assertEquals(1, edges.size());
  }

  @Test
  public void testGetRootVertices() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IRVertex> roots = irDAG.getRootVertices();
    
    assertEquals(1, roots.size());
    assertEquals(source, roots.get(0));
  }

  @Test
  public void testGetIncomingEdgesOf() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IREdge> edges = irDAG.getIncomingEdgesOf(vertex);
    
    assertEquals(1, edges.size());
    assertEquals(edge, edges.get(0));
  }

  @Test
  public void testGetOutgoingEdgesOf() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IREdge> edges = irDAG.getOutgoingEdgesOf(source);
    
    assertEquals(1, edges.size());
    assertEquals(edge, edges.get(0));
  }

  @Test
  public void testFilterVertices() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    builder.addVertex(source);
    builder.addVertex(vertex);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IRVertex> filtered = irDAG.filterVertices(v -> v instanceof SourceVertex);
    
    assertEquals(1, filtered.size());
    assertTrue(filtered.get(0) instanceof SourceVertex);
  }

  @Test
  public void testToString() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    String str = irDAG.toString();
    
    assertNotNull(str);
  }
}
