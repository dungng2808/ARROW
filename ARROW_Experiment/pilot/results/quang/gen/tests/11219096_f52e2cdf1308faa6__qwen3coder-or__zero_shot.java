package org.jskat.control.iss;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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
import org.junit.Test;

public class MessageParser_ArrowTest {

	@Test
	public void testGetTableStatus() {
		List<String> params = new ArrayList<String>();
		params.add("3");
		params.add("bar");
		params.add("xskat");
		params.add("xskat:2");
		params.add(".");
		params.add("xskat");
		params.add("$");
		params.add("0");
		params.add("0");
		params.add("0");
		params.add("0");
		params.add("0");
		params.add("0");
		params.add("1");
		params.add("0");
		params.add("xskat");
		params.add("$");
		params.add("0");
		params.add("0");
		params.add("0");
		params.add("0");
		params.add("0");
		params.add("0");
		params.add("1");
		params.add("1");
		params.add("xskat:2");
		params.add("$");
		params.add("0");
		params.add("0");
		params.add("0");
		params.add("0");
		params.add("0");
		params.add("0");
		params.add("1");
		params.add("1");
		params.add(".");
		params.add(".");
		params.add("0");
		params.add("0");
		params.add("0");
		params.add("0");
		params.add("0");
		params.add("0");
		params.add("0");
		params.add("0");
		params.add("false");

		TablePanelStatus status = MessageParser.getTableStatus("test", params);
		assertEquals("test", status.getLoginName());
		assertEquals(3, status.getMaxPlayers());
		assertEquals(1, status.getPlayerCount());
		assertTrue(status.hasPlayer("xskat"));
	}

	@Test
	public void testParsePlayerStatus() {
		List<String> params = new ArrayList<String>();
		params.add("xskat");
		params.add("$");
		params.add("0");
		params.add("0");
		params.add("0");
		params.add("1");
		params.add("0");
		params.add("0");
		params.add("1");
		params.add("1");

		PlayerStatus status = MessageParser.parsePlayerStatus(params);
		assertEquals("xskat", status.getName());
		assertEquals("$", status.getIP());
		assertEquals(0, status.getGamesPlayed());
		assertEquals(0, status.getGamesWon());
		assertEquals(0, status.getLastGameResult());
		assertEquals(1, status.getTotalPoints());
		assertFalse(status.isSwitch34());
		assertTrue(status.isTalkEnabled());
		assertTrue(status.isReadyToPlay());
	}

	@Test
	public void testGetGameStartStatus() {
		List<String> params = new ArrayList<String>();
		params.add("1");
		params.add("player1");
		params.add("10.0");
		params.add("player2");
		params.add("15.0");
		params.add("player3");
		params.add("20.0");

		GameStartInformation info = MessageParser.getGameStartStatus("test", params);
		assertEquals("test", info.getLoginName());
		assertEquals(1, info.getGameNo());
		assertEquals("player1", info.getPlayerName(Player.FOREHAND));
		assertEquals(10.0, info.getPlayerTime(Player.FOREHAND), 0.0);
		assertEquals("player2", info.getPlayerName(Player.MIDDLEHAND));
		assertEquals(15.0, info.getPlayerTime(Player.MIDDLEHAND), 0.0);
		assertEquals("player3", info.getPlayerName(Player.REARHAND));
		assertEquals(20.0, info.getPlayerTime(Player.REARHAND), 0.0);
	}

	@Test
	public void testGetMoveInformation_HoldBid() {
		List<String> params = new ArrayList<String>();
		params.add("0");
		params.add("y");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertEquals(MoveType.HOLD_BID, info.getType());
		assertEquals(MovePlayer.FOREHAND, info.getMovePlayer());
	}

	@Test
	public void testGetMoveInformation_Pass() {
		List<String> params = new ArrayList<String>();
		params.add("1");
		params.add("p");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertEquals(MoveType.PASS, info.getType());
		assertEquals(MovePlayer.MIDDLEHAND, info.getMovePlayer());
	}

