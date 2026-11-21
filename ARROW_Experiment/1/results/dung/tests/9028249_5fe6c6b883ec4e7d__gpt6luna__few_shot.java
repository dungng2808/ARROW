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
    public void hitNumReturnsBucketForMatchingId() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(10);
        config.setTotal(100);

        Diverter diverter = new Diverter(config);

        assertEquals(3L, diverter.hitNum(103L));
    }

    @Test
    public void isHitReturnsFalseWhenIdIsOutsideConfiguredPercent() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(10);
        config.setTotal(100);

        Diverter diverter = new Diverter(config);

        assertFalse(diverter.isHit(123L));
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
    public void whiteListForcesHit() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(0);
        config.setTotal(0);
        config.setWhiteSet(Collections.singleton("123"));

        Diverter diverter = new Diverter(config);

        assertEquals(0L, diverter.hitNum(123L));
        assertTrue(diverter.isHit(123L));
    }

    @Test
    public void invalidPercentReturnsMiss() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(0);
        config.setTotal(100);

        Diverter diverter = new Diverter(config);

        assertEquals(-1L, diverter.hitNum(3L));
    }

    @Test
    public void invalidTotalReturnsMiss() {
        DiverterConfig config = new DiverterConfig();
        config.setPercent(10);
        config.setTotal(0);

        Diverter diverter = new Diverter(config);

        assertEquals(-1L, diverter.hitNum(3L));
    }
}
