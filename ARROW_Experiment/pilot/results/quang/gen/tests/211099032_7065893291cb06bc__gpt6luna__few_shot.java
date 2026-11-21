package io.jaegertracing.analytics;

import static org.junit.Assert.assertTrue;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Test;

public class ServiceHeight_ArrowTest {

  @Test
  public void calculateWithNullGraphThrowsException() {
    try {
      ServiceHeight.calculate((Graph) null);
      assertTrue("Expected an exception", false);
    } catch (Exception expected) {
      assertTrue(true);
    }
  }
}