	@Test
	public void testGetMoveInformation_SkatRequest() {
		List<String> params = new ArrayList<String>();
		params.add("2");
		params.add("s");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertEquals(MoveType.SKAT_REQUEST, info.getType());
		assertEquals(MovePlayer.REARHAND, info.getMovePlayer());
	}

	@Test
	public void testGetMoveInformation_PickUpSkat() {
		List<String> params = new ArrayList<String>();
		params.add("w");
		params.add("??.??");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertEquals(MoveType.PICK_UP_SKAT, info.getType());
		assertEquals(MovePlayer.WORLD, info.getMovePlayer());
	}

	@Test
	public void testGetMoveInformation_TimeOut() {
		List<String> params = new ArrayList<String>();
		params.add("0");
		params.add("TI.0");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertEquals(MoveType.TIME_OUT, info.getType());
		assertEquals(Player.FOREHAND, info.getTimeOutPlayer());
	}

	@Test
	public void testGetMoveInformation_Resign() {
		List<String> params = new ArrayList<String>();
		params.add("1");
		params.add("RE");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertEquals(MoveType.RESIGN, info.getType());
		assertEquals(MovePlayer.MIDDLEHAND, info.getMovePlayer());
	}

	@Test
	public void testGetMoveInformation_ShowCards() {
		List<String> params = new ArrayList<String>();
		params.add("2");
		params.add("SC.C7.HA");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertEquals(MoveType.SHOW_CARDS, info.getType());
		assertEquals(MovePlayer.REARHAND, info.getMovePlayer());
		assertEquals(2, info.getOuvertCards().size());
	}

	@Test
	public void testGetMoveInformation_LeaveTable() {
		List<String> params = new ArrayList<String>();
		params.add("0");
		params.add("LE.0");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertEquals(MoveType.LEAVE_TABLE, info.getType());
		assertEquals(Player.FOREHAND, info.getLeavingPlayer());
	}

	@Test
	public void testGetMoveInformation_CardPlay() {
		List<String> params = new ArrayList<String>();
		params.add("0");
		params.add("C7");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertEquals(MoveType.CARD_PLAY, info.getType());
		assertEquals(MovePlayer.FOREHAND, info.getMovePlayer());
		assertEquals(Card.C7, info.getCard());
	}

	@Test
	public void testGetMoveInformation_Bid() {
		List<String> params = new ArrayList<String>();
		params.add("1");
		params.add("20");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertEquals(MoveType.BID, info.getType());
		assertEquals(MovePlayer.MIDDLEHAND, info.getMovePlayer());
		assertEquals(20, info.getBidValue());
	}

	@Test
	public void testGetMoveInformation_Deal() {
		List<String> params = new ArrayList<String>();
		params.add("2");
		params.add("CQ.H7.C8.C9.CT.SQ.DK.H8.H9.SA.S9.HK.HQ.DJ.CJ.S8.DT.HT.ST.D7.CK.S7.C7.DQ.SJ.HA.CA.DA.D8.D9.SK.HJ");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertEquals(MoveType.DEAL, info.getType());
		assertEquals(MovePlayer.REARHAND, info.getMovePlayer());
		assertEquals(3, info.getDealtCards().size());
		assertEquals(2, info.getSkat().size());
	}

	@Test
	public void testGetMoveInformation_GameAnnouncement() {
		List<String> params = new ArrayList<String>();
		params.add("0");
		params.add("G.O.H.S.Z.C7.HA");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertEquals(MoveType.GAME_ANNOUNCEMENT, info.getType());
		assertEquals(MovePlayer.FOREHAND, info.getMovePlayer());
		GameAnnouncement announcement = info.getGameAnnouncement();
		assertEquals(GameType.GRAND, announcement.getGameType());
		assertTrue(announcement.isOuvert());
		assertTrue(announcement.isHand());
		assertTrue(announcement.isSchneider());
		assertTrue(announcement.isSchwarz());
		assertEquals(2, announcement.getDiscardedCards().size());
		assertEquals(2, announcement.getOuvertCards().size());
	}

