package org.apache.nemo.common.ir;

import org.apache.nemo.common.dag.DAG;
import org.apache.nemo.common.dag.DAGBuilder;
import org.apache.nemo.common.ir.edge.IREdge;
import org.apache.nemo.common.ir.edge.executionproperty.CommunicationPatternProperty;
import org.apache.nemo.common.ir.vertex.IRVertex;
import org.apache.nemo.common.ir.vertex.SourceVertex;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IRDAG_ArrowTest {
  private IRDAG emptyIRDAG() {
    return new IRDAG(new DAGBuilder<IRVertex, IREdge>().build());
  }

  @Test
  public void emptyDagHasNoVerticesEdgesOrRoots() {
    IRDAG dag = emptyIRDAG();

    assertTrue(dag.getVertices().isEmpty());
    assertTrue(dag.getEdges().isEmpty());
    assertTrue(dag.getRootVertices().isEmpty());
    assertEquals(Long.valueOf(0L), dag.getInputSize());
    assertEquals("rv0_v0_e0_0B", dag.irDAGSummary());
  }

  @Test
  public void forwardsDagVertexAndEdgeQueries() {
    IRVertex source = new SourceVertex();
    IRVertex destination = new SourceVertex();
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, destination);
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    builder.addVertex(source);
    builder.addVertex(destination);
    builder.connectVertices(edge);
    IRDAG dag = new IRDAG(builder.build());

    assertEquals(2, dag.getVertices().size());
    assertEquals(1, dag.getEdges().size());
    assertEquals(1, dag.getRootVertices().size());
    assertEquals(source, dag.getRootVertices().get(0));
    assertEquals(edge, dag.getEdgeBetween(source.getId(), destination.getId()));
    assertEquals(destination, dag.getChildren(source.getId()).get(0));
    assertEquals(source, dag.getParents(destination.getId()).get(0));
    assertEquals(edge, dag.getIncomingEdgesOf(destination).get(0));
    assertEquals(edge, dag.getOutgoingEdgesOf(source).get(0));
    assertEquals(edge, dag.getIncomingEdgesOf(destination.getId()).get(0));
    assertEquals(edge, dag.getOutgoingEdgesOf(source.getId()).get(0));
    assertEquals(source, dag.getVertexById(source.getId()));
    assertEquals(edge, dag.getEdgeById(edge.getId()));
    assertTrue(dag.pathExistsBetween(source, destination));
    assertFalse(dag.pathExistsBetween(destination, source));
    assertEquals(2, dag.getTopologicalSort().size());
    assertEquals(1, dag.getDescendants(source.getId()).size());
    assertEquals(1, dag.getAncestors(destination.getId()).size());
  }

  @Test
  public void traversalsVisitVertices() {
    IRVertex source = new SourceVertex();
    IRVertex destination = new SourceVertex();
    IREdge edge = new IREdge(CommunicationPatternProperty.Value.ONE_TO_ONE, source, destination);
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    builder.addVertex(source);
    builder.addVertex(destination);
    builder.connectVertices(edge);
    IRDAG dag = new IRDAG(builder.build());
    final List<IRVertex> topologicalVisits = new ArrayList<>();
    final List<IRVertex> depthFirstVisits = new ArrayList<>();

    dag.topologicalDo(topologicalVisits::add);
    dag.dfsTraverse(depthFirstVisits::add, DAGInterface.TraversalOrder.PreOrder);

    assertEquals(2, topologicalVisits.size());
    assertEquals(2, depthFirstVisits.size());
    assertTrue(topologicalVisits.contains(source));
    assertTrue(topologicalVisits.contains(destination));
    assertTrue(depthFirstVisits.contains(source));
    assertTrue(depthFirstVisits.contains(destination));
  }

  @Test
  public void unsafeReshapeCanReplaceDag() {
    IRDAG dag = emptyIRDAG();
    IRVertex vertex = new SourceVertex();
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    builder.addVertex(vertex);
    final DAG<IRVertex, IREdge> replacement = builder.build();

    dag.reshapeUnsafely(ignored -> replacement);

    assertEquals(1, dag.getVertices().size());
    assertEquals(vertex, dag.getVertices().get(0));
  }

  @Test
  public void snapshotAdvancesWhenCheckerAccepts() {
    IRDAG dag = emptyIRDAG();
    IRVertex vertex = new SourceVertex();
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    builder.addVertex(vertex);
    final DAG<IRVertex, IREdge> replacement = builder.build();
    dag.reshapeUnsafely(ignored -> replacement);

    assertTrue(dag.advanceDAGSnapshot((before, after) -> true));
  }

  @Test
  public void snapshotDoesNotAdvanceWhenCheckerRejects() {
    IRDAG dag = emptyIRDAG();
    IRVertex vertex = new SourceVertex();
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    builder.addVertex(vertex);
    dag.reshapeUnsafely(ignored -> builder.build());

    assertFalse(dag.advanceDAGSnapshot((before, after) -> false));
  }

  @Test
  public void integrityCheckReturnsResult() {
    IRDAG dag = emptyIRDAG();

    assertNotNull(dag.checkIntegrity());
  }

  @Test
  public void stringRepresentationIsNotNull() {
    assertNotNull(emptyIRDAG().toString());
  }
}
