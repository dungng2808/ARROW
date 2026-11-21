package com.github.phenomics.ontolib.ontology.scoredist;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

public class ScoreSamplingOptions_ArrowTest {

  @Test
  public void defaultConstructorSetsDefaults() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();

    assertEquals(1, options.getNumThreads());
    assertNull(options.getMinObjectId());
    assertNull(options.getMaxObjectId());
    assertEquals(1, options.getMinNumTerms());
    assertEquals(20, options.getMaxNumTerms());
    assertEquals(42, options.getSeed());
    assertEquals(100000, options.getNumIterations());
  }

  @Test
  public void parameterizedConstructorSetsValues() {
    ScoreSamplingOptions options =
        new ScoreSamplingOptions(3, 10, 50, 2, 12, 7, 500);

    assertEquals(3, options.getNumThreads());
    assertEquals(Integer.valueOf(10), options.getMinObjectId());
    assertEquals(Integer.valueOf(50), options.getMaxObjectId());
    assertEquals(2, options.getMinNumTerms());
    assertEquals(12, options.getMaxNumTerms());
    assertEquals(7, options.getSeed());
    assertEquals(500, options.getNumIterations());
  }

  @Test
  public void settersUpdateValues() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();

    options.setNumThreads(4);
    options.setMinObjectId(11);
    options.setMaxObjectId(99);
    options.setMinNumTerms(3);
    options.setMaxNumTerms(15);
    options.setSeed(123);
    options.setNumIterations(250);

    assertEquals(4, options.getNumThreads());
    assertEquals(Integer.valueOf(11), options.getMinObjectId());
    assertEquals(Integer.valueOf(99), options.getMaxObjectId());
    assertEquals(3, options.getMinNumTerms());
    assertEquals(15, options.getMaxNumTerms());
    assertEquals(123, options.getSeed());
    assertEquals(250, options.getNumIterations());
  }

  @Test
  public void objectIdSettersAcceptNull() {
    ScoreSamplingOptions options = new ScoreSamplingOptions(1, 10, 20, 1, 20, 42, 100000);

    options.setMinObjectId(null);
    options.setMaxObjectId(null);

    assertNull(options.getMinObjectId());
    assertNull(options.getMaxObjectId());
  }

  @Test
  public void cloneCopiesValuesIntoDistinctObject() throws CloneNotSupportedException {
    ScoreSamplingOptions options =
        new ScoreSamplingOptions(2, 5, 30, 4, 9, 17, 800);

    ScoreSamplingOptions copy = (ScoreSamplingOptions) options.clone();

    assertNotSame(options, copy);
    assertEquals(2, copy.getNumThreads());
    assertEquals(Integer.valueOf(5), copy.getMinObjectId());
    assertEquals(Integer.valueOf(30), copy.getMaxObjectId());
    assertEquals(4, copy.getMinNumTerms());
    assertEquals(9, copy.getMaxNumTerms());
    assertEquals(17, copy.getSeed());
    assertEquals(800, copy.getNumIterations());
  }

  @Test
  public void toStringReturnsFormattedOptions() {
    ScoreSamplingOptions options =
        new ScoreSamplingOptions(2, 5, 30, 4, 9, 17, 800);

    assertEquals(
        "ScoreSamplingOptions [numThreads=2, minObjectId=5, maxObjectId=30, minNumTerms=4, "
            + "maxNumTerms=9, numIterations=800, seed=17]",
        options.toString());
  }
}
