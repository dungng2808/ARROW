package com.github.phenomics.ontolib.ontology.scoredist;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ScoreSamplingOptions_ArrowTest {

  @Test
  public void defaultConstructorSetsDefaults() {
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
  public void parameterizedConstructorSetsValues() {
    ScoreSamplingOptions options =
        new ScoreSamplingOptions(4, 10, 30, 2, 15, 99, 500);

    assertEquals(4, options.getNumThreads());
    assertEquals(Integer.valueOf(10), options.getMinObjectId());
    assertEquals(Integer.valueOf(30), options.getMaxObjectId());
    assertEquals(2, options.getMinNumTerms());
    assertEquals(15, options.getMaxNumTerms());
    assertEquals(500, options.getNumIterations());
    assertEquals(99, options.getSeed());
  }

  @Test
  public void settersUpdateValues() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();

    options.setNumThreads(3);
    options.setMinObjectId(7);
    options.setMaxObjectId(12);
    options.setMinNumTerms(4);
    options.setMaxNumTerms(18);
    options.setSeed(123);
    options.setNumIterations(250);

    assertEquals(3, options.getNumThreads());
    assertEquals(Integer.valueOf(7), options.getMinObjectId());
    assertEquals(Integer.valueOf(12), options.getMaxObjectId());
    assertEquals(4, options.getMinNumTerms());
    assertEquals(18, options.getMaxNumTerms());
    assertEquals(123, options.getSeed());
    assertEquals(250, options.getNumIterations());
  }

  @Test
  public void objectIdSettersAcceptNull() {
    ScoreSamplingOptions options = new ScoreSamplingOptions(1, 5, 9, 1, 20, 42, 100);

    options.setMinObjectId(null);
    options.setMaxObjectId(null);

    assertNull(options.getMinObjectId());
    assertNull(options.getMaxObjectId());
  }

  @Test
  public void cloneCopiesAllValuesIntoDistinctObject() throws CloneNotSupportedException {
    ScoreSamplingOptions options =
        new ScoreSamplingOptions(4, 10, 30, 2, 15, 99, 500);

    ScoreSamplingOptions copy = (ScoreSamplingOptions) options.clone();

    assertNotSame(options, copy);
    assertEquals(4, copy.getNumThreads());
    assertEquals(Integer.valueOf(10), copy.getMinObjectId());
    assertEquals(Integer.valueOf(30), copy.getMaxObjectId());
    assertEquals(2, copy.getMinNumTerms());
    assertEquals(15, copy.getMaxNumTerms());
    assertEquals(500, copy.getNumIterations());
    assertEquals(99, copy.getSeed());
  }

  @Test
  public void toStringIncludesAllValues() {
    ScoreSamplingOptions options =
        new ScoreSamplingOptions(4, 10, 30, 2, 15, 99, 500);

    assertEquals(
        "ScoreSamplingOptions [numThreads=4, minObjectId=10, maxObjectId=30, minNumTerms=2, "
            + "maxNumTerms=15, numIterations=500, seed=99]",
        options.toString());
  }

  @Test
  public void toStringShowsNullObjectBounds() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();

    assertTrue(options.toString().contains("minObjectId=null, maxObjectId=null"));
  }
}
