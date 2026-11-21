package com.xuan.mix.bt.diverter;

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
    public void hitNumUsesAbsoluteValueForNegativeBucket() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(10);
        config.setTotal(100);

        Diverter diverter = new Diverter(config);

        assertEquals(3L, diverter.hitNum(-103L));
    }

    @Test
    public void hitNumBlackListTakesPrecedenceOverWhiteList() {
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
    public void hitNumWhiteListHitsEvenWhenPercentIsInvalid() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(0);
        config.setTotal(0);

        Set<String> whiteSet = new HashSet<String>();
        whiteSet.add("123");
        config.setWhiteSet(whiteSet);

        Diverter diverter = new Diverter(config);

        assertEquals(0L, diverter.hitNum(123L));
    }

    @Test
    public void hitNumReturnsMissWhenPercentIsNonPositive() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(0);
        config.setTotal(100);

        Diverter diverter = new Diverter(config);

        assertEquals(-1L, diverter.hitNum(103L));
    }

    @Test
    public void hitNumReturnsMissWhenTotalIsNonPositive() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(10);
        config.setTotal(0);

        Diverter diverter = new Diverter(config);

        assertEquals(-1L, diverter.hitNum(103L));
    }

    @Test
    public void isHitReturnsTrueForHitBucket() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(10);
        config.setTotal(100);

        Diverter diverter = new Diverter(config);

        assertTrue(diverter.isHit(103L));
    }

    @Test
    public void isHitReturnsFalseForMissedBucket() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(10);
        config.setTotal(100);

        Diverter diverter = new Diverter(config);

        assertFalse(diverter.isHit(123L));
    }
}
