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
import org.apache.nemo.common.ir.vertex.utility.RelayVertex;
import org.apache.nemo.common.ir.vertex.utility.SamplingVertex;
import org.apache.nemo.common.ir.vertex.utility.runtimepass.MessageAggregatorVertex;
import org.apache.nemo.common.ir.vertex.utility.runtimepass.MessageGeneratorVertex;
import org.apache.nemo.common.ir.vertex.utility.runtimepass.SignalVertex;
import org.apache.nemo.common.ir.vertex.utility.TaskSizeSplitterVertex;
import org.apache.nemo.common.exception.CompileTimeOptimizationException;
import org.apache.nemo.common.exception.MetricException;
import org.apache.nemo.common.coder.BytesDecoderFactory;
import org.apache.nemo.common.coder.BytesEncoderFactory;
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
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    String summary = irDAG.irDAGSummary();
    assertNotNull(summary);
  }

  @Test
  public void testGetInputSize() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    Long inputSize = irDAG.getInputSize();
    assertNotNull(inputSize);
  }

  @Test
  public void testRecordExecutorInfo() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<Pair<Integer, ResourceSpecification>> executorInfo = new ArrayList<>();
    irDAG.recordExecutorInfo(executorInfo);
    
    assertEquals(executorInfo, irDAG.getExecutorInfo());
  }

  @Test
  public void testGetExecutorInfo() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<Pair<Integer, ResourceSpecification>> executorInfo = new ArrayList<>();
    irDAG.recordExecutorInfo(executorInfo);
    
    assertEquals(executorInfo, irDAG.getExecutorInfo());
  }

  @Test
  public void testDeleteRelayVertex() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    RelayVertex relay = new RelayVertex("relay");
    IRVertex vertex2 = new IRVertex("vertex2");
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    builder.addVertex(relay);
    builder.addVertex(vertex2);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    IREdge edge2 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, vertex1, relay);
    IREdge edge3 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, relay, vertex2);
    
    builder.connectVertices(edge1);
    builder.connectVertices(edge2);
    builder.connectVertices(edge3);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    irDAG.delete(relay);
    
    assertTrue(irDAG.getVertices().contains(source));
    assertTrue(irDAG.getVertices().contains(vertex1));
    assertTrue(irDAG.getVertices().contains(vertex2));
    assertFalse(irDAG.getVertices().contains(relay));
  }

  @Test
  public void testInsertRelayVertex() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    RelayVertex relay = new RelayVertex("relay");
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    builder.addVertex(relay);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    builder.connectVertices(edge1);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    irDAG.insert(relay, edge1);
    
    assertTrue(irDAG.getVertices().contains(relay));
    assertTrue(irDAG.getEdges().size() >= 3);
  }

  @Test
  public void testInsertMessageGeneratorAndAggregator() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    MessageGeneratorVertex<String> messageGenerator = new MessageGeneratorVertex<>(s -> s);
    MessageAggregatorVertex<String> messageAggregator = new MessageAggregatorVertex<>(s -> s);
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    builder.addVertex(messageGenerator);
    builder.addVertex(messageAggregator);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    builder.connectVertices(edge1);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    Set<IREdge> edgesToGetStatisticsOf = new HashSet<>();
    edgesToGetStatisticsOf.add(edge1);
    Set<IREdge> edgesToOptimize = new HashSet<>();
    edgesToOptimize.add(edge1);
    
    irDAG.insert(messageGenerator, messageAggregator, 
                 EncoderProperty.of(BytesEncoderFactory.of()), 
                 DecoderProperty.of(BytesDecoderFactory.of()), 
                 edgesToGetStatisticsOf, 
                 edgesToOptimize);
    
    assertTrue(irDAG.getVertices().contains(messageGenerator));
    assertTrue(irDAG.getVertices().contains(messageAggregator));
  }

  @Test
  public void testInsertSignalVertex() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    SignalVertex signal = new SignalVertex("signal");
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    builder.addVertex(signal);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    builder.connectVertices(edge1);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    irDAG.insert(signal, edge1);
    
    assertTrue(irDAG.getVertices().contains(signal));
  }

  @Test
  public void testInsertSamplingVertices() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    SamplingVertex sampling1 = new SamplingVertex(new IRVertex("sampling1"), 0.5);
    SamplingVertex sampling2 = new SamplingVertex(new IRVertex("sampling2"), 0.5);
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    builder.addVertex(sampling1);
    builder.addVertex(sampling2);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    builder.connectVertices(edge1);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    Set<SamplingVertex> toInsert = new HashSet<>();
    toInsert.add(sampling1);
    toInsert.add(sampling2);
    
    Set<IRVertex> executeAfter = new HashSet<>();
    executeAfter.add(vertex1);
    
    irDAG.insert(toInsert, executeAfter);
    
    assertTrue(irDAG.getVertices().contains(sampling1));
    assertTrue(irDAG.getVertices().contains(sampling2));
  }

  @Test
  public void testInsertTaskSizeSplitterVertex() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    builder.connectVertices(edge1);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    TaskSizeSplitterVertex splitter = new TaskSizeSplitterVertex("splitter");
    
    irDAG.insert(splitter);
    
    assertTrue(irDAG.getVertices().contains(splitter));
  }

  @Test
  public void testReshapeUnsafely() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    builder.connectVertices(edge1);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    DAG<IRVertex, IREdge> newDag = irDAG.getVertices().stream()
        .filter(v -> v.getId().equals("source"))
        .findFirst()
        .map(v -> {
          DAGBuilder<IRVertex, IREdge> newBuilder = new DAGBuilder<>();
          newBuilder.addVertex(v);
          return newBuilder.build();
        })
        .orElse(null);
    
    irDAG.reshapeUnsafely(d -> newDag);
    
    assertNotNull(irDAG.getVertices());
  }

  @Test
  public void testGetSinksWithinVertexSet() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    IRVertex vertex2 = new IRVertex("vertex2");
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    builder.addVertex(vertex2);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    IREdge edge2 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, vertex1, vertex2);
    builder.connectVertices(edge1);
    builder.connectVertices(edge2);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    Set<IRVertex> vertexSet = new HashSet<>();
    vertexSet.add(vertex1);
    vertexSet.add(vertex2);
    
    Set<IRVertex> sinks = irDAG.getSinksWithinVertexSet(dag, vertexSet);
    
    assertEquals(1, sinks.size());
    assertTrue(sinks.contains(vertex2));
  }

  @Test
  public void testWrapSamplingVertexIfNeeded() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    SamplingVertex sampling = new SamplingVertex(new IRVertex("sampling"), 0.5);
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    builder.addVertex(sampling);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    builder.connectVertices(edge1);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    IRVertex result = irDAG.wrapSamplingVertexIfNeeded(vertex1, sampling);
    
    assertTrue(result instanceof SamplingVertex);
  }

  @Test
  public void testAssertNonControlEdge() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    builder.connectVertices(edge1);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    try {
      irDAG.assertNonControlEdge(edge1);
      assertTrue(true); // Should not throw exception
    } catch (IllegalArgumentException e) {
      fail("Should not throw IllegalArgumentException");
    }
  }

  @Test
  public void testAssertExistence() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    try {
      irDAG.assertExistence(vertex1);
      assertTrue(true); // Should not throw exception
    } catch (IllegalArgumentException e) {
      fail("Should not throw IllegalArgumentException");
    }
  }

  @Test
  public void testAssertNonExistence() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    IRVertex vertex2 = new IRVertex("vertex2");
    
    try {
      irDAG.assertNonExistence(vertex2);
      assertTrue(true); // Should not throw exception
    } catch (IllegalArgumentException e) {
      fail("Should not throw IllegalArgumentException");
    }
  }

  @Test
  public void testEdgeToMessageAggregator() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    MessageAggregatorVertex<String> messageAggregator = new MessageAggregatorVertex<>(s -> s);
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    builder.addVertex(messageAggregator);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    builder.connectVertices(edge1);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    IREdge result = irDAG.edgeToMessageAggregator(source, messageAggregator, 
                                                   EncoderProperty.of(BytesEncoderFactory.of()), 
                                                   DecoderProperty.of(BytesDecoderFactory.of()));
    
    assertNotNull(result);
    assertEquals(CommunicationPatternProperty.Value.SHUFFLE, 
                 result.getPropertyValue(CommunicationPatternProperty.class).get());
  }

  @Test
  public void testGetVertexById() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    builder.connectVertices(edge1);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    IRVertex result = irDAG.getVertexById("source");
    
    assertNotNull(result);
    assertEquals("source", result.getId());
  }

  @Test
  public void testGetEdgeById() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    builder.connectVertices(edge1);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    IREdge result = irDAG.getEdgeById(edge1.getId());
    
    assertNotNull(result);
    assertEquals(edge1.getId(), result.getId());
  }

  @Test
  public void testGetVertices() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    builder.connectVertices(edge1);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IRVertex> vertices = irDAG.getVertices();
    
    assertNotNull(vertices);
    assertEquals(2, vertices.size());
  }

  @Test
  public void testGetEdges() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    builder.connectVertices(edge1);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IREdge> edges = irDAG.getEdges();
    
    assertNotNull(edges);
    assertEquals(1, edges.size());
  }

  @Test
  public void testGetRootVertices() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    builder.connectVertices(edge1);
    
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
    IRVertex vertex1 = new IRVertex("vertex1");
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    builder.connectVertices(edge1);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IREdge> incomingEdges = irDAG.getIncomingEdgesOf(vertex1);
    
    assertNotNull(incomingEdges);
    assertEquals(1, incomingEdges.size());
    assertEquals(edge1.getId(), incomingEdges.get(0).getId());
  }

  @Test
  public void testGetOutgoingEdgesOf() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    builder.connectVertices(edge1);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IREdge> outgoingEdges = irDAG.getOutgoingEdgesOf(source);
    
    assertNotNull(outgoingEdges);
    assertEquals(1, outgoingEdges.size());
    assertEquals(edge1.getId(), outgoingEdges.get(0).getId());
  }

  @Test
  public void testAsJsonNode() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    builder.connectVertices(edge1);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    ObjectNode jsonNode = irDAG.asJsonNode();
    
    assertNotNull(jsonNode);
  }

  @Test
  public void testToString() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    SourceVertex source = new SourceVertex<>("source");
    IRVertex vertex1 = new IRVertex("vertex1");
    
    builder.addVertex(source);
    builder.addVertex(vertex1);
    
    IREdge edge1 = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, vertex1);
    builder.connectVertices(edge1);
    
    DAG<IRVertex, IREdge> dag = builder.build();
    IRDAG irDAG = new IRDAG(dag);
    
    String str = irDAG.toString();
    
    assertNotNull(str);
  }
}
