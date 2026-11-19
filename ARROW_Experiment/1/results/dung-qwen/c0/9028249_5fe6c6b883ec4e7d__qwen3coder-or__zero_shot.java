package com.xuan.mix.bt.diverter;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

public class Diverter_ArrowTest {

    @Test
    public void testIsHit_True() {
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
    public void testIsHit_False() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        
        Set<String> blackSet = new HashSet<>();
        blackSet.add("123");
        config.setBlackSet(blackSet);
        
        Diverter diverter = new Diverter(config);
        assertFalse(diverter.isHit(123));
    }

    @Test
    public void testHitNum_BlackList() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        
        Set<String> blackSet = new HashSet<>();
        blackSet.add("123");
        config.setBlackSet(blackSet);
        
        Diverter diverter = new Diverter(config);
        assertEquals(-1, diverter.hitNum(123));
    }

    @Test
    public void testHitNum_WhiteList() {
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
    public void testHitNum_NormalCase() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        
        Diverter diverter = new Diverter(config);
        assertEquals(3, diverter.hitNum(103));
    }

    @Test
    public void testHitNum_NotHit() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        
        Diverter diverter = new Diverter(config);
        assertEquals(-1, diverter.hitNum(123));
    }

    @Test
    public void testHitNum_NegativeBucket() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        
        Diverter diverter = new Diverter(config);
        assertEquals(3, diverter.hitNum(-97));
    }

    @Test
    public void testHitNum_InvalidConfig() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(0);
        config.setPercent(10);
        
        Diverter diverter = new Diverter(config);
        assertEquals(-1, diverter.hitNum(103));
    }

    @Test
    public void testHitNum_InvalidConfigNegativePercent() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(-1);
        
        Diverter diverter = new Diverter(config);
        assertEquals(-1, diverter.hitNum(103));
    }

    @Test
    public void testHitNum_InvalidConfigNegativeTotal() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(-1);
        config.setPercent(10);
        
        Diverter diverter = new Diverter(config);
        assertEquals(-1, diverter.hitNum(103));
    }

    @Test
    public void testHitNum_EmptyBlackAndWhiteSet() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        
        Diverter diverter = new Diverter(config);
        assertEquals(3, diverter.hitNum(103));
    }

    @Test
    public void testHitNum_WhiteSetNull() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        config.setWhiteSet(null);
        
        Diverter diverter = new Diverter(config);
        assertEquals(3, diverter.hitNum(103));
    }

    @Test
    public void testHitNum_BlackSetNull() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        config.setBlackSet(null);
        
        Diverter diverter = new Diverter(config);
        assertEquals(3, diverter.hitNum(103));
    }
}
