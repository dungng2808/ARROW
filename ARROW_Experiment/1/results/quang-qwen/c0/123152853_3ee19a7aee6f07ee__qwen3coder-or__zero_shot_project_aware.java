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
import org.apache.nemo.common.ir.vertex.utility.RelayVertex;
import org.apache.nemo.common.ir.vertex.utility.SamplingVertex;
import org.apache.nemo.common.ir.vertex.utility.runtimepass.MessageAggregatorVertex;
import org.apache.nemo.common.ir.vertex.utility.runtimepass.MessageGeneratorVertex;
import org.apache.nemo.common.ir.vertex.utility.runtimepass.SignalVertex;
import org.apache.nemo.common.ir.vertex.utility.TaskSizeSplitterVertex;
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
    assertEquals(dag, irDAG.dagSnapshot);
    assertEquals(dag, irDAG.modifiedDAG);
    assertNotNull(irDAG.streamVertexToOriginalEdge);
    assertNotNull(irDAG.samplingVertexToGroup);
    assertNotNull(irDAG.messageVertexToGroup);
    assertNotNull(irDAG.executorInfo);
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
    assertEquals(irDAG.modifiedDAG, irDAG.dagSnapshot);
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
  }

  @Test
  public void testGetInputSize() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    Long size = irDAG.getInputSize();
    assertNotNull(size);
  }

  @Test
  public void testRecordExecutorInfo() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<Pair<Integer, ResourceSpecification>> info = new ArrayList<>();
    Pair<Integer, ResourceSpecification> pair = new Pair<>(1, ResourceSpecification.of(1000));
    info.add(pair);
    
    irDAG.recordExecutorInfo(info);
    
    assertEquals(info, irDAG.getExecutorInfo());
  }

  @Test
  public void testGetExecutorInfo() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<Pair<Integer, ResourceSpecification>> info = new ArrayList<>();
    Pair<Integer, ResourceSpecification> pair = new Pair<>(1, ResourceSpecification.of(1000));
    info.add(pair);
    
    irDAG.recordExecutorInfo(info);
    
    assertEquals(info, irDAG.getExecutorInfo());
  }

  @Test
  public void testDeleteRelayVertex() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    RelayVertex relay = new RelayVertex("relay");
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, relay);
    IREdge edge2 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, relay, vertex);
    
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.addVertex(relay);
    builder.connectVertices(edge1);
    builder.connectVertices(edge2);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    irDAG.delete(relay);
    
    assertTrue(irDAG.getVertices().contains(source));
    assertTrue(irDAG.getVertices().contains(vertex));
    assertFalse(irDAG.getVertices().contains(relay));
  }

  @Test
  public void testInsertRelayVertex() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    RelayVertex relay = new RelayVertex("relay");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    irDAG.insert(relay, edge);
    
    assertTrue(irDAG.getVertices().contains(relay));
    assertEquals(3, irDAG.getVertices().size());
  }

  @Test
  public void testInsertMessageGeneratorAndAggregator() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    MessageGeneratorVertex messageGen = new MessageGeneratorVertex<>(null);
    MessageAggregatorVertex messageAgg = new MessageAggregatorVertex(null);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    Set<IREdge> edgesToGetStats = Sets.newHashSet(edge);
    Set<IREdge> edgesToOptimize = Sets.newHashSet(edge);
    
    irDAG.insert(messageGen, messageAgg, 
                 EncoderProperty.of(BytesEncoderFactory.of()),
                 DecoderProperty.of(BytesDecoderFactory.of()),
                 edgesToGetStats, edgesToOptimize);
    
    assertTrue(irDAG.getVertices().contains(messageGen));
    assertTrue(irDAG.getVertices().contains(messageAgg));
  }

  @Test
  public void testInsertSignalVertex() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    SignalVertex signal = new SignalVertex("signal");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    irDAG.insert(signal, edge);
    
    assertTrue(irDAG.getVertices().contains(signal));
  }

  @Test
  public void testInsertSamplingVertices() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    SamplingVertex sampling = new SamplingVertex(new IRVertex("inner"), 0.5);
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    Set<SamplingVertex> toInsert = Sets.newHashSet(sampling);
    Set<IRVertex> executeAfter = Sets.newHashSet(vertex);
    
    irDAG.insert(toInsert, executeAfter);
    
    assertTrue(irDAG.getVertices().contains(sampling));
  }

  @Test
  public void testInsertTaskSizeSplitterVertex() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    TaskSizeSplitterVertex splitter = new TaskSizeSplitterVertex("splitter", new DAGBuilder<IRVertex, IREdge>().build());
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    irDAG.insert(splitter);
    
    assertTrue(irDAG.getVertices().contains(splitter));
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
    
    assertNotNull(irDAG.modifiedDAG);
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
    
    final List<String> visited = new ArrayList<>();
    Consumer<IRVertex> consumer = v -> visited.add(v.getId());
    
    irDAG.topologicalDo(consumer);
    
    assertEquals(2, visited.size());
  }

  @Test
  public void testGetVertices() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex = new IRVertex("vertex");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex);
    
    builder.addVertex(source);
    builder.addVertex(vertex);
    builder.connectVertices(edge);
    
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
