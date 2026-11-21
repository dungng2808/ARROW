package com.hello;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class LeaderBoard_ArrowTest {

    @Test
    public void ranksPlayersAgainstDescendingScores() {
        int[] scores = {100, 100, 50, 40, 40, 20, 10};
        int[] alice = {5, 25, 50, 120};

        assertArrayEquals(new int[]{6, 4, 2, 1}, LeaderBoard.getRanks(scores, alice));
    }

    @Test
    public void ranksEqualScoresTogether() {
        int[] scores = {100, 90, 90, 80};
        int[] alice = {90, 95, 80};

        assertArrayEquals(new int[]{2, 2, 4}, LeaderBoard.getRanks(scores, alice));
    }

    @Test
    public void handlesEmptyScores() {
        int[] scores = {};
        int[] alice = {10, 0};

        assertArrayEquals(new int[]{1, 1}, LeaderBoard.getRanks(scores, alice));
    }

    @Test
    public void handlesEmptyAliceScores() {
        int[] scores = {100, 50};
        int[] alice = {};

        assertArrayEquals(new int[]{}, LeaderBoard.getRanks(scores, alice));
    }

    @Test
    public void playerGettersAndSetters() {
        LeaderBoard.Player player = new LeaderBoard.Player(42, 3);

        assertEquals(42, player.getScore());
        assertEquals(3, player.getRank());

        player.setScore(55);
        player.setRank(2);

        assertEquals(55, player.getScore());
        assertEquals(2, player.getRank());
    }

    @Test
    public void comparesPlayersByDescendingScore() {
        LeaderBoard.Player higher = new LeaderBoard.Player(100, 1);
        LeaderBoard.Player lower = new LeaderBoard.Player(50, 2);

        assertEquals(-50, higher.compareTo(lower));
        assertEquals(50, lower.compareTo(higher));
    }
}
