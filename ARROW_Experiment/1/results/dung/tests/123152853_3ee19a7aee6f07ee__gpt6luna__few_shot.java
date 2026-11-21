package org.apache.nemo.common.ir;

import org.apache.nemo.common.dag.DAG;
import org.apache.nemo.common.dag.DAGBuilder;
import org.apache.nemo.common.ir.edge.IREdge;
import org.apache.nemo.common.ir.edge.executionproperty.CommunicationPatternProperty;
import org.apache.nemo.common.ir.vertex.IRVertex;
import org.apache.nemo.common.ir.vertex.OperatorVertex;
import org.apache.nemo.common.ir.vertex.transform.Transform;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IRDAG_ArrowTest {

  private IRDAG emptyIRDAG() {
    DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    return new IRDAG(builder.build());
  }

  @Test
  public void emptyDagHasNoVerticesOrEdges() {
    IRDAG dag = emptyIRDAG();

    assertTrue(dag.getVertices().isEmpty());
    assertTrue(dag.getEdges().isEmpty());
    assertTrue(dag.getRootVertices().isEmpty());
    assertEquals(Long.valueOf(0L), dag.getInputSize());
    assertEquals("rv0_v0_e0_0B", dag.irDAGSummary());
  }

  @Test
  public void advanceSnapshotUsesCheckerResult() {
    IRDAG dag = emptyIRDAG();

    assertTrue(dag.advanceDAGSnapshot((snapshot, modified) -> true));
    assertFalse(dag.advanceDAGSnapshot((snapshot, modified) -> false));
  }

  @Test
  public void reshapeUnsafelyCanReplaceDag() {
    IRDAG dag = emptyIRDAG();

    dag.reshapeUnsafely(original -> {
      DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
      return builder.build();
    });

    assertTrue(dag.getVertices().isEmpty());
    assertTrue(dag.getEdges().isEmpty());
  }

  @Test
  public void executorInfoStartsEmpty() {
    IRDAG dag = emptyIRDAG();

    assertTrue(dag.getExecutorInfo().isEmpty());
  }
}
