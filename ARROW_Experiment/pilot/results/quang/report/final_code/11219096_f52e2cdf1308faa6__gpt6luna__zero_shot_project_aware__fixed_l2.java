package org.jskat.control.iss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.jskat.data.SkatGameData;
import org.jskat.data.iss.ChatMessage;
import org.jskat.data.iss.GameStartInformation;
import org.jskat.data.iss.MoveInformation;
import org.jskat.data.iss.MovePlayer;
import org.jskat.data.iss.MoveType;
import org.jskat.data.iss.TablePanelStatus;
import org.jskat.util.Player;
import org.junit.jupiter.api.Test;

public class MessageParser_ArrowTest {

	@Test
	public void parsesGameStartInformation() {
		GameStartInformation result = MessageParser.getGameStartStatus("login",
				Arrays.asList("7", "Alice", "1.5", "Bob", "2.5", "Carol", "3.5"));

		assertEquals("login", result.getLoginName());
		assertEquals(7, result.getGameNo());
		assertEquals("Alice", result.getPlayerName(Player.FOREHAND));
		assertEquals(1.5, result.getPlayerTime(Player.FOREHAND), 0.0001);
		assertEquals("Bob", result.getPlayerName(Player.MIDDLEHAND));
		assertEquals(2.5, result.getPlayerTime(Player.MIDDLEHAND), 0.0001);
		assertEquals("Carol", result.getPlayerName(Player.REARHAND));
		assertEquals(3.5, result.getPlayerTime(Player.REARHAND), 0.0001);
	}

	@Test
	public void parsesMovePlayerAndPassMove() {
		MoveInformation result = MessageParser.getMoveInformation(
				Arrays.asList("1", "p"));

		assertEquals(MovePlayer.MIDDLEHAND, result.getMovePlayer());
		assertEquals(MoveType.PASS, result.getType());
	}

	@Test
	public void parsesBidMove() {
		MoveInformation result = MessageParser.getMoveInformation(
				Arrays.asList("0", "18"));

		assertEquals(MovePlayer.FOREHAND, result.getMovePlayer());
		assertEquals(MoveType.BID, result.getType());
		assertEquals(18, result.getBidValue());
	}

	@Test
	public void parsesTimeoutMove() {
		MoveInformation result = MessageParser.getMoveInformation(
				Arrays.asList("w", "TI.2"));

		assertEquals(MovePlayer.WORLD, result.getMovePlayer());
		assertEquals(MoveType.TIME_OUT, result.getType());
		assertEquals(Player.REARHAND, result.getTimeOutPlayer());
	}

	@Test
	public void parsesPlayerTimes() {
		MoveInformation result = new MoveInformation();
		MessageParser.parsePlayerTimes(Arrays.asList("unused", "1.25", "2.5", "3.75"), result);

		assertEquals(1.25, result.getPlayerTime(Player.FOREHAND), 0.0001);
		assertEquals(2.5, result.getPlayerTime(Player.MIDDLEHAND), 0.0001);
		assertEquals(3.75, result.getPlayerTime(Player.REARHAND), 0.0001);
	}

	@Test
	public void parsesEmptyGameSummary() {
		SkatGameData result = MessageParser.parseGameSummary("");

		assertNotNull(result);
		assertTrue(result.getTricks().isEmpty());
	}

	@Test
	public void createsTableChatMessage() {
		ChatMessage result = MessageParser.getTableChatMessage("table1",
				Arrays.asList("Alice", "hello", "world"));

		assertEquals("table1", result.getChatName());
		assertEquals("Alice: hello world ", result.getMessage());
	}

	@Test
	public void parsesTableStatusWithoutPlayers() {
		TablePanelStatus result = MessageParser.getTableStatus("login",
				Collections.singletonList("0"));

		assertEquals("login", result.getLoginName());
		assertEquals(0, result.getMaxPlayers());
	}
}
