package com.github.phenomics.ontolib.ontology.scoredist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ScoreSamplingOptions_ArrowTest {

  @Test
  public void testDefaultConstructor() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();
    
    assertEquals(1, options.getNumThreads());
    assertNull(options.getMinObjectId());
    assertNull(options.getMaxObjectId());
    assertEquals(1, options.getMinNumTerms());
    assertEquals(20, options.getMaxNumTerms());
    assertEquals(100000, options.getNumIterations());
    assertEquals(42, options.getSeed());
  }

  @Test
  public void testFullConstructor() {
    Integer minObjectId = 10;
    Integer maxObjectId = 100;
    ScoreSamplingOptions options = new ScoreSamplingOptions(
        5, minObjectId, maxObjectId, 3, 15, 123, 50000);

    assertEquals(5, options.getNumThreads());
    assertEquals(minObjectId, options.getMinObjectId());
    assertEquals(maxObjectId, options.getMaxObjectId());
    assertEquals(3, options.getMinNumTerms());
    assertEquals(15, options.getMaxNumTerms());
    assertEquals(50000, options.getNumIterations());
    assertEquals(123, options.getSeed());
  }

  @Test
  public void testGetAndSetNumThreads() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();
    options.setNumThreads(10);
    assertEquals(10, options.getNumThreads());
  }

  @Test
  public void testGetAndSetMinObjectId() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();
    Integer value = 42;
    options.setMinObjectId(value);
    assertEquals(value, options.getMinObjectId());
    
    options.setMinObjectId(null);
    assertNull(options.getMinObjectId());
  }

  @Test
  public void testGetAndSetMaxObjectId() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();
    Integer value = 1000;
    options.setMaxObjectId(value);
    assertEquals(value, options.getMaxObjectId());
    
    options.setMaxObjectId(null);
    assertNull(options.getMaxObjectId());
  }

  @Test
  public void testGetAndSetMinNumTerms() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();
    options.setMinNumTerms(50);
    assertEquals(50, options.getMinNumTerms());
  }

  @Test
  public void testGetAndSetMaxNumTerms() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();
    options.setMaxNumTerms(100);
    assertEquals(100, options.getMaxNumTerms());
  }

  @Test
  public void testGetAndSetSeed() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();
    options.setSeed(999);
    assertEquals(999, options.getSeed());
  }

  @Test
  public void testGetAndSetNumIterations() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();
    options.setNumIterations(25000);
    assertEquals(25000, options.getNumIterations());
  }

  @Test
  public void testClone() throws CloneNotSupportedException {
    ScoreSamplingOptions original = new ScoreSamplingOptions(5, 10, 100, 3, 15, 123, 50000);
    ScoreSamplingOptions cloned = (ScoreSamplingOptions) original.clone();
    
    assertEquals(original.getNumThreads(), cloned.getNumThreads());
    assertEquals(original.getMinObjectId(), cloned.getMinObjectId());
    assertEquals(original.getMaxObjectId(), cloned.getMaxObjectId());
    assertEquals(original.getMinNumTerms(), cloned.getMinNumTerms());
    assertEquals(original.getMaxNumTerms(), cloned.getMaxNumTerms());
    assertEquals(original.getNumIterations(), cloned.getNumIterations());
    assertEquals(original.getSeed(), cloned.getSeed());
  }

  @Test
  public void testToString() {
    ScoreSamplingOptions options = new ScoreSamplingOptions(2, 5, 50, 1, 10, 100, 1000);
    String result = options.toString();
    
    assertNotNull(result);
    // Check that all fields are present in the string representation
    assertEquals(true, result.contains("numThreads=2"));
    assertEquals(true, result.contains("minObjectId=5"));
    assertEquals(true, result.contains("maxObjectId=50"));
    assertEquals(true, result.contains("minNumTerms=1"));
    assertEquals(true, result.contains("maxNumTerms=10"));
    assertEquals(true, result.contains("numIterations=1000"));
    assertEquals(true, result.contains("seed=100"));
  }
}
