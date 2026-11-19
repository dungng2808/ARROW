package org.apache.nemo.common.ir;

import org.apache.nemo.common.dag.DAG;
import org.apache.nemo.common.dag.DAGBuilder;
import org.apache.nemo.common.ir.edge.IREdge;
import org.apache.nemo.common.ir.edge.executionproperty.CommunicationPatternProperty;
import org.apache.nemo.common.ir.vertex.IRVertex;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IRDAG_ArrowTest {
  private DAG<IRVertex, IREdge> emptyDag() {
    return new DAGBuilder<IRVertex, IREdge>().build();
  }

  @Test
  public void emptyDagHasNoVerticesOrEdges() {
    IRDAG dag = new IRDAG(emptyDag());

    assertTrue(dag.getVertices().isEmpty());
    assertTrue(dag.getEdges().isEmpty());
    assertTrue(dag.getRootVertices().isEmpty());
    assertEquals(Long.valueOf(0L), dag.getInputSize());
    assertEquals("rv0_v0_e0_0B", dag.irDAGSummary());
  }

  @Test
  public void unsafeReshapeCanReplaceDag() {
    IRDAG dag = new IRDAG(emptyDag());
    DAG<IRVertex, IREdge> replacement = emptyDag();

    dag.reshapeUnsafely(current -> replacement);

    assertNotNull(dag.getVertices());
    assertTrue(dag.getVertices().isEmpty());
    assertTrue(dag.getEdges().isEmpty());
  }

  @Test
  public void snapshotAdvancesWhenCheckerAccepts() {
    IRDAG dag = new IRDAG(emptyDag());

    assertTrue(dag.advanceDAGSnapshot((snapshot, modified) -> true));
  }

  @Test
  public void snapshotDoesNotAdvanceWhenCheckerRejects() {
    IRDAG dag = new IRDAG(emptyDag());

    assertFalse(dag.advanceDAGSnapshot((snapshot, modified) -> false));
  }

  @Test
  public void topologicalTraversalVisitsEveryVertex() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    IRDAG dag = new IRDAG(builder.build());
    List<IRVertex> visited = new ArrayList<>();

    dag.topologicalDo(visited::add);

    assertTrue(visited.isEmpty());
  }

  @Test
  public void filterVerticesReturnsEmptyListForEmptyDag() {
    IRDAG dag = new IRDAG(emptyDag());

    assertTrue(dag.filterVertices(vertex -> true).isEmpty());
  }

  @Test
  public void pathDoesNotExistInEmptyDag() {
    IRDAG dag = new IRDAG(emptyDag());

    assertFalse(dag.pathExistsBetween(null, null));
  }

  @Test
  public void jsonRepresentationIsAvailableForEmptyDag() {
    IRDAG dag = new IRDAG(emptyDag());

    assertNotNull(dag.asJsonNode());
    assertEquals(dag.asJsonNode().toString(), dag.toString());
  }

  @Test
  public void executorInfoStartsEmpty() {
    IRDAG dag = new IRDAG(emptyDag());

    assertTrue(dag.getExecutorInfo().isEmpty());
  }
}
