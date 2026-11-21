package org.jskat.control.iss;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jskat.data.SkatGameData;
import org.jskat.data.iss.ChatMessage;
import org.jskat.data.iss.GameStartInformation;
import org.jskat.data.iss.MoveInformation;
import org.jskat.data.iss.MovePlayer;
import org.jskat.data.iss.MoveType;
import org.jskat.util.Player;
import org.junit.Test;

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
		assertEquals(Double.valueOf(1.5), result.getPlayerTime(Player.FOREHAND));
		assertEquals("Bob", result.getPlayerName(Player.MIDDLEHAND));
		assertEquals(Double.valueOf(2.5), result.getPlayerTime(Player.MIDDLEHAND));
		assertEquals("Carol", result.getPlayerName(Player.REARHAND));
		assertEquals(Double.valueOf(3.5), result.getPlayerTime(Player.REARHAND));
	}

	@Test
	public void parsesHoldBidMove() {
		MoveInformation result = MessageParser.getMoveInformation(
				Arrays.asList("1", "y"));

		assertEquals(MovePlayer.MIDDLEHAND, result.getPlayer());
		assertEquals(MoveType.HOLD_BID, result.getType());
	}

	@Test
	public void parsesPassMove() {
		MoveInformation result = MessageParser.getMoveInformation(
				Arrays.asList("2", "p"));

		assertEquals(MovePlayer.REARHAND, result.getPlayer());
		assertEquals(MoveType.PASS, result.getType());
	}

	@Test
	public void parsesBidMove() {
		MoveInformation result = MessageParser.getMoveInformation(
				Arrays.asList("0", "18"));

		assertEquals(MovePlayer.FOREHAND, result.getPlayer());
		assertEquals(MoveType.BID, result.getType());
		assertEquals(18, result.getBidValue());
	}

	@Test
	public void parsesResignMove() {
		MoveInformation result = MessageParser.getMoveInformation(
				Arrays.asList("w", "RE"));

		assertEquals(MovePlayer.WORLD, result.getPlayer());
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
		List<String> params = new ArrayList<String>();
		params.add("Alice");
		params.add("hello");
		params.add("there");

		ChatMessage result = MessageParser.getTableChatMessage("table-1", params);

		assertNotNull(result);
		assertEquals("table-1", result.getTableName());
		assertEquals("Alice: hello there ", result.getText());
	}

	@Test
	public void createsChatMessageWithSpeakerOnly() {
		ChatMessage result = MessageParser.getTableChatMessage("table-2",
				Arrays.asList("Bob"));

		assertEquals("table-2", result.getTableName());
		assertEquals("Bob: ", result.getText());
	}
}
