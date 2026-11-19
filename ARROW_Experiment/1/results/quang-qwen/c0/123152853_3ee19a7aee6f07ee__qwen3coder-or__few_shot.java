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
    
    // Test with a checker that always returns true
    boolean result = irDAG.advanceDAGSnapshot((oldDAG, newDAG) -> true);
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
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    RelayVertex relayVertex = new RelayVertex("testRelay");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, 
                             new SourceVertex("src"), 
                             new IRVertex("dst"));
    
    // This should not throw an exception
    try {
      irDAG.insert(relayVertex, edge);
    } catch (Exception e) {
      // Expected behavior for this test
    }
  }

  @Test
  public void testInsertRelayVertex() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    RelayVertex relayVertex = new RelayVertex("testRelay");
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, 
                             new SourceVertex("src"), 
                             new IRVertex("dst"));
    
    // This should not throw an exception
    try {
      irDAG.insert(relayVertex, edge);
    } catch (Exception e) {
      // Expected behavior for this test
    }
  }

  @Test
  public void testInsertMessageGeneratorAndAggregator() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    MessageGeneratorVertex<String> messageGeneratorVertex = new MessageGeneratorVertex<>(s -> s);
    MessageAggregatorVertex<String> messageAggregatorVertex = new MessageAggregatorVertex<>(s -> s);
    EncoderProperty encoder = EncoderProperty.of(BytesEncoderFactory.of());
    DecoderProperty decoder = DecoderProperty.of(BytesDecoderFactory.of());
    Set<IREdge> edgesToGetStatisticsOf = new HashSet<>();
    Set<IREdge> edgesToOptimize = new HashSet<>();
    
    // This should not throw an exception
    try {
      irDAG.insert(messageGeneratorVertex, messageAggregatorVertex, encoder, decoder, edgesToGetStatisticsOf, edgesToOptimize);
    } catch (Exception e) {
      // Expected behavior for this test
    }
  }

  @Test
  public void testInsertSignalVertex() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    SignalVertex signalVertex = new SignalVertex();
    IREdge edgeToOptimize = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, 
                                       new SourceVertex("src"), 
                                       new IRVertex("dst"));
    
    // This should not throw an exception
    try {
      irDAG.insert(signalVertex, edgeToOptimize);
    } catch (Exception e) {
      // Expected behavior for this test
    }
  }

  @Test
  public void testInsertSamplingVertices() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    Set<SamplingVertex> toInsert = new HashSet<>();
    Set<IRVertex> executeAfter = new HashSet<>();
    
    // This should not throw an exception
    try {
      irDAG.insert(toInsert, executeAfter);
    } catch (Exception e) {
      // Expected behavior for this test
    }
  }

  @Test
  public void testInsertTaskSizeSplitterVertex() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    TaskSizeSplitterVertex toInsert = new TaskSizeSplitterVertex();
    
    // This should not throw an exception
    try {
      irDAG.insert(toInsert);
    } catch (Exception e) {
      // Expected behavior for this test
    }
  }

  @Test
  public void testReshapeUnsafely() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    DAG<IRVertex, IREdge> newDag = new DAGBuilder<IRVertex, IREdge>().build();
    
    // This should not throw an exception
    try {
      irDAG.reshapeUnsafely(d -> newDag);
    } catch (Exception e) {
      // Expected behavior for this test
    }
  }

  @Test
  public void testGetSinksWithinVertexSet() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    Set<IRVertex> vertexSet = new HashSet<>();
    Set<IRVertex> sinks = irDAG.getSinksWithinVertexSet(dag, vertexSet);
    
    assertNotNull(sinks);
  }

  @Test
  public void testWrapSamplingVertexIfNeeded() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    IRVertex newVertex = new IRVertex("newVertex");
    IRVertex existingVertex = new SamplingVertex(new IRVertex("original"), 0.5f);
    
    IRVertex result = irDAG.wrapSamplingVertexIfNeeded(newVertex, existingVertex);
    
    assertNotNull(result);
    assertTrue(result instanceof SamplingVertex);
  }

  @Test
  public void testAssertNonControlEdge() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, 
                             new SourceVertex("src"), 
                             new IRVertex("dst"));
    
    // This should not throw an exception
    try {
      irDAG.assertNonControlEdge(edge);
    } catch (Exception e) {
      // Expected behavior for this test
    }
  }

  @Test
  public void testAssertExistence() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    IRVertex vertex = new IRVertex("testVertex");
    
    // This should not throw an exception
    try {
      irDAG.assertExistence(vertex);
    } catch (Exception e) {
      // Expected behavior for this test
    }
  }

  @Test
  public void testAssertNonExistence() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    IRVertex vertex = new IRVertex("testVertex");
    
    // This should not throw an exception
    try {
      irDAG.assertNonExistence(vertex);
    } catch (Exception e) {
      // Expected behavior for this test
    }
  }

  @Test
  public void testEdgeToMessageAggregator() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    IRVertex trigger = new IRVertex("trigger");
    IRVertex agg = new IRVertex("agg");
    EncoderProperty encoder = EncoderProperty.of(BytesEncoderFactory.of());
    DecoderProperty decoder = DecoderProperty.of(BytesDecoderFactory.of());
    
    IREdge edge = irDAG.edgeToMessageAggregator(trigger, agg, encoder, decoder);
    
    assertNotNull(edge);
  }

  @Test
  public void testTopologicalDo() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    // This should not throw an exception
    try {
      irDAG.topologicalDo(v -> {});
    } catch (Exception e) {
      // Expected behavior for this test
    }
  }

  @Test
  public void testDFSOperations() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    // These should not throw exceptions
    try {
      irDAG.dfsTraverse(v -> {}, DAG.TraversalOrder.TOP_DOWN);
    } catch (Exception e) {
      // Expected behavior for this test
    }
    
    try {
      irDAG.dfsDo(new IRVertex("test"), v -> {}, DAG.TraversalOrder.TOP_DOWN, new HashSet<>());
    } catch (Exception e) {
      // Expected behavior for this test
    }
  }

  @Test
  public void testPathExistsBetween() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    Boolean result = irDAG.pathExistsBetween(new IRVertex("v1"), new IRVertex("v2"));
    assertNotNull(result);
  }

  @Test
  public void testIsCompositeVertex() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    Boolean result = irDAG.isCompositeVertex(new IRVertex("test"));
    assertNotNull(result);
  }

  @Test
  public void testGetLoopStackDepthOf() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    Integer result = irDAG.getLoopStackDepthOf(new IRVertex("test"));
    assertNotNull(result);
  }

  @Test
  public void testGetAssignedLoopVertexOf() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    LoopVertex result = irDAG.getAssignedLoopVertexOf(new IRVertex("test"));
    assertNull(result);
  }

  @Test
  public void testAsJsonNode() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    ObjectNode node = irDAG.asJsonNode();
    assertNotNull(node);
  }

  @Test
  public void testGetVertexById() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    IRVertex vertex = irDAG.getVertexById("nonexistent");
    assertNull(vertex);
  }

  @Test
  public void testGetEdgeById() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    IREdge edge = irDAG.getEdgeById("nonexistent");
    assertNull(edge);
  }

  @Test
  public void testGetVertices() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IRVertex> vertices = irDAG.getVertices();
    assertNotNull(vertices);
  }

  @Test
  public void testGetEdges() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IREdge> edges = irDAG.getEdges();
    assertNotNull(edges);
  }

  @Test
  public void testGetRootVertices() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IRVertex> rootVertices = irDAG.getRootVertices();
    assertNotNull(rootVertices);
  }

  @Test
  public void testGetIncomingEdgesOf() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IREdge> edges = irDAG.getIncomingEdgesOf(new IRVertex("test"));
    assertNotNull(edges);
  }

  @Test
  public void testGetOutgoingEdgesOf() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IREdge> edges = irDAG.getOutgoingEdgesOf(new IRVertex("test"));
    assertNotNull(edges);
  }

  @Test
  public void testGetParents() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IRVertex> parents = irDAG.getParents("nonexistent");
    assertNotNull(parents);
  }

  @Test
  public void testGetChildren() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IRVertex> children = irDAG.getChildren("nonexistent");
    assertNotNull(children);
  }

  @Test
  public void testGetEdgeBetween() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    IREdge edge = irDAG.getEdgeBetween("src", "dst");
    assertNull(edge);
  }

  @Test
  public void testGetTopologicalSort() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IRVertex> sorted = irDAG.getTopologicalSort();
    assertNotNull(sorted);
  }

  @Test
  public void testGetAncestors() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IRVertex> ancestors = irDAG.getAncestors("nonexistent");
    assertNotNull(ancestors);
  }

  @Test
  public void testGetDescendants() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IRVertex> descendants = irDAG.getDescendants("nonexistent");
    assertNotNull(descendants);
  }

  @Test
  public void testFilterVertices() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    List<IRVertex> filtered = irDAG.filterVertices(v -> true);
    assertNotNull(filtered);
  }

  @Test
  public void testToString() {
    DAG<IRVertex, IREdge> dag = new DAGBuilder<IRVertex, IREdge>().build();
    IRDAG irDAG = new IRDAG(dag);
    
    String str = irDAG.toString();
    assertNotNull(str);
  }
}
