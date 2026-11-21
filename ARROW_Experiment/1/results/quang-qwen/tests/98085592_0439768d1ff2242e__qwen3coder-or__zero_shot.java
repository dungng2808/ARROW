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
    Integer minObjId = 10;
    Integer maxObjId = 100;
    ScoreSamplingOptions options = new ScoreSamplingOptions(
        4, minObjId, maxObjId, 5, 15, 123, 50000);
    
    assertEquals(4, options.getNumThreads());
    assertEquals(minObjId, options.getMinObjectId());
    assertEquals(maxObjId, options.getMaxObjectId());
    assertEquals(5, options.getMinNumTerms());
    assertEquals(15, options.getMaxNumTerms());
    assertEquals(50000, options.getNumIterations());
    assertEquals(123, options.getSeed());
  }
  
  @Test
  public void testGetSetNumThreads() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();
    assertEquals(1, options.getNumThreads());
    
    options.setNumThreads(5);
    assertEquals(5, options.getNumThreads());
  }
  
  @Test
  public void testGetSetMinObjectId() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();
    assertNull(options.getMinObjectId());
    
    Integer value = 42;
    options.setMinObjectId(value);
    assertEquals(value, options.getMinObjectId());
  }
  
  @Test
  public void testGetSetMaxObjectId() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();
    assertNull(options.getMaxObjectId());
    
    Integer value = 1000;
    options.setMaxObjectId(value);
    assertEquals(value, options.getMaxObjectId());
  }
  
  @Test
  public void testGetSetMinNumTerms() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();
    assertEquals(1, options.getMinNumTerms());
    
    options.setMinNumTerms(10);
    assertEquals(10, options.getMinNumTerms());
  }
  
  @Test
  public void testGetSetMaxNumTerms() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();
    assertEquals(20, options.getMaxNumTerms());
    
    options.setMaxNumTerms(30);
    assertEquals(30, options.getMaxNumTerms());
  }
  
  @Test
  public void testGetSetSeed() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();
    assertEquals(42, options.getSeed());
    
    options.setSeed(999);
    assertEquals(999, options.getSeed());
  }
  
  @Test
  public void testGetSetNumIterations() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();
    assertEquals(100000, options.getNumIterations());
    
    options.setNumIterations(75000);
    assertEquals(75000, options.getNumIterations());
  }
  
  @Test
  public void testClone() throws CloneNotSupportedException {
    ScoreSamplingOptions original = new ScoreSamplingOptions(3, 5, 15, 2, 10, 100, 5000);
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
    ScoreSamplingOptions options = new ScoreSamplingOptions(2, 10, 20, 5, 15, 42, 10000);
    String result = options.toString();
    
    assertNotNull(result);
    // Check that all fields are present in the string representation
    assertEquals(true, result.contains("numThreads=2"));
    assertEquals(true, result.contains("minObjectId=10"));
    assertEquals(true, result.contains("maxObjectId=20"));
    assertEquals(true, result.contains("minNumTerms=5"));
    assertEquals(true, result.contains("maxNumTerms=15"));
    assertEquals(true, result.contains("numIterations=10000"));
    assertEquals(true, result.contains("seed=42"));
  }
}
