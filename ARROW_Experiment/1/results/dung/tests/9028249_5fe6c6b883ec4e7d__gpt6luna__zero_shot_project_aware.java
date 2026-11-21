package com.xuan.mix.bt.diverter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Diverter_ArrowTest {

    @Test
    public void hitNumReturnsBucketWhenWithinPercent() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(10);
        config.setTotal(100);

        Diverter diverter = new Diverter(config);

        assertEquals(3L, diverter.hitNum(103L));
    }

    @Test
    public void hitNumReturnsMissWhenOutsidePercent() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(10);
        config.setTotal(100);

        Diverter diverter = new Diverter(config);

        assertEquals(-1L, diverter.hitNum(123L));
    }

    @Test
    public void isHitReturnsTrueForMatchingBucket() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(10);
        config.setTotal(100);

        Diverter diverter = new Diverter(config);

        assertTrue(diverter.isHit(103L));
    }

    @Test
    public void isHitReturnsFalseForNonmatchingBucket() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(10);
        config.setTotal(100);

        Diverter diverter = new Diverter(config);

        assertFalse(diverter.isHit(123L));
    }

    @Test
    public void hitNumReturnsMissWhenPercentIsNotPositive() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(0);
        config.setTotal(100);

        Diverter diverter = new Diverter(config);

        assertEquals(-1L, diverter.hitNum(3L));
    }

    @Test
    public void hitNumReturnsMissWhenTotalIsNotPositive() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(10);
        config.setTotal(0);

        Diverter diverter = new Diverter(config);

        assertEquals(-1L, diverter.hitNum(3L));
    }

    @Test
    public void hitNumReturnsAbsoluteBucketForNegativeBusinessId() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(10);
        config.setTotal(100);

        Diverter diverter = new Diverter(config);

        assertEquals(3L, diverter.hitNum(-103L));
    }

    @Test
    public void blackListTakesPrecedenceOverWhiteList() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(10);
        config.setTotal(100);

        Set<String> blackSet = new HashSet<String>();
        blackSet.add("103");
        config.setBlackSet(blackSet);

        Set<String> whiteSet = new HashSet<String>();
        whiteSet.add("103");
        config.setWhiteSet(whiteSet);

        Diverter diverter = new Diverter(config);

        assertEquals(-1L, diverter.hitNum(103L));
    }

    @Test
    public void whiteListReturnsZeroEvenWhenBucketWouldMiss() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(10);
        config.setTotal(100);

        Set<String> whiteSet = new HashSet<String>();
        whiteSet.add("123");
        config.setWhiteSet(whiteSet);

        Diverter diverter = new Diverter(config);

        assertEquals(0L, diverter.hitNum(123L));
    }

    @Test
    public void emptyListsDoNotOverrideBucketCalculation() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(10);
        config.setTotal(100);
        config.setBlackSet(Collections.<String>emptySet());
        config.setWhiteSet(Collections.<String>emptySet());

        Diverter diverter = new Diverter(config);

        assertEquals(3L, diverter.hitNum(103L));
    }
}
