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
        Diverter diverter = new Diverter(config);
        
        // bizId = 103, should return true
        assertTrue(diverter.isHit(103L));
    }

    @Test
    public void testIsHit_False() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        Diverter diverter = new Diverter(config);
        
        // bizId = 123, should return false
        assertFalse(diverter.isHit(123L));
    }

    @Test
    public void testHitNum_Positive() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        Diverter diverter = new Diverter(config);
        
        // bizId = 103, should return 3
        assertEquals(3L, diverter.hitNum(103L));
    }

    @Test
    public void testHitNum_Negative() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        Diverter diverter = new Diverter(config);
        
        // bizId = 123, should return -1
        assertEquals(-1L, diverter.hitNum(123L));
    }

    @Test
    public void testHitNum_BlackList() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        Set<String> blackSet = new HashSet<>();
        blackSet.add("103");
        config.setBlackSet(blackSet);
        Diverter diverter = new Diverter(config);
        
        // bizId = 103, should return -1 because it's in blacklist
        assertEquals(-1L, diverter.hitNum(103L));
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
        
        // bizId = 103, should return 0 because it's in whitelist
        assertEquals(0L, diverter.hitNum(103L));
    }

    @Test
    public void testHitNum_InvalidConfig() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(0);
        config.setPercent(10);
        Diverter diverter = new Diverter(config);
        
        // Invalid config, should return -1
        assertEquals(-1L, diverter.hitNum(103L));
    }

    @Test
    public void testHitNum_NegativeBizId() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        Diverter diverter = new Diverter(config);
        
        // bizId = -103, should return 97
        assertEquals(97L, diverter.hitNum(-103L));
    }

    @Test
    public void testHitNum_ZeroPercent() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(0);
        Diverter diverter = new Diverter(config);
        
        // Zero percent, should return -1
        assertEquals(-1L, diverter.hitNum(103L));
    }

    @Test
    public void testHitNum_ZeroTotal() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(0);
        config.setPercent(10);
        Diverter diverter = new Diverter(config);
        
        // Zero total, should return -1
        assertEquals(-1L, diverter.hitNum(103L));
    }

    @Test
    public void testIsHit_BlackList() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        Set<String> blackSet = new HashSet<>();
        blackSet.add("103");
        config.setBlackSet(blackSet);
        Diverter diverter = new Diverter(config);
        
        // bizId = 103, should return false because it's in blacklist
        assertFalse(diverter.isHit(103L));
    }

    @Test
    public void testIsHit_WhiteList() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        Set<String> whiteSet = new HashSet<>();
        whiteSet.add("103");
        config.setWhiteSet(whiteSet);
        Diverter diverter = new Diverter(config);
        
        // bizId = 103, should return true because it's in whitelist
        assertTrue(diverter.isHit(103L));
    }

    @Test
    public void testIsHit_InvalidConfig() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(0);
        config.setPercent(10);
        Diverter diverter = new Diverter(config);
        
        // Invalid config, should return false
        assertFalse(diverter.isHit(103L));
    }

    @Test
    public void testIsHit_NegativeBizId() {
        DiverterConfig config = new DiverterConfig();
        config.setTotal(100);
        config.setPercent(10);
        Diverter diverter = new Diverter(config);
        
        // bizId = -103, should return true because bucket = 97 >= 10 is false, but we're checking hitNum
        // Actually, -103 % 100 = -3, abs(-3) = 3, 3 < 10, so it should return 3, which is >= 0, so true
        assertTrue(diverter.isHit(-103L));
    }
}
