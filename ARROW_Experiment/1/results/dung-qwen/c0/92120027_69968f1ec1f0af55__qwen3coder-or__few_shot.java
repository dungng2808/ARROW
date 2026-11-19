package com.hello;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;

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
    public void testGetRanks_SameScores() {
        int[] scores = {50, 50, 50, 50};
        int[] alice = {40, 50, 60};
        int[] expected = {4, 1, 1};
        int[] actual = LeaderBoard.getRanks(scores, alice);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetRanks_BoundaryValues() {
        int[] scores = {1000, 100, 10};
        int[] alice = {0, 1000, 1001};
        int[] expected = {3, 1, 1};
        int[] actual = LeaderBoard.getRanks(scores, alice);
        assertArrayEquals(expected, actual);
    }
}
