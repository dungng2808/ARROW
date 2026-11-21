package io.jaegertracing.analytics;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Test;

public class ServiceHeight_ArrowTest {

  @Test
  public void calculateReturnsEmptyMapForGraphWithoutVertices() {
    Graph graph = GraphTraversalTestSupport.emptyGraph();

    Map<String, Integer> result = ServiceHeight.calculate(graph);

    assertEquals(Collections.emptyMap(), result);
  }
}
