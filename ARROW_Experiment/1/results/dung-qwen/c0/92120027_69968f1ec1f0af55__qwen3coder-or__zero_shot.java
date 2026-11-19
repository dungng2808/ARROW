package com.hello;

import org.junit.Test;
import static org.junit.Assert.*;

public class LeaderBoard_ArrowTest {

    @Test
    public void testGetRanks_NormalCase() {
        int[] scores = {100, 100, 50, 40, 40, 20, 10};
        int[] alice = {5, 25, 50, 120};
        int[] expected = {6, 4, 2, 1};
        
        int[] actual = LeaderBoard.getRanks(scores, alice);
        
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetRanks_EmptyAlice() {
        int[] scores = {100, 100, 50, 40, 40, 20, 10};
        int[] alice = {};
        int[] expected = {};
        
        int[] actual = LeaderBoard.getRanks(scores, alice);
        
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetRanks_EmptyScores() {
        int[] scores = {};
        int[] alice = {5, 25, 50, 120};
        int[] expected = {1, 1, 1, 1};
        
        int[] actual = LeaderBoard.getRanks(scores, alice);
        
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetRanks_SingleScore() {
        int[] scores = {50};
        int[] alice = {25, 50, 75};
        int[] expected = {2, 1, 1};
        
        int[] actual = LeaderBoard.getRanks(scores, alice);
        
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetRanks_AllSameScores() {
        int[] scores = {50, 50, 50, 50};
        int[] alice = {25, 50, 75};
        int[] expected = {2, 1, 1};
        
        int[] actual = LeaderBoard.getRanks(scores, alice);
        
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetRanks_AliceScoresLowerThanAll() {
        int[] scores = {100, 50, 25};
        int[] alice = {10, 15, 20};
        int[] expected = {4, 4, 4};
        
        int[] actual = LeaderBoard.getRanks(scores, alice);
        
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetRanks_AliceScoresHigherThanAll() {
        int[] scores = {10, 20, 30};
        int[] alice = {40, 50, 60};
        int[] expected = {1, 1, 1};
        
        int[] actual = LeaderBoard.getRanks(scores, alice);
        
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testPlayerCompareTo() {
        LeaderBoard.Player p1 = new LeaderBoard.Player(100, 1);
        LeaderBoard.Player p2 = new LeaderBoard.Player(50, 2);
        LeaderBoard.Player p3 = new LeaderBoard.Player(100, 3);
        
        assertTrue(p1.compareTo(p2) > 0);
        assertTrue(p2.compareTo(p1) < 0);
        assertEquals(0, p1.compareTo(p3));
    }
}
