package com.xuan.mix.bt.diverter;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class Diverter_ArrowTest {

    @Test
    public void testIsHitTrue() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        Set<String> whiteSet = new HashSet<>();
        whiteSet.add("103");
        config.setWhiteSet(whiteSet);
        Diverter diverter = new Diverter(config);
        assertTrue(diverter.isHit(103));
    }

    @Test
    public void testIsHitFalse() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        Diverter diverter = new Diverter(config);
        assertFalse(diverter.isHit(123));
    }

    @Test
    public void testHitNumBlackList() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        Set<String> blackSet = new HashSet<>();
        blackSet.add("103");
        config.setBlackSet(blackSet);
        Diverter diverter = new Diverter(config);
        assertEquals(-1, diverter.hitNum(103));
    }

    @Test
    public void testHitNumWhiteList() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        Set<String> whiteSet = new HashSet<>();
        whiteSet.add("103");
        config.setWhiteSet(whiteSet);
        Diverter diverter = new Diverter(config);
        assertEquals(0, diverter.hitNum(103));
    }

    @Test
    public void testHitNumInvalidConfig() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(-1);
        config.setPercent(10);
        Diverter diverter = new Diverter(config);
        assertEquals(-1, diverter.hitNum(103));
    }

    @Test
    public void testHitNumHit() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        Diverter diverter = new Diverter(config);
        assertEquals(3, diverter.hitNum(103));
    }

    @Test
    public void testHitNumMiss() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        Diverter diverter = new Diverter(config);
        assertEquals(-1, diverter.hitNum(123));
    }

    @Test
    public void testHitNumNegativeBizId() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        Diverter diverter = new Diverter(config);
        assertEquals(3, diverter.hitNum(-103));
    }

    @Test
    public void testHitNumZeroPercent() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(0);
        Diverter diverter = new Diverter(config);
        assertEquals(-1, diverter.hitNum(103));
    }

    @Test
    public void testHitNumZeroTotal() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(0);
        config.setPercent(10);
        Diverter diverter = new Diverter(config);
        assertEquals(-1, diverter.hitNum(103));
    }
}
