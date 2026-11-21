package org.jskat.control.iss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jskat.data.SkatGameData;
import org.jskat.data.iss.ChatMessage;
import org.jskat.data.iss.GameStartInformation;
import org.jskat.data.iss.MoveInformation;
import org.jskat.data.iss.MovePlayer;
import org.jskat.data.iss.MoveType;
import org.jskat.data.iss.PlayerStatus;
import org.jskat.data.iss.TablePanelStatus;
import org.jskat.util.Player;
import org.junit.jupiter.api.Test;

public class MessageParser_ArrowTest {

	@Test
	public void getTableChatMessageFormatsSpeakerAndText() {
		ChatMessage message = MessageParser.getTableChatMessage("table1",
				Arrays.asList("alice", "hello", "there"));

		assertNotNull(message);
		assertEquals("alice: hello there ", message.getMessage());
	}

	@Test
	public void getTableChatMessageWithSpeakerOnlyHasTrailingSeparator() {
		ChatMessage message = MessageParser.getTableChatMessage("t",
				Arrays.asList("alice"));

		assertEquals("alice: ", message.getMessage());
	}

	@Test
	public void getGameStartStatusParsesPlayersAndTimes() {
		List<String> params = Arrays.asList("7", "alice", "1.5", "bob", "2.5",
				"carol", "3.5");

		GameStartInformation information = MessageParser.getGameStartStatus(
				"alice", params);

		assertEquals("alice", information.getLoginName());
		assertEquals(7, information.getGameNo());
		assertEquals("alice", information.getPlayerName(Player.FOREHAND));
		assertEquals(1.5, information.getPlayerTime(Player.FOREHAND));
		assertEquals("bob", information.getPlayerName(Player.MIDDLEHAND));
		assertEquals(2.5, information.getPlayerTime(Player.MIDDLEHAND));
		assertEquals("carol", information.getPlayerName(Player.REARHAND));
		assertEquals(3.5, information.getPlayerTime(Player.REARHAND));
	}

	@Test
	public void getMoveInformationParsesPassAndPlayer() {
		MoveInformation information = MessageParser.getMoveInformation(
				Arrays.asList("1", "p"));

		assertEquals(MovePlayer.MIDDLEHAND, information.getMovePlayer());
		assertEquals(MoveType.PASS, information.getType());
	}

	@Test
	public void getMoveInformationParsesBid() {
		MoveInformation information = MessageParser.getMoveInformation(
				Arrays.asList("0", "18"));

		assertEquals(MovePlayer.FOREHAND, information.getMovePlayer());
		assertEquals(MoveType.BID, information.getType());
		assertEquals(18, information.getBidValue());
	}

	@Test
	public void getMoveInformationParsesTimeoutPlayer() {
		MoveInformation information = MessageParser.getMoveInformation(
				Arrays.asList("w", "TI.2"));

		assertEquals(MovePlayer.WORLD, information.getMovePlayer());
		assertEquals(MoveType.TIME_OUT, information.getType());
		assertEquals(Player.REARHAND, information.getTimeOutPlayer());
	}

	@Test
	public void getMoveInformationParsesResign() {
		MoveInformation information = MessageParser.getMoveInformation(
				Arrays.asList("2", "RE"));

		assertEquals(MoveType.RESIGN, information.getType());
	}

	@Test
	public void getMoveInformationParsesShowCardsWithoutCards() {
		MoveInformation information = MessageParser.getMoveInformation(
				Arrays.asList("0", "SC"));

		assertEquals(MoveType.SHOW_CARDS, information.getType());
	}

	@Test
	public void parsePlayerTimesReadsLastThreeEntries() {
		MoveInformation information = new MoveInformation();
		MessageParser.parsePlayerTimes(
				Arrays.asList("unused", "1.25", "2.5", "3.75"), information);

		assertEquals(1.25, information.getPlayerTime(Player.FOREHAND));
		assertEquals(2.5, information.getPlayerTime(Player.MIDDLEHAND));
		assertEquals(3.75, information.getPlayerTime(Player.REARHAND));
	}

	@Test
	public void getTableStatusParsesOccupiedSeatAndPlayerLeftFlag() {
		List<String> params = new ArrayList<String>();
		params.add("1");
		params.add(".");
		params.add(".");
		params.add(".");
		params.add(".");
		params.add("alice");
		params.add("127.0.0.1");
		params.add("4");
		params.add("3");
		params.add("1");
		params.add("50");
		params.add("1");
		params.add("0");
		params.add("0");
		params.add("1");
		params.add("1");

		TablePanelStatus status = MessageParser.getTableStatus("alice", params);

		assertEquals("alice", status.getLoginName());
		assertEquals(1, status.getMaxPlayers());
		assertNotNull(status);
	}

	@Test
	public void parseGameSummaryReturnsDataForEmptySummary() {
		SkatGameData data = MessageParser.parseGameSummary("");

		assertNotNull(data);
	}
}
