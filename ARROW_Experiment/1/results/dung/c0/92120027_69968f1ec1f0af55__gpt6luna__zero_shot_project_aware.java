package com.hello;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class LeaderBoard_ArrowTest {

    @Test
    public void getRanksForDescendingScoresAndDuplicates() {
        int[] scores = {100, 100, 50, 40, 40, 20, 10};
        int[] alice = {5, 25, 50, 120};

        assertArrayEquals(new int[]{6, 4, 2, 1}, LeaderBoard.getRanks(scores, alice));
    }

    @Test
    public void getRanksForScoresBetweenExistingRanks() {
        int[] scores = {100, 90, 80};
        int[] alice = {95, 85, 75};

        assertArrayEquals(new int[]{2, 3, 4}, LeaderBoard.getRanks(scores, alice));
    }

    @Test
    public void getRanksForEmptyLeaderboard() {
        assertArrayEquals(new int[]{1, 1}, LeaderBoard.getRanks(new int[0], new int[]{10, 20}));
    }

    @Test
    public void getRanksForEmptyAliceScores() {
        assertArrayEquals(new int[0], LeaderBoard.getRanks(new int[]{100, 50}, new int[0]));
    }

    @Test
    public void playerGettersAndSetters() {
        LeaderBoard.Player player = new LeaderBoard.Player(10, 2);

        assertEquals(10, player.getScore());
        assertEquals(2, player.getRank());

        player.setScore(20);
        player.setRank(3);

        assertEquals(20, player.getScore());
        assertEquals(3, player.getRank());
    }

    @Test
    public void playerComparisonOrdersHigherScoresFirst() {
        LeaderBoard.Player higher = new LeaderBoard.Player(100, 1);
        LeaderBoard.Player lower = new LeaderBoard.Player(50, 2);

        assertEquals(-50, higher.compareTo(lower));
        assertEquals(50, lower.compareTo(higher));
    }
}
