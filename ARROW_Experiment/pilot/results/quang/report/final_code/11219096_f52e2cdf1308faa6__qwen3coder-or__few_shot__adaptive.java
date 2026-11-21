package org.jskat.control.iss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jskat.data.GameAnnouncement;
import org.jskat.data.SkatGameData;
import org.jskat.data.Trick;
import org.jskat.data.iss.ChatMessage;
import org.jskat.data.iss.GameStartInformation;
import org.jskat.data.iss.MoveInformation;
import org.jskat.data.iss.MovePlayer;
import org.jskat.data.iss.MoveType;
import org.jskat.data.iss.PlayerStatus;
import org.jskat.data.iss.TablePanelStatus;
import org.jskat.util.Card;
import org.jskat.util.CardList;
import org.jskat.util.GameType;
import org.jskat.util.Player;
import org.junit.jupiter.api.Test;

public class MessageParser_ArrowTest {

	@Test
	public void testGetTableStatus() {
		String loginName = "testUser";
		List<String> params = Arrays.asList("3", "bar", "xskat", "xskat:2", ".", "bar", ".", "0", "0", "0", "0", "0", "0", "1", "0", "xskat", "$", "0", "0", "0", "0", "0", "0", "1", "1", "xskat:2", "$", "0", "0", "0", "0", "0", "0", "1", "1", ".", ".", "0", "0", "0", "0", "0", "0", "0", "0", "false");

		TablePanelStatus status = MessageParser.getTableStatus(loginName, params);

		assertNotNull(status);
		assertEquals(loginName, status.getLoginName());
		assertEquals(3, status.getMaxPlayers());
		assertEquals(2, status.getNumberOfPlayers());
		assertTrue(status.getPlayerInformations().containsKey("bar"));
		assertTrue(status.getPlayerInformations().containsKey("xskat"));
	}

	@Test
	public void testParsePlayerStatus() {
		List<String> params = Arrays.asList("bar", "xskat", "10", "5", "3", "100", "1", "0", "1", "1");

		// Since parsePlayerStatus is private, we'll test through getTableStatus
		// which internally calls parsePlayerStatus
		List<String> fullParams = Arrays.asList("3", "bar", "xskat", "xskat:2", ".", "bar", ".", "0", "0", "0", "0", "0", "0", "1", "0", "xskat", "$", "0", "0", "0", "0", "0", "0", "1", "1", "xskat:2", "$", "0", "0", "0", "0", "0", "0", "1", "1", ".", ".", "0", "0", "0", "0", "0", "0", "0", "0", "false");
		TablePanelStatus status = MessageParser.getTableStatus("testUser", fullParams);
		
		PlayerStatus playerStatus = status.getPlayerInformation("bar");
		assertNotNull(playerStatus);
		assertEquals("bar", playerStatus.getName());
		assertEquals("xskat", playerStatus.getIP());
		assertEquals(10, playerStatus.getGamesPlayed());
		assertEquals(5, playerStatus.getGamesWon());
		assertEquals(3, playerStatus.getLastGameResult());
		assertEquals(100, playerStatus.getTotalPoints());
		assertTrue(playerStatus.isSwitch34());
		assertTrue(playerStatus.isTalkEnabled());
		assertTrue(playerStatus.isReadyToPlay());
	}

	@Test
	public void testGetGameStartStatus() {
		String loginName = "testUser";
		List<String> params = Arrays.asList("1", "forehand", "10.5", "middle", "12.3", "rear", "8.7");

		GameStartInformation status = MessageParser.getGameStartStatus(loginName, params);

		assertNotNull(status);
		assertEquals(loginName, status.getLoginName());
		assertEquals(1, status.getGameNo());
		assertEquals("forehand", status.getPlayerName(Player.FOREHAND));
		assertEquals(10.5, status.getPlayerTime(Player.FOREHAND));
		assertEquals("middle", status.getPlayerName(Player.MIDDLEHAND));
		assertEquals(12.3, status.getPlayerTime(Player.MIDDLEHAND));
		assertEquals("rear", status.getPlayerName(Player.REARHAND));
		assertEquals(8.7, status.getPlayerTime(Player.REARHAND));
	}

