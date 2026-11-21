package com.github.phenomics.ontolib.ontology.scoredist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

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
    Integer minObjId = 5;
    Integer maxObjId = 15;
    ScoreSamplingOptions options = new ScoreSamplingOptions(3, minObjId, maxObjId, 2, 10, 100, 50000);
    
    assertEquals(3, options.getNumThreads());
    assertSame(minObjId, options.getMinObjectId());
    assertSame(maxObjId, options.getMaxObjectId());
    assertEquals(2, options.getMinNumTerms());
    assertEquals(10, options.getMaxNumTerms());
    assertEquals(50000, options.getNumIterations());
    assertEquals(100, options.getSeed());
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
    
    Integer value = 10;
    options.setMinObjectId(value);
    assertSame(value, options.getMinObjectId());
  }
  
  @Test
  public void testGetSetMaxObjectId() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();
    assertNull(options.getMaxObjectId());
    
    Integer value = 20;
    options.setMaxObjectId(value);
    assertSame(value, options.getMaxObjectId());
  }
  
  @Test
  public void testGetSetMinNumTerms() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();
    assertEquals(1, options.getMinNumTerms());
    
    options.setMinNumTerms(5);
    assertEquals(5, options.getMinNumTerms());
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
    
    options.setSeed(1000);
    assertEquals(1000, options.getSeed());
  }
  
  @Test
  public void testGetSetNumIterations() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();
    assertEquals(100000, options.getNumIterations());
    
    options.setNumIterations(200000);
    assertEquals(200000, options.getNumIterations());
  }
  
  @Test
  public void testClone() throws CloneNotSupportedException {
    ScoreSamplingOptions original = new ScoreSamplingOptions(3, 5, 15, 2, 10, 100, 50000);
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
    ScoreSamplingOptions options = new ScoreSamplingOptions(2, 1, 10, 3, 15, 50, 75000);
    String result = options.toString();
    
    assertEquals("ScoreSamplingOptions [numThreads=2, minObjectId=1, maxObjectId=10, minNumTerms=3, maxNumTerms=15, numIterations=75000, seed=50]", result);
  }
}
