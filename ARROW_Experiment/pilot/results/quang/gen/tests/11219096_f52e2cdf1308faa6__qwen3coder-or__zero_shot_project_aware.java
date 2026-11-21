package org.jskat.control.iss;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
		List<String> params = Arrays.asList("3", "bar", "xskat", "xskat:2", ".", "xskat", "$", "0", "0", "0", "0", "0", "0", "1", "0", "xskat", "$", "0", "0", "0", "0", "0", "0", "1", "1", "xskat:2", "$", "0", "0", "0", "0", "0", "0", "1", "1", ".", ".", "0", "0", "0", "0", "0", "0", "0", "0", "false");

		TablePanelStatus status = MessageParser.getTableStatus("testUser", params);

		assertEquals("testUser", status.getLoginName());
		assertEquals(3, status.getMaxPlayers());
		assertEquals(3, status.getPlayerCount());
		assertTrue(status.hasPlayer("xskat"));
		assertTrue(status.hasPlayer("xskat:2"));
	}

	@Test
	public void testParsePlayerStatus() {
		List<String> params = Arrays.asList("xskat", "$", "0", "0", "0", "0", "0", "0", "1", "0");

		PlayerStatus status = MessageParser.parsePlayerStatus(params);

		assertEquals("xskat", status.getName());
		assertEquals("$", status.getIP());
		assertEquals(0, status.getGamesPlayed());
		assertEquals(0, status.getGamesWon());
		assertEquals(0, status.getLastGameResult());
		assertEquals(0, status.getTotalPoints());
		assertFalse(status.isSwitch34());
		assertFalse(status.isTalkEnabled());
		assertFalse(status.isReadyToPlay());
	}

	@Test
	public void testGetGameStartStatus() {
		List<String> params = Arrays.asList("1", "forehand", "10.5", "middle", "12.3", "rear", "8.7");

		GameStartInformation info = MessageParser.getGameStartStatus("testUser", params);

		assertEquals("testUser", info.getLoginName());
		assertEquals(1, info.getGameNo());
		assertEquals("forehand", info.getPlayerName(Player.FOREHAND));
		assertEquals(10.5, info.getPlayerTime(Player.FOREHAND));
		assertEquals("middle", info.getPlayerName(Player.MIDDLEHAND));
		assertEquals(12.3, info.getPlayerTime(Player.MIDDLEHAND));
		assertEquals("rear", info.getPlayerName(Player.REARHAND));
		assertEquals(8.7, info.getPlayerTime(Player.REARHAND));
	}

	@Test
	public void testGetMoveInformationHoldBid() {
		List<String> params = Arrays.asList("0", "y");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MoveType.HOLD_BID, info.getType());
		assertEquals(MovePlayer.FOREHAND, info.getMovePlayer());
	}

	@Test
	public void testGetMoveInformationPass() {
		List<String> params = Arrays.asList("1", "p");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MoveType.PASS, info.getType());
		assertEquals(MovePlayer.MIDDLEHAND, info.getMovePlayer());
	}

	@Test
	public void testGetMoveInformationSkatRequest() {
		List<String> params = Arrays.asList("2", "s");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MoveType.SKAT_REQUEST, info.getType());
		assertEquals(MovePlayer.REARHAND, info.getMovePlayer());
	}

	@Test
	public void testGetMoveInformationPickUpSkat() {
		List<String> params = Arrays.asList("w", "??.??");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MoveType.PICK_UP_SKAT, info.getType());
		assertEquals(MovePlayer.WORLD, info.getMovePlayer());
	}

	@Test
	public void testGetMoveInformationTimeout() {
		List<String> params = Arrays.asList("0", "TI.0");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MoveType.TIME_OUT, info.getType());
		assertEquals(Player.FOREHAND, info.getTimeOutPlayer());
		assertEquals(MovePlayer.FOREHAND, info.getMovePlayer());
	}

	@Test
	public void testGetMoveInformationResign() {
		List<String> params = Arrays.asList("1", "RE");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MoveType.RESIGN, info.getType());
		assertEquals(MovePlayer.MIDDLEHAND, info.getMovePlayer());
	}

	@Test
	public void testGetMoveInformationShowCards() {
		List<String> params = Arrays.asList("2", "SC.C7.H7");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MoveType.SHOW_CARDS, info.getType());
		assertEquals(MovePlayer.REARHAND, info.getMovePlayer());
		assertEquals(2, info.getOuvertCards().size());
	}

	@Test
	public void testGetMoveInformationLeaveTable() {
		List<String> params = Arrays.asList("0", "LE.0");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MoveType.LEAVE_TABLE, info.getType());
		assertEquals(Player.FOREHAND, info.getLeavingPlayer());
		assertEquals(MovePlayer.FOREHAND, info.getMovePlayer());
	}

	@Test
	public void testGetMoveInformationCardPlay() {
		List<String> params = Arrays.asList("1", "C7");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MoveType.CARD_PLAY, info.getType());
		assertEquals(MovePlayer.MIDDLEHAND, info.getMovePlayer());
		assertEquals(Card.C7, info.getCard());
	}

	@Test
	public void testGetMoveInformationBid() {
		List<String> params = Arrays.asList("2", "20");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MoveType.BID, info.getType());
		assertEquals(MovePlayer.REARHAND, info.getMovePlayer());
		assertEquals(20, info.getBidValue());
	}

	@Test
	public void testGetMoveInformationDeal() {
		List<String> params = Arrays.asList("0", "CQ.H7.C8.C9.CT.SQ.DK.H8.H9.SA|S9.HK.HQ.DJ.CJ.S8.DT.HT.ST.D7.CK|S7.C7.DQ.SJ.HA.CA.DA.D8.D9.SK.HJ|??");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MoveType.DEAL, info.getType());
		assertEquals(MovePlayer.FOREHAND, info.getMovePlayer());
		assertEquals(3, info.getDealtCards().size());
		assertEquals(2, info.getSkat().size());
	}

	@Test
	public void testGetMoveInformationGameAnnouncement() {
		List<String> params = Arrays.asList("1", "G.O.H.S.Z");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MoveType.GAME_ANNOUNCEMENT, info.getType());
		assertEquals(MovePlayer.MIDDLEHAND, info.getMovePlayer());
		GameAnnouncement announcement = info.getGameAnnouncement();
		assertEquals(GameType.GRAND, announcement.getGameType());
		assertTrue(announcement.isOuvert());
		assertTrue(announcement.isHand());
		assertTrue(announcement.isSchneider());
		assertTrue(announcement.isSchwarz());
	}

	@Test
	public void testParseSkatCards() {
		String move = "C7.H7";

		CardList cards = MessageParser.parseSkatCards(move);

		assertEquals(2, cards.size());
		assertEquals(Card.C7, cards.get(0));
		assertEquals(Card.H7, cards.get(1));
	}

	@Test
	public void testGetMovePlayerWorld() {
		List<String> params = Arrays.asList("w", "C7");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MovePlayer.WORLD, info.getMovePlayer());
	}

	@Test
	public void testGetMovePlayerForehand() {
		List<String> params = Arrays.asList("0", "C7");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MovePlayer.FOREHAND, info.getMovePlayer());
	}

	@Test
	public void testGetMovePlayerMiddlehand() {
		List<String> params = Arrays.asList("1", "C7");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MovePlayer.MIDDLEHAND, info.getMovePlayer());
	}

	@Test
	public void testGetMovePlayerRearhand() {
		List<String> params = Arrays.asList("2", "C7");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MovePlayer.REARHAND, info.getMovePlayer());
	}

	@Test
	public void testParseGameAnnouncementGrand() {
		List<String> params = Arrays.asList("0", "G");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MoveType.GAME_ANNOUNCEMENT, info.getType());
		GameAnnouncement announcement = info.getGameAnnouncement();
		assertEquals(GameType.GRAND, announcement.getGameType());
	}

	@Test
	public void testParseGameAnnouncementClubs() {
		List<String> params = Arrays.asList("0", "C");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MoveType.GAME_ANNOUNCEMENT, info.getType());
		GameAnnouncement announcement = info.getGameAnnouncement();
		assertEquals(GameType.CLUBS, announcement.getGameType());
	}

	@Test
	public void testParseGameAnnouncementSpades() {
		List<String> params = Arrays.asList("0", "S");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MoveType.GAME_ANNOUNCEMENT, info.getType());
		GameAnnouncement announcement = info.getGameAnnouncement();
		assertEquals(GameType.SPADES, announcement.getGameType());
	}

	@Test
	public void testParseGameAnnouncementHearts() {
		List<String> params = Arrays.asList("0", "H");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MoveType.GAME_ANNOUNCEMENT, info.getType());
		GameAnnouncement announcement = info.getGameAnnouncement();
		assertEquals(GameType.HEARTS, announcement.getGameType());
	}

	@Test
	public void testParseGameAnnouncementDiamonds() {
		List<String> params = Arrays.asList("0", "D");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MoveType.GAME_ANNOUNCEMENT, info.getType());
		GameAnnouncement announcement = info.getGameAnnouncement();
		assertEquals(GameType.DIAMONDS, announcement.getGameType());
	}

	@Test
	public void testParseGameAnnouncementNull() {
		List<String> params = Arrays.asList("0", "N");

		MoveInformation info = MessageParser.getMoveInformation(params);

		assertEquals(MoveType.GAME_ANNOUNCEMENT, info.getType());
		GameAnnouncement announcement = info.getGameAnnouncement();
		assertEquals(GameType.NULL, announcement.getGameType());
	}

	@Test
	public void testParseCardDealFromISSMessage() {
		String move = "CQ.H7.C8.C9.CT.SQ.DK.H8.H9.SA|S9.HK.HQ.DJ.CJ.S8.DT.HT.ST.D7.CK|S7.C7.DQ.SJ.HA.CA.DA.D8.D9.SK.HJ|??";

		List<CardList> dealtCards = MessageParser.parseCardDeal(move);

		assertEquals(4, dealtCards.size());
		assertEquals(10, dealtCards.get(0).size());
		assertEquals(10, dealtCards.get(1).size());
		assertEquals(10, dealtCards.get(2).size());
		assertEquals(2, dealtCards.get(3).size());
	}

	@Test
	public void testParseCardDealFromSummary() {
		String move = "CQ.H7.C8.C9.CT.SQ.DK.H8.H9.SA.S9.HK.HQ.DJ.CJ.S8.DT.HT.ST.D7.CK.S7.C7.DQ.SJ.HA.CA.DA.D8.D9.SK.HJ";

		List<CardList> dealtCards = MessageParser.parseCardDeal(move);

		assertEquals(4, dealtCards.size());
		assertEquals(10, dealtCards.get(0).size());
		assertEquals(10, dealtCards.get(1).size());
		assertEquals(10, dealtCards.get(2).size());
		assertEquals(2, dealtCards.get(3).size());
	}

	@Test
	public void testParseHand() {
		String hand = "CQ.H7.C8.C9.CT.SQ.DK.H8.H9.SA";

		CardList cards = MessageParser.parseHand(hand);

		assertEquals(10, cards.size());
		assertEquals(Card.CQ, cards.get(0));
		assertEquals(Card.H7, cards.get(1));
		assertEquals(Card.C8, cards.get(2));
		assertEquals(Card.C9, cards.get(3));
		assertEquals(Card.CT, cards.get(4));
		assertEquals(Card.SQ, cards.get(5));
		assertEquals(Card.DK, cards.get(6));
		assertEquals(Card.H8, cards.get(7));
		assertEquals(Card.H9, cards.get(8));
		assertEquals(Card.SA, cards.get(9));
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
		String gameSummary = "P0[forehand]P1[middle]P2[rear]MV[0 y 1 p 2 C7]R[d:0 win v:120 p:120 s:1 z:1]";

		SkatGameData gameData = MessageParser.parseGameSummary(gameSummary);

		assertEquals("forehand", gameData.getPlayerName(Player.FOREHAND));
		assertEquals("middle", gameData.getPlayerName(Player.MIDDLEHAND));
		assertEquals("rear", gameData.getPlayerName(Player.REARHAND));
		assertEquals(Player.FOREHAND, gameData.getDeclarer());
		assertTrue(gameData.getResult().isWon());
		assertEquals(120, gameData.getResult().getGameValue());
		assertEquals(120, gameData.getDeclarerScore());
		assertTrue(gameData.getResult().isSchneider());
		assertTrue(gameData.getResult().isSchwarz());
	}

	@Test
	public void testGetTableChatMessage() {
		List<String> detailParams = Arrays.asList("foo", "asdf", "jklö");

		ChatMessage chatMessage = MessageParser.getTableChatMessage("table1", detailParams);

		assertEquals("table1", chatMessage.getTableName());
		assertEquals("foo: asdf jklö ", chatMessage.getMessage());
	}

	private void assertFalse(boolean b) {
		assertEquals(false, b);
	}
}