	@Test
	public void testGetMoveInformation_HoldBid() {
		List<String> params = Arrays.asList("0", "y");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertNotNull(info);
		assertEquals(MoveType.HOLD_BID, info.getType());
		assertEquals(MovePlayer.FOREHAND, info.getMovePlayer());
	}

	@Test
	public void testGetMoveInformation_Pass() {
		List<String> params = Arrays.asList("1", "p");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertNotNull(info);
		assertEquals(MoveType.PASS, info.getType());
		assertEquals(MovePlayer.MIDDLEHAND, info.getMovePlayer());
	}

	@Test
	public void testGetMoveInformation_SkatRequest() {
		List<String> params = Arrays.asList("2", "s");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertNotNull(info);
		assertEquals(MoveType.SKAT_REQUEST, info.getType());
		assertEquals(MovePlayer.REARHAND, info.getMovePlayer());
	}

	@Test
	public void testGetMoveInformation_TimeOut() {
		List<String> params = Arrays.asList("w", "TI.0");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertNotNull(info);
		assertEquals(MoveType.TIME_OUT, info.getType());
		assertEquals(Player.FOREHAND, info.getTimeOutPlayer());
	}

	@Test
	public void testGetMoveInformation_Resign() {
		List<String> params = Arrays.asList("0", "RE");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertNotNull(info);
		assertEquals(MoveType.RESIGN, info.getType());
		assertEquals(MovePlayer.FOREHAND, info.getMovePlayer());
	}

	@Test
	public void testGetMoveInformation_ShowCards() {
		List<String> params = Arrays.asList("1", "SC.C7.H7");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertNotNull(info);
		assertEquals(MoveType.SHOW_CARDS, info.getType());
		assertEquals(MovePlayer.MIDDLEHAND, info.getMovePlayer());
		assertNotNull(info.getOuvertCards());
		assertEquals(2, info.getOuvertCards().size());
	}

	@Test
	public void testGetMoveInformation_LeaveTable() {
		List<String> params = Arrays.asList("2", "LE.1");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertNotNull(info);
		assertEquals(MoveType.LEAVE_TABLE, info.getType());
		assertEquals(Player.MIDDLEHAND, info.getLeavingPlayer());
		assertEquals(MovePlayer.REARHAND, info.getMovePlayer());
	}

	@Test
	public void testGetMoveInformation_CardPlay() {
		List<String> params = Arrays.asList("0", "C7");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertNotNull(info);
		assertEquals(MoveType.CARD_PLAY, info.getType());
		assertEquals(MovePlayer.FOREHAND, info.getMovePlayer());
		assertEquals(Card.C7, info.getCard());
	}

	@Test
	public void testGetMoveInformation_Bid() {
		List<String> params = Arrays.asList("1", "20");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertNotNull(info);
		assertEquals(MoveType.BID, info.getType());
		assertEquals(MovePlayer.MIDDLEHAND, info.getMovePlayer());
		assertEquals(20, info.getBidValue());
	}

	@Test
	public void testGetMoveInformation_Deal() {
		List<String> params = Arrays.asList("2", "CQ.H7.C8.C9.CT.SQ.DK.H8.H9.SA.S9.HK.HQ.DJ.CJ.S8.DT.HT.ST.D7.CK.S7.C7.DQ.SJ.HA.CA.DA.D8.D9.SK.HJ");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertNotNull(info);
		assertEquals(MoveType.DEAL, info.getType());
		assertEquals(MovePlayer.REARHAND, info.getMovePlayer());
		assertNotNull(info.getCards(Player.FOREHAND));
		assertEquals(10, info.getCards(Player.FOREHAND).size());
		assertNotNull(info.getCards(Player.MIDDLEHAND));
		assertEquals(10, info.getCards(Player.MIDDLEHAND).size());
		assertNotNull(info.getCards(Player.REARHAND));
		assertEquals(10, info.getCards(Player.REARHAND).size());
		assertNotNull(info.getSkat());
		assertEquals(2, info.getSkat().size());
	}

