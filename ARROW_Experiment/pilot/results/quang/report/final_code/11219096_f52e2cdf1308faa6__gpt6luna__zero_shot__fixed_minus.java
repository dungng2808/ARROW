package org.jskat.control.iss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.jskat.data.SkatGameData;
import org.jskat.data.iss.GameStartInformation;
import org.jskat.data.iss.MoveInformation;
import org.jskat.data.iss.MoveType;
import org.jskat.util.Player;
import org.junit.jupiter.api.Test;

public class MessageParser_ArrowTest {

	@Test
	public void parsesGameStartStatus() {
		List<String> params = Arrays.asList("42", "Alice", "1.5", "Bob", "2.5",
				"Carol", "3.5");

		GameStartInformation result = MessageParser.getGameStartStatus("Alice",
				params);

		assertNotNull(result);
		assertEquals("Alice", result.getLoginName());
		assertEquals(42, result.getGameNo());
		assertEquals("Alice", result.getPlayerName(Player.FOREHAND));
		assertEquals(1.5, result.getPlayerTime(Player.FOREHAND), 0.1);
		assertEquals("Bob", result.getPlayerName(Player.MIDDLEHAND));
		assertEquals(2.5, result.getPlayerTime(Player.MIDDLEHAND), 0.1);
		assertEquals("Carol", result.getPlayerName(Player.REARHAND));
		assertEquals(3.5, result.getPlayerTime(Player.REARHAND), 0.1);
	}

	@Test
	public void parsesHoldBidMove() {
		MoveInformation result = MessageParser.getMoveInformation(
				Arrays.asList("1", "y"));

		assertEquals(Player.MIDDLEHAND, result.getPlayer());
		assertEquals(MoveType.HOLD_BID, result.getType());
	}

	@Test
	public void parsesPassMove() {
		MoveInformation result = MessageParser.getMoveInformation(
				Arrays.asList("2", "p"));

		assertEquals(Player.REARHAND, result.getPlayer());
		assertEquals(MoveType.PASS, result.getType());
	}

	@Test
	public void parsesBidMove() {
		MoveInformation result = MessageParser.getMoveInformation(
				Arrays.asList("0", "18"));

		assertEquals(Player.FOREHAND, result.getPlayer());
		assertEquals(MoveType.BID, result.getType());
		assertEquals(18, result.getBidValue());
	}

	@Test
	public void parsesResignMove() {
		MoveInformation result = MessageParser.getMoveInformation(
				Arrays.asList("w", "RE"));

		assertEquals(null, result.getPlayer());
		assertEquals(MoveType.RESIGN, result.getType());
	}

	@Test
	public void parsesTimeoutMove() {
		MoveInformation result = MessageParser.getMoveInformation(
				Arrays.asList("0", "TI.2"));

		assertEquals(MoveType.TIME_OUT, result.getType());
		assertEquals(Player.REARHAND, result.getTimeOutPlayer());
	}

	@Test
	public void parsesLeaveTableMove() {
		MoveInformation result = MessageParser.getMoveInformation(
				Arrays.asList("1", "LE.0"));

		assertEquals(MoveType.LEAVE_TABLE, result.getType());
		assertEquals(Player.FOREHAND, result.getLeavingPlayer());
	}

	@Test
	public void parsesSummaryPlayerNames() {
		SkatGameData result = MessageParser.parseGameSummary(
				"P0[Alice] P1[Bob] P2[Carol]");

		assertEquals("Alice", result.getPlayerName(Player.FOREHAND));
		assertEquals("Bob", result.getPlayerName(Player.MIDDLEHAND));
		assertEquals("Carol", result.getPlayerName(Player.REARHAND));
	}

	@Test
	public void ignoresUnknownSummaryParts() {
		SkatGameData result = MessageParser.parseGameSummary("UNKNOWN[value]");

		assertNotNull(result);
	}

	@Test
	public void createsTableChatMessage() {
		List<String> params = Arrays.asList("Alice", "hello", "there");

		org.jskat.data.iss.ChatMessage result =
				MessageParser.getTableChatMessage("table-1", params);

		assertNotNull(result);
	}

	@Test
	public void createsChatMessageWithSpeakerOnly() {
		org.jskat.data.iss.ChatMessage result =
				MessageParser.getTableChatMessage("table-2",
						Arrays.asList("Bob"));

		assertNotNull(result);
	}
}
