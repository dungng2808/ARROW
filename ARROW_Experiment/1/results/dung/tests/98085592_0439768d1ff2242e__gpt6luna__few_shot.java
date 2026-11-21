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
        new ScoreSamplingOptions(3, 10, 50, 2, 12, 99, 500);

    assertEquals(3, options.getNumThreads());
    assertEquals(Integer.valueOf(10), options.getMinObjectId());
    assertEquals(Integer.valueOf(50), options.getMaxObjectId());
    assertEquals(2, options.getMinNumTerms());
    assertEquals(12, options.getMaxNumTerms());
    assertEquals(500, options.getNumIterations());
    assertEquals(99, options.getSeed());
  }

  @Test
  public void settersUpdateValues() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();
    options.setNumThreads(4);
    options.setMinObjectId(7);
    options.setMaxObjectId(21);
    options.setMinNumTerms(3);
    options.setMaxNumTerms(15);
    options.setSeed(123);
    options.setNumIterations(800);

    assertEquals(4, options.getNumThreads());
    assertEquals(Integer.valueOf(7), options.getMinObjectId());
    assertEquals(Integer.valueOf(21), options.getMaxObjectId());
    assertEquals(3, options.getMinNumTerms());
    assertEquals(15, options.getMaxNumTerms());
    assertEquals(123, options.getSeed());
    assertEquals(800, options.getNumIterations());
  }

  @Test
  public void objectIdSettersAcceptNull() {
    ScoreSamplingOptions options = new ScoreSamplingOptions(1, 2, 3, 1, 20, 42, 100000);
    options.setMinObjectId(null);
    options.setMaxObjectId(null);

    assertNull(options.getMinObjectId());
    assertNull(options.getMaxObjectId());
  }

  @Test
  public void cloneCopiesValuesIntoDistinctObject() throws CloneNotSupportedException {
    ScoreSamplingOptions options =
        new ScoreSamplingOptions(2, 5, 9, 4, 11, 17, 250);

    ScoreSamplingOptions copy = (ScoreSamplingOptions) options.clone();

    assertNotSame(options, copy);
    assertEquals(2, copy.getNumThreads());
    assertEquals(Integer.valueOf(5), copy.getMinObjectId());
    assertEquals(Integer.valueOf(9), copy.getMaxObjectId());
    assertEquals(4, copy.getMinNumTerms());
    assertEquals(11, copy.getMaxNumTerms());
    assertEquals(17, copy.getSeed());
    assertEquals(250, copy.getNumIterations());
  }

  @Test
  public void toStringIncludesAllOptionValues() {
    ScoreSamplingOptions options =
        new ScoreSamplingOptions(2, 5, 9, 4, 11, 17, 250);

    assertEquals(
        "ScoreSamplingOptions [numThreads=2, minObjectId=5, maxObjectId=9, minNumTerms=4, "
            + "maxNumTerms=11, numIterations=250, seed=17]",
        options.toString());
  }

  @Test
  public void toStringShowsNullObjectBounds() {
    ScoreSamplingOptions options = new ScoreSamplingOptions();

    assertTrue(options.toString().contains("minObjectId=null, maxObjectId=null"));
  }
}