	@Test
	public void testGetMoveInformation_GameAnnouncement() {
		List<String> params = Arrays.asList("0", "G.O.H.S.Z");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertNotNull(info);
		assertEquals(MoveType.GAME_ANNOUNCEMENT, info.getType());
		assertEquals(MovePlayer.FOREHAND, info.getMovePlayer());
		assertNotNull(info.getGameAnnouncement());
		assertEquals(GameType.GRAND, info.getGameAnnouncement().getGameType());
		assertTrue(info.getGameAnnouncement().isOuvert());
		assertTrue(info.getGameAnnouncement().isHand());
		assertTrue(info.getGameAnnouncement().isSchneider());
		assertTrue(info.getGameAnnouncement().isSchwarz());
	}

	@Test
	public void testParseGameAnnouncement() {
		String move = "G.O.H.S.Z";
		MoveInformation info = new MoveInformation();
		// Since parseGameAnnoucement is private, we'll test through getMoveInformation
		// which internally calls parseGameAnnoucement
		List<String> params = Arrays.asList("0", move);
		MoveInformation parsedInfo = MessageParser.getMoveInformation(params);
		
		assertNotNull(parsedInfo.getGameAnnouncement());
		assertEquals(GameType.GRAND, parsedInfo.getGameAnnouncement().getGameType());
		assertTrue(parsedInfo.getGameAnnouncement().isOuvert());
		assertTrue(parsedInfo.getGameAnnouncement().isHand());
		assertTrue(parsedInfo.getGameAnnouncement().isSchneider());
		assertTrue(parsedInfo.getGameAnnouncement().isSchwarz());
	}

	@Test
	public void testParseCardDealFromISSMessage() {
		String move = "??.??.??.??.??.??.??.??.??.??|D9.S9.ST.S8.C9.DT.DQ.CJ.SA.HA|??.??.??.??.??.??.??.??.??|??.??";

		// Since parseCardDeal is private, we'll test through getMoveInformation
		// which internally calls parseCardDeal
		List<String> params = Arrays.asList("0", move);
		MoveInformation info = MessageParser.getMoveInformation(params);
		
		assertNotNull(info.getCards(Player.FOREHAND));
		assertEquals(10, info.getCards(Player.FOREHAND).size());
		assertNotNull(info.getCards(Player.MIDDLEHAND));
		assertEquals(10, info.getCards(Player.MIDDLEHAND).size());
		assertNotNull(info.getCards(Player.REARHAND));
		assertEquals(10, info.getCards(Player.REARHAND).size());
		assertNotNull(info.getSkat());
		assertEquals(2, info.getSkat().size());
	}

	@Test
	public void testParseCardDealFromSummary() {
		String move = "CQ.H7.C8.C9.CT.SQ.DK.H8.H9.SA.S9.HK.HQ.DJ.CJ.S8.DT.HT.ST.D7.CK.S7.C7.DQ.SJ.HA.CA.DA.D8.D9.SK.HJ";

		// Since parseCardDeal is private, we'll test through getMoveInformation
		// which internally calls parseCardDeal
		List<String> params = Arrays.asList("0", move);
		MoveInformation info = MessageParser.getMoveInformation(params);
		
		assertNotNull(info.getCards(Player.FOREHAND));
		assertEquals(10, info.getCards(Player.FOREHAND).size());
		assertNotNull(info.getCards(Player.MIDDLEHAND));
		assertEquals(10, info.getCards(Player.MIDDLEHAND).size());
		assertNotNull(info.getCards(Player.REARHAND));
		assertEquals(10, info.getCards(Player.REARHAND).size());
		assertNotNull(info.getSkat());
		assertEquals(2, info.getSkat().size());
	}