	@Test
	public void testParsePlayerTimes() {
		List<String> params = new ArrayList<String>();
		params.add("10.0");
		params.add("15.0");
		params.add("20.0");

		MoveInformation info = new MoveInformation();
		MessageParser.parsePlayerTimes(params, info);
		assertEquals(10.0, info.getPlayerTime(Player.FOREHAND), 0.0);
		assertEquals(15.0, info.getPlayerTime(Player.MIDDLEHAND), 0.0);
		assertEquals(20.0, info.getPlayerTime(Player.REARHAND), 0.0);
	}

	@Test
	public void testParseSkatCards() {
		String move = "C7.HA";
		CardList cards = MessageParser.parseSkatCards(move);
		assertEquals(2, cards.size());
		assertEquals(Card.C7, cards.get(0));
		assertEquals(Card.HA, cards.get(1));
	}

	@Test
	public void testGetMovePlayer_World() {
		MoveInformation info = new MoveInformation();
		MessageParser.getMovePlayer("w", info);
		assertEquals(MovePlayer.WORLD, info.getMovePlayer());
	}

	@Test
	public void testGetMovePlayer_Forehand() {
		MoveInformation info = new MoveInformation();
		MessageParser.getMovePlayer("0", info);
		assertEquals(MovePlayer.FOREHAND, info.getMovePlayer());
	}

	@Test
	public void testGetMovePlayer_Middlehand() {
		MoveInformation info = new MoveInformation();
		MessageParser.getMovePlayer("1", info);
		assertEquals(MovePlayer.MIDDLEHAND, info.getMovePlayer());
	}

	@Test
	public void testGetMovePlayer_Rearhand() {
		MoveInformation info = new MoveInformation();
		MessageParser.getMovePlayer("2", info);
		assertEquals(MovePlayer.REARHAND, info.getMovePlayer());
	}

	@Test
	public void testParseGameAnnoucement_Grand() {
		MoveInformation info = new MoveInformation();
		GameAnnouncement announcement = MessageParser.parseGameAnnoucement(info, "G");
		assertEquals(GameType.GRAND, announcement.getGameType());
	}

	@Test
	public void testParseGameAnnoucement_Clubs() {
		MoveInformation info = new MoveInformation();
		GameAnnouncement announcement = MessageParser.parseGameAnnoucement(info, "C");
		assertEquals(GameType.CLUBS, announcement.getGameType());
	}

	@Test
	public void testParseGameAnnoucement_Spades() {
		MoveInformation info = new MoveInformation();
		GameAnnouncement announcement = MessageParser.parseGameAnnoucement(info, "S");
		assertEquals(GameType.SPADES, announcement.getGameType());
	}

	@Test
	public void testParseGameAnnoucement_Hearts() {
		MoveInformation info = new MoveInformation();
		GameAnnouncement announcement = MessageParser.parseGameAnnoucement(info, "H");
		assertEquals(GameType.HEARTS, announcement.getGameType());
	}

	@Test
	public void testParseGameAnnoucement_Diamonds() {
		MoveInformation info = new MoveInformation();
		GameAnnouncement announcement = MessageParser.parseGameAnnoucement(info, "D");
		assertEquals(GameType.DIAMONDS, announcement.getGameType());
	}

	@Test
	public void testParseGameAnnoucement_Null() {
		MoveInformation info = new MoveInformation();
		GameAnnouncement announcement = MessageParser.parseGameAnnoucement(info, "N");
		assertEquals(GameType.NULL, announcement.getGameType());
	}

	@Test
	public void testParseCardDealFromISSMessage() {
		String move = "??.??.??.??.??.??.??.??.??.??|C9.S9.ST.S8.C9.DT.DQ.CJ.SA.HA|??.??.??.??.??.??.??.??.??|??.??";
		List<CardList> deals = MessageParser.parseCardDeal(move);
		assertEquals(4, deals.size());
	}

