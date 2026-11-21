package io.jaegertracing.analytics;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Test;

public class ServiceHeight_ArrowTest {

  @Test
  public void calculateWithNullGraphThrowsException() {
    boolean thrown = false;
    try {
      ServiceHeight.calculate((Graph) null);
    } catch (Exception e) {
      thrown = true;
    }
    assertTrue(thrown);
  }
}
