package com.hello;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class LeaderBoard_ArrowTest {

    @Test
    public void getRanksForDescendingScoresAndTies() {
        int[] scores = {100, 100, 50, 40, 40, 20, 10};
        int[] alice = {5, 25, 50, 120};

        int[] ranks = LeaderBoard.getRanks(scores, alice);

        assertArrayEquals(new int[]{6, 4, 2, 1}, ranks);
    }

    @Test
    public void getRanksForEmptyLeaderboard() {
        int[] ranks = LeaderBoard.getRanks(new int[0], new int[]{10, 0});

        assertArrayEquals(new int[]{1, 1}, ranks);
    }

    @Test
    public void getRanksForEmptyAliceScores() {
        int[] ranks = LeaderBoard.getRanks(new int[]{100, 50}, new int[0]);

        assertArrayEquals(new int[0], ranks);
    }

    @Test
    public void playerGettersAndSetters() {
        LeaderBoard.Player player = new LeaderBoard.Player(42, 3);

        assertEquals(42, player.getScore());
        assertEquals(3, player.getRank());

        player.setScore(50);
        player.setRank(2);

        assertEquals(50, player.getScore());
        assertEquals(2, player.getRank());
    }

    @Test
    public void playerComparisonOrdersHigherScoresFirst() {
        LeaderBoard.Player higher = new LeaderBoard.Player(100, 1);
        LeaderBoard.Player lower = new LeaderBoard.Player(50, 2);

        assertEquals(-50, higher.compareTo(lower));
        assertEquals(50, lower.compareTo(higher));
    }
}