	@Test
	public void testParseCardDealFromSummary() {
		String move = "CQ.H7.C8.C9.CT.SQ.DK.H8.H9.SA.S9.HK.HQ.DJ.CJ.S8.DT.HT.ST.D7.CK.S7.C7.DQ.SJ.HA.CA.DA.D8.D9.SK.HJ";
		List<CardList> deals = MessageParser.parseCardDeal(move);
		assertEquals(4, deals.size());
		assertEquals(10, deals.get(0).size());
		assertEquals(10, deals.get(1).size());
		assertEquals(10, deals.get(2).size());
		assertEquals(2, deals.get(3).size());
	}

	@Test
	public void testParseHand() {
		String hand = "C7.HA.C9";
		CardList cards = MessageParser.parseHand(hand);
		assertEquals(3, cards.size());
		assertEquals(Card.C7, cards.get(0));
		assertEquals(Card.HA, cards.get(1));
		assertEquals(Card.C9, cards.get(2));
	}

	@Test
	public void testParseTimeOut() {
		Player player = MessageParser.parseTimeOut("TI.0");
		assertEquals(Player.FOREHAND, player);
	}

	@Test
	public void testParseLeaveTable() {
		Player player = MessageParser.parseLeaveTable("LE.1");
		assertEquals(Player.MIDDLEHAND, player);
	}

	@Test
	public void testExtractPlayer() {
		Player player = MessageParser.extractPlayer("LE.2");
		assertEquals(Player.REARHAND, player);
	}

	@Test
	public void testParseGameSummary() {
		String gameSummary = "P0[player1]P1[player2]P2[player3]MV[0 y 1 p 2 s]R[d:0 win v:120 p:120]";
		SkatGameData gameData = MessageParser.parseGameSummary(gameSummary);
		assertEquals("player1", gameData.getPlayerName(Player.FOREHAND));
		assertEquals("player2", gameData.getPlayerName(Player.MIDDLEHAND));
		assertEquals("player3", gameData.getPlayerName(Player.REARHAND));
		assertEquals(1, gameData.getBids().size());
		assertEquals(1, gameData.getTricks().size());
		assertTrue(gameData.getResult().isWon());
		assertEquals(120, gameData.getResult().getGameValue());
		assertEquals(120, gameData.getDeclarerScore());
		assertEquals(0, gameData.getResult().getFinalOpponentPoints());
	}