	@Test
	public void testParseHand() {
		String hand = "C7.H7.S7.D7";

		// Since parseHand is private, we'll test through getMoveInformation
		// which internally calls parseHand
		List<String> params = Arrays.asList("0", hand);
		MoveInformation info = MessageParser.getMoveInformation(params);
		
		assertNotNull(info.getOuvertCards());
		assertEquals(4, info.getOuvertCards().size());
		assertEquals(Card.C7, info.getOuvertCards().get(0));
		assertEquals(Card.H7, info.getOuvertCards().get(1));
		assertEquals(Card.S7, info.getOuvertCards().get(2));
		assertEquals(Card.D7, info.getOuvertCards().get(3));
	}

	@Test
	public void testExtractPlayer() {
		// Since extractPlayer is private, we'll test through getMoveInformation
		// which internally calls extractPlayer
		List<String> params = Arrays.asList("w", "TI.0");
		MoveInformation info = MessageParser.getMoveInformation(params);
		assertEquals(Player.FOREHAND, info.getTimeOutPlayer());
		
		params = Arrays.asList("0", "LE.1");
		info = MessageParser.getMoveInformation(params);
		assertEquals(Player.MIDDLEHAND, info.getLeavingPlayer());
		
		params = Arrays.asList("0", "LE.2");
		info = MessageParser.getMoveInformation(params);
		assertEquals(Player.REARHAND, info.getLeavingPlayer());
	}

	@Test
	public void testParseGameSummary() {
		String gameSummary = "P0[forehand]P1[middle]P2[rear]MV[0 y 1 p 2 s]R[d:0 win v:120 p:80 s:1 z:1]";

		SkatGameData result = MessageParser.parseGameSummary(gameSummary);

		assertNotNull(result);
		assertEquals("forehand", result.getPlayerName(Player.FOREHAND));
		assertEquals("middle", result.getPlayerName(Player.MIDDLEHAND));
		assertEquals("rear", result.getPlayerName(Player.REARHAND));
		assertEquals(1, result.getGameMoves().size());
		assertEquals(1, result.getTricks().size());
		assertTrue(result.getResult().isWon());
		assertEquals(120, result.getResult().getGameValue());
		assertEquals(80, result.getDeclarerScore());
		assertTrue(result.getResult().isSchneider());
		assertTrue(result.getResult().isSchwarz());
	}

	@Test
	public void testParseMoves() {
		String summaryPart = "0 y 1 p 2 s";
		SkatGameData gameData = new SkatGameData();
		// Since parseMoves is private, we'll test through parseGameSummary
		// which internally calls parseMoves
		String gameSummary = "P0[forehand]P1[middle]P2[rear]MV[" + summaryPart + "]R[d:0 win v:120 p:80 s:1 z:1]";
		SkatGameData result = MessageParser.parseGameSummary(gameSummary);
		
		assertEquals(1, result.getGameMoves().size());
		assertEquals(1, result.getTricks().size());
	}

	@Test
	public void testParseGameResult() {
		String summaryPart = "d:0 win v:120 p:80 s:1 z:1";
		SkatGameData gameData = new SkatGameData();
		// Since parseGameResult is private, we'll test through parseGameSummary
		// which internally calls parseGameResult
		String gameSummary = "P0[forehand]P1[middle]P2[rear]MV[0 y]R[" + summaryPart + "]";
		SkatGameData result = MessageParser.parseGameSummary(gameSummary);
		
		assertTrue(result.getResult().isWon());
		assertEquals(120, result.getResult().getGameValue());
		assertEquals(80, result.getDeclarerScore());
		assertEquals(40, result.getResult().getFinalOpponentPoints());
		assertTrue(result.getResult().isSchneider());
		assertTrue(result.getResult().isSchwarz());
	}

	@Test
	public void testGetTableChatMessage() {
		String tableName = "testTable";
		List<String> detailParams = Arrays.asList("user1", "Hello", "world!");

		ChatMessage chatMessage = MessageParser.getTableChatMessage(tableName, detailParams);

		assertNotNull(chatMessage);
		assertEquals(tableName, chatMessage.getChatName());
		assertEquals("user1: Hello world! ", chatMessage.getMessage());
	}
}