	@Test
	public void testParseSummaryPart_Player0() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseSummaryPart(result, "P0", "player1");
		assertEquals("player1", result.getPlayerName(Player.FOREHAND));
	}

	@Test
	public void testParseSummaryPart_Player1() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseSummaryPart(result, "P1", "player2");
		assertEquals("player2", result.getPlayerName(Player.MIDDLEHAND));
	}

	@Test
	public void testParseSummaryPart_Player2() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseSummaryPart(result, "P2", "player3");
		assertEquals("player3", result.getPlayerName(Player.REARHAND));
	}

	@Test
	public void testParseSummaryPart_Moves() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseSummaryPart(result, "MV", "0 y 1 p 2 s");
		assertEquals(1, result.getBids().size());
		assertEquals(1, result.getTricks().size());
	}

	@Test
	public void testParseSummaryPart_Result() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseSummaryPart(result, "R", "d:0 win v:120 p:120");
		assertTrue(result.getResult().isWon());
		assertEquals(120, result.getResult().getGameValue());
		assertEquals(120, result.getDeclarerScore());
		assertEquals(0, result.getResult().getFinalOpponentPoints());
	}

	@Test
	public void testParseMoves_Deal() {
		SkatGameData result = new SkatGameData();
		String summaryPart = "2 CQ.H7.C8.C9.CT.SQ.DK.H8.H9.SA.S9.HK.HQ.DJ.CJ.S8.DT.HT.ST.D7.CK.S7.C7.DQ.SJ.HA.CA.DA.D8.D9.SK.HJ";
		MessageParser.parseMoves(result, summaryPart);
		assertEquals(3, result.getDealtCards(Player.FOREHAND).size());
		assertEquals(3, result.getDealtCards(Player.MIDDLEHAND).size());
		assertEquals(3, result.getDealtCards(Player.REARHAND).size());
		assertEquals(2, result.getDealtSkatCards().size());
	}

	@Test
	public void testParseMoves_Bid() {
		SkatGameData result = new SkatGameData();
		String summaryPart = "0 20";
		MessageParser.parseMoves(result, summaryPart);
		assertEquals(1, result.getBids().size());
		assertEquals(20, result.getBids().get(0).intValue());
	}

	@Test
	public void testParseMoves_HoldBid() {
		SkatGameData result = new SkatGameData();
		String summaryPart = "0 y";
		MessageParser.parseMoves(result, summaryPart);
		assertEquals(1, result.getBids().size());
		assertEquals(0, result.getBids().get(0).intValue());
	}

	@Test
	public void testParseMoves_Pass() {
		SkatGameData result = new SkatGameData();
		String summaryPart = "1 p";
		MessageParser.parseMoves(result, summaryPart);
		assertTrue(result.isPlayerPass(Player.MIDDLEHAND));
	}

	@Test
	public void testParseMoves_GameAnnouncement() {
		SkatGameData result = new SkatGameData();
		String summaryPart = "0 G.O.H.S.Z.C7.HA";
		MessageParser.parseMoves(result, summaryPart);
		assertEquals(GameType.GRAND, result.getAnnouncement().getGameType());
		assertTrue(result.getAnnouncement().isOuvert());
		assertTrue(result.getAnnouncement().isHand());
		assertTrue(result.getAnnouncement().isSchneider());
		assertTrue(result.getAnnouncement().isSchwarz());
		assertEquals(2, result.getAnnouncement().getDiscardedCards().size());
		assertEquals(2, result.getAnnouncement().getOuvertCards().size());
	}

	@Test
	public void testParseMoves_CardPlay() {
		SkatGameData result = new SkatGameData();
		String summaryPart = "0 C7";
		MessageParser.parseMoves(result, summaryPart);
		assertEquals(1, result.getTricks().size());
		assertEquals(1, result.getTricks().get(0).getCards().size());
		assertEquals(Card.C7, result.getTricks().get(0).getCards().get(0));
	}

	@Test
	public void testParseGameResult_Loss() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseGameResult(result, "loss");
		assertFalse(result.getResult().isWon());
	}

	@Test
	public void testParseGameResult_Win() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseGameResult(result, "win");
		assertTrue(result.getResult().isWon());
	}

	@Test
	public void testParseGameResult_GameValue() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseGameResult(result, "v:120");
		assertEquals(120, result.getResult().getGameValue());
	}

	@Test
	public void testParseGameResult_DeclarerPoints() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseGameResult(result, "p:120");
		assertEquals(120, result.getDeclarerScore());
		assertEquals(0, result.getResult().getFinalOpponentPoints());
	}

	@Test
	public void testParseGameResult_Overbid() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseGameResult(result, "overbid");
		assertTrue(result.getResult().isOverBidded());
	}

	@Test
	public void testParseGameResult_Schneider() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseGameResult(result, "s:1");
		assertTrue(result.getResult().isSchneider());
	}

	@Test
	public void testParseGameResult_Schwarz() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseGameResult(result, "z:1");
		assertTrue(result.getResult().isSchwarz());
	}

	@Test
	public void testParseDeclarerToken_Forehand() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseDeclarerToken(result, "d:0");
		assertEquals(Player.FOREHAND, result.getDeclarer());
	}

	@Test
	public void testParseDeclarerToken_Middlehand() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseDeclarerToken(result, "d:1");
		assertEquals(Player.MIDDLEHAND, result.getDeclarer());
	}

	@Test
	public void testParseDeclarerToken_Rearhand() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseDeclarerToken(result, "d:2");
		assertEquals(Player.REARHAND, result.getDeclarer());
	}

	@Test
	public void testGetTableChatMessage() {
		List<String> detailParams = new ArrayList<String>();
		detailParams.add("foo");
		detailParams.add("asdf");
		detailParams.add("jklö");

		ChatMessage message = MessageParser.getTableChatMessage("table1", detailParams);
		assertEquals("table1", message.getTableName());
		assertEquals("foo: asdf jklö ", message.getText());
	}

	private void assertFalse(boolean b) {
		assertEquals(false, b);
	}
}
