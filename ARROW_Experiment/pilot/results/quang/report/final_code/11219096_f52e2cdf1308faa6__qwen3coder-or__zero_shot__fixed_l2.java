package org.jskat.control.iss;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.junit.jupiter.api.Test;

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
		assertThat(status.getLoginName()).isEqualTo("test");
		assertThat(status.getMaxPlayers()).isEqualTo(3);
		assertThat(status.getNumberOfPlayers()).isEqualTo(1);
		assertThat(status.getPlayerInformations().containsKey("xskat")).isTrue();
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
		assertThat(info.getLoginName()).isEqualTo("test");
		assertThat(info.getGameNo()).isEqualTo(1);
		assertThat(info.getPlayerName(Player.FOREHAND)).isEqualTo("player1");
		assertThat(info.getPlayerTime(Player.FOREHAND)).isEqualTo(10.0);
		assertThat(info.getPlayerName(Player.MIDDLEHAND)).isEqualTo("player2");
		assertThat(info.getPlayerTime(Player.MIDDLEHAND)).isEqualTo(15.0);
		assertThat(info.getPlayerName(Player.REARHAND)).isEqualTo("player3");
		assertThat(info.getPlayerTime(Player.REARHAND)).isEqualTo(20.0);
	}

	@Test
	public void testGetMoveInformation_HoldBid() {
		List<String> params = new ArrayList<String>();
		params.add("0");
		params.add("y");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertThat(info.getType()).isEqualTo(MoveType.HOLD_BID);
		assertThat(info.getMovePlayer()).isEqualTo(MovePlayer.FOREHAND);
	}

	@Test
	public void testGetMoveInformation_Pass() {
		List<String> params = new ArrayList<String>();
		params.add("1");
		params.add("p");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertThat(info.getType()).isEqualTo(MoveType.PASS);
		assertThat(info.getMovePlayer()).isEqualTo(MovePlayer.MIDDLEHAND);
	}

	@Test
	public void testGetMoveInformation_SkatRequest() {
		List<String> params = new ArrayList<String>();
		params.add("2");
		params.add("s");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertThat(info.getType()).isEqualTo(MoveType.SKAT_REQUEST);
		assertThat(info.getMovePlayer()).isEqualTo(MovePlayer.REARHAND);
	}

	@Test
	public void testGetMoveInformation_PickUpSkat() {
		List<String> params = new ArrayList<String>();
		params.add("w");
		params.add("??.??");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertThat(info.getType()).isEqualTo(MoveType.PICK_UP_SKAT);
		assertThat(info.getMovePlayer()).isEqualTo(MovePlayer.WORLD);
	}

	@Test
	public void testGetMoveInformation_TimeOut() {
		List<String> params = new ArrayList<String>();
		params.add("0");
		params.add("TI.0");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertThat(info.getType()).isEqualTo(MoveType.TIME_OUT);
		assertThat(info.getTimeOutPlayer()).isEqualTo(Player.FOREHAND);
	}

	@Test
	public void testGetMoveInformation_Resign() {
		List<String> params = new ArrayList<String>();
		params.add("1");
		params.add("RE");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertThat(info.getType()).isEqualTo(MoveType.RESIGN);
		assertThat(info.getMovePlayer()).isEqualTo(MovePlayer.MIDDLEHAND);
	}

	@Test
	public void testGetMoveInformation_ShowCards() {
		List<String> params = new ArrayList<String>();
		params.add("2");
		params.add("SC.C7.HA");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertThat(info.getType()).isEqualTo(MoveType.SHOW_CARDS);
		assertThat(info.getMovePlayer()).isEqualTo(MovePlayer.REARHAND);
		assertThat(info.getOuvertCards().size()).isEqualTo(2);
	}

	@Test
	public void testGetMoveInformation_LeaveTable() {
		List<String> params = new ArrayList<String>();
		params.add("0");
		params.add("LE.0");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertThat(info.getType()).isEqualTo(MoveType.LEAVE_TABLE);
		assertThat(info.getLeavingPlayer()).isEqualTo(Player.FOREHAND);
	}

	@Test
	public void testGetMoveInformation_CardPlay() {
		List<String> params = new ArrayList<String>();
		params.add("0");
		params.add("C7");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertThat(info.getType()).isEqualTo(MoveType.CARD_PLAY);
		assertThat(info.getMovePlayer()).isEqualTo(MovePlayer.FOREHAND);
		assertThat(info.getCard()).isEqualTo(Card.C7);
	}

	@Test
	public void testGetMoveInformation_Bid() {
		List<String> params = new ArrayList<String>();
		params.add("1");
		params.add("20");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertThat(info.getType()).isEqualTo(MoveType.BID);
		assertThat(info.getMovePlayer()).isEqualTo(MovePlayer.MIDDLEHAND);
		assertThat(info.getBidValue()).isEqualTo(20);
	}

	@Test
	public void testGetMoveInformation_Deal() {
		List<String> params = new ArrayList<String>();
		params.add("2");
		params.add("CQ.H7.C8.C9.CT.SQ.DK.H8.H9.SA.S9.HK.HQ.DJ.CJ.S8.DT.HT.ST.D7.CK.S7.C7.DQ.SJ.HA.CA.DA.D8.D9.SK.HJ");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertThat(info.getType()).isEqualTo(MoveType.DEAL);
		assertThat(info.getMovePlayer()).isEqualTo(MovePlayer.REARHAND);
		assertThat(info.getCards(Player.FOREHAND).size()).isEqualTo(10);
		assertThat(info.getCards(Player.MIDDLEHAND).size()).isEqualTo(10);
		assertThat(info.getCards(Player.REARHAND).size()).isEqualTo(10);
		assertThat(info.getSkat().size()).isEqualTo(2);
	}

	@Test
	public void testGetMoveInformation_GameAnnouncement() {
		List<String> params = new ArrayList<String>();
		params.add("0");
		params.add("G.O.H.S.Z.C7.HA");

		MoveInformation info = MessageParser.getMoveInformation(params);
		assertThat(info.getType()).isEqualTo(MoveType.GAME_ANNOUNCEMENT);
		assertThat(info.getMovePlayer()).isEqualTo(MovePlayer.FOREHAND);
		GameAnnouncement announcement = info.getGameAnnouncement();
		assertThat(announcement.getGameType()).isEqualTo(GameType.GRAND);
		assertThat(announcement.isOuvert()).isTrue();
		assertThat(announcement.isHand()).isTrue();
		assertThat(announcement.isSchneider()).isTrue();
		assertThat(announcement.isSchwarz()).isTrue();
		assertThat(announcement.getDiscardedCards().size()).isEqualTo(2);
	}

	@Test
	public void testParsePlayerTimes() {
		List<String> params = new ArrayList<String>();
		params.add("10.0");
		params.add("15.0");
		params.add("20.0");

		MoveInformation info = new MoveInformation();
		MessageParser.parsePlayerTimes(params, info);
		assertThat(info.getPlayerTime(Player.FOREHAND)).isEqualTo(10.0);
		assertThat(info.getPlayerTime(Player.MIDDLEHAND)).isEqualTo(15.0);
		assertThat(info.getPlayerTime(Player.REARHAND)).isEqualTo(20.0);
	}

	@Test
	public void testParseSkatCards() {
		String move = "C7.HA";
		CardList cards = MessageParser.parseSkatCards(move);
		assertThat(cards.size()).isEqualTo(2);
		assertThat(cards.get(0)).isEqualTo(Card.C7);
		assertThat(cards.get(1)).isEqualTo(Card.HA);
	}

	@Test
	public void testGetMovePlayer_World() {
		MoveInformation info = new MoveInformation();
		MessageParser.getMovePlayer("w", info);
		assertThat(info.getMovePlayer()).isEqualTo(MovePlayer.WORLD);
	}

	@Test
	public void testGetMovePlayer_Forehand() {
		MoveInformation info = new MoveInformation();
		MessageParser.getMovePlayer("0", info);
		assertThat(info.getMovePlayer()).isEqualTo(MovePlayer.FOREHAND);
	}

	@Test
	public void testGetMovePlayer_Middlehand() {
		MoveInformation info = new MoveInformation();
		MessageParser.getMovePlayer("1", info);
		assertThat(info.getMovePlayer()).isEqualTo(MovePlayer.MIDDLEHAND);
	}

	@Test
	public void testGetMovePlayer_Rearhand() {
		MoveInformation info = new MoveInformation();
		MessageParser.getMovePlayer("2", info);
		assertThat(info.getMovePlayer()).isEqualTo(MovePlayer.REARHAND);
	}

	@Test
	public void testParseGameAnnoucement_Grand() {
		MoveInformation info = new MoveInformation();
		GameAnnouncement announcement = MessageParser.parseGameAnnoucement(info, "G");
		assertThat(announcement.getGameType()).isEqualTo(GameType.GRAND);
	}

	@Test
	public void testParseGameAnnoucement_Clubs() {
		MoveInformation info = new MoveInformation();
		GameAnnouncement announcement = MessageParser.parseGameAnnoucement(info, "C");
		assertThat(announcement.getGameType()).isEqualTo(GameType.CLUBS);
	}

	@Test
	public void testParseGameAnnoucement_Spades() {
		MoveInformation info = new MoveInformation();
		GameAnnouncement announcement = MessageParser.parseGameAnnoucement(info, "S");
		assertThat(announcement.getGameType()).isEqualTo(GameType.SPADES);
	}

	@Test
	public void testParseGameAnnoucement_Hearts() {
		MoveInformation info = new MoveInformation();
		GameAnnouncement announcement = MessageParser.parseGameAnnoucement(info, "H");
		assertThat(announcement.getGameType()).isEqualTo(GameType.HEARTS);
	}

	@Test
	public void testParseGameAnnoucement_Diamonds() {
		MoveInformation info = new MoveInformation();
		GameAnnouncement announcement = MessageParser.parseGameAnnoucement(info, "D");
		assertThat(announcement.getGameType()).isEqualTo(GameType.DIAMONDS);
	}

	@Test
	public void testParseGameAnnoucement_Null() {
		MoveInformation info = new MoveInformation();
		GameAnnouncement announcement = MessageParser.parseGameAnnoucement(info, "N");
		assertThat(announcement.getGameType()).isEqualTo(GameType.NULL);
	}

	@Test
	public void testParseCardDealFromISSMessage() {
		String move = "??.??.??.??.??.??.??.??.??|C9.S9.ST.S8.C9.DT.DQ.CJ.SA.HA|??.??.??.??.??.??.??|??.??";
		List<CardList> deals = MessageParser.parseCardDeal(move);
		assertThat(deals.size()).isEqualTo(4);
	}

	@Test
	public void testParseCardDealFromSummary() {
		String move = "CQ.H7.C8.C9.CT.SQ.DK.H8.H9.SA.S9.HK.HQ.DJ.CJ.S8.DT.HT.ST.D7.CK.S7.C7.DQ.SJ.HA.CA.DA.D8.D9.SK.HJ";
		List<CardList> deals = MessageParser.parseCardDeal(move);
		assertThat(deals.size()).isEqualTo(4);
		assertThat(deals.get(0).size()).isEqualTo(10);
		assertThat(deals.get(1).size()).isEqualTo(10);
		assertThat(deals.get(2).size()).isEqualTo(10);
		assertThat(deals.get(3).size()).isEqualTo(2);
	}

	@Test
	public void testParseHand() {
		String hand = "C7.HA.C9";
		CardList cards = MessageParser.parseHand(hand);
		assertThat(cards.size()).isEqualTo(3);
		assertThat(cards.get(0)).isEqualTo(Card.C7);
		assertThat(cards.get(1)).isEqualTo(Card.HA);
		assertThat(cards.get(2)).isEqualTo(Card.C9);
	}

	@Test
	public void testParseTimeOut() {
		Player player = MessageParser.parseTimeOut("TI.0");
		assertThat(player).isEqualTo(Player.FOREHAND);
	}

	@Test
	public void testParseLeaveTable() {
		Player player = MessageParser.parseLeaveTable("LE.1");
		assertThat(player).isEqualTo(Player.MIDDLEHAND);
	}

	@Test
	public void testExtractPlayer() {
		Player player = MessageParser.extractPlayer("LE.2");
		assertThat(player).isEqualTo(Player.REARHAND);
	}

	@Test
	public void testParseGameSummary() {
		String gameSummary = "P0[player1]P1[player2]P2[player3]MV[0 y 1 p 2 s]R[d:0 win v:120 p:120]";
		SkatGameData gameData = MessageParser.parseGameSummary(gameSummary);
		assertThat(gameData.getPlayerName(Player.FOREHAND)).isEqualTo("player1");
		assertThat(gameData.getPlayerName(Player.MIDDLEHAND)).isEqualTo("player2");
		assertThat(gameData.getPlayerName(Player.REARHAND)).isEqualTo("player3");
		assertThat(gameData.getGameMoves().size()).isEqualTo(1);
		assertThat(gameData.getTricks().size()).isEqualTo(1);
		assertThat(gameData.getResult().isWon()).isTrue();
		assertThat(gameData.getResult().getGameValue()).isEqualTo(120);
		assertThat(gameData.getDeclarerScore()).isEqualTo(120);
		assertThat(gameData.getResult().getFinalOpponentPoints()).isEqualTo(0);
	}

	@Test
	public void testParseSummaryPart_Player0() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseSummaryPart(result, "P0", "player1");
		assertThat(result.getPlayerName(Player.FOREHAND)).isEqualTo("player1");
	}

	@Test
	public void testParseSummaryPart_Player1() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseSummaryPart(result, "P1", "player2");
		assertThat(result.getPlayerName(Player.MIDDLEHAND)).isEqualTo("player2");
	}

	@Test
	public void testParseSummaryPart_Player2() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseSummaryPart(result, "P2", "player3");
		assertThat(result.getPlayerName(Player.REARHAND)).isEqualTo("player3");
	}

	@Test
	public void testParseSummaryPart_Moves() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseSummaryPart(result, "MV", "0 y 1 p 2 s");
		assertThat(result.getGameMoves().size()).isEqualTo(1);
		assertThat(result.getTricks().size()).isEqualTo(1);
	}

	@Test
	public void testParseSummaryPart_Result() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseSummaryPart(result, "R", "d:0 win v:120 p:120");
		assertThat(result.getResult().isWon()).isTrue();
		assertThat(result.getResult().getGameValue()).isEqualTo(120);
		assertThat(result.getDeclarerScore()).isEqualTo(120);
		assertThat(result.getResult().getFinalOpponentPoints()).isEqualTo(0);
	}

	@Test
	public void testParseMoves_Deal() {
		SkatGameData result = new SkatGameData();
		String summaryPart = "2 CQ.H7.C8.C9.CT.SQ.DK.H8.H9.SA.S9.HK.HQ.DJ.CJ.S8.DT.HT.ST.D7.CK.S7.C7.DQ.SJ.HA.CA.DA.D8.D9.SK.HJ";
		MessageParser.parseMoves(result, summaryPart);
		assertThat(result.getDealtCards().get(Player.FOREHAND).size()).isEqualTo(10);
		assertThat(result.getDealtCards().get(Player.MIDDLEHAND).size()).isEqualTo(10);
		assertThat(result.getDealtCards().get(Player.REARHAND).size()).isEqualTo(10);
		assertThat(result.getDealtSkat().size()).isEqualTo(2);
	}

	@Test
	public void testParseMoves_Bid() {
		SkatGameData result = new SkatGameData();
		String summaryPart = "0 20";
		MessageParser.parseMoves(result, summaryPart);
		assertThat(result.getGameMoves().size()).isEqualTo(1);
		assertThat(result.getGameMoves().get(0).getBidValue()).isEqualTo(20);
	}

	@Test
	public void testParseMoves_HoldBid() {
		SkatGameData result = new SkatGameData();
		String summaryPart = "0 y";
		MessageParser.parseMoves(result, summaryPart);
		assertThat(result.getGameMoves().size()).isEqualTo(1);
		assertThat(result.getGameMoves().get(0).getBidValue()).isEqualTo(0);
	}

	@Test
	public void testParseMoves_Pass() {
		SkatGameData result = new SkatGameData();
		String summaryPart = "1 p";
		MessageParser.parseMoves(result, summaryPart);
		assertThat(result.isPlayerPass(Player.MIDDLEHAND)).isTrue();
	}

	@Test
	public void testParseMoves_GameAnnouncement() {
		SkatGameData result = new SkatGameData();
		String summaryPart = "0 G.O.H.S.Z.C7.HA";
		MessageParser.parseMoves(result, summaryPart);
		assertThat(result.getAnnoucement().getGameType()).isEqualTo(GameType.GRAND);
		assertThat(result.getAnnoucement().isOuvert()).isTrue();
		assertThat(result.getAnnoucement().isHand()).isTrue();
		assertThat(result.getAnnoucement().isSchneider()).isTrue();
		assertThat(result.getAnnoucement().isSchwarz()).isTrue();
		assertThat(result.getAnnoucement().getDiscardedCards().size()).isEqualTo(2);
	}

	@Test
	public void testParseMoves_CardPlay() {
		SkatGameData result = new SkatGameData();
		String summaryPart = "0 C7";
		MessageParser.parseMoves(result, summaryPart);
		assertThat(result.getTricks().size()).isEqualTo(1);
		assertThat(result.getTricks().get(0).getCardList().size()).isEqualTo(1);
		assertThat(result.getTricks().get(0).getCardList().get(0)).isEqualTo(Card.C7);
	}

	@Test
	public void testParseGameResult_Loss() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseGameResult(result, "loss");
		assertThat(result.getResult().isWon()).isFalse();
	}

	@Test
	public void testParseGameResult_Win() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseGameResult(result, "win");
		assertThat(result.getResult().isWon()).isTrue();
	}

	@Test
	public void testParseGameResult_GameValue() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseGameResult(result, "v:120");
		assertThat(result.getResult().getGameValue()).isEqualTo(120);
	}

	@Test
	public void testParseGameResult_DeclarerPoints() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseGameResult(result, "p:120");
		assertThat(result.getDeclarerScore()).isEqualTo(120);
		assertThat(result.getResult().getFinalOpponentPoints()).isEqualTo(0);
	}

	@Test
	public void testParseGameResult_Overbid() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseGameResult(result, "overbid");
		assertThat(result.getResult().isOverBidded()).isTrue();
	}

	@Test
	public void testParseGameResult_Schneider() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseGameResult(result, "s:1");
		assertThat(result.getResult().isSchneider()).isTrue();
	}

	@Test
	public void testParseGameResult_Schwarz() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseGameResult(result, "z:1");
		assertThat(result.getResult().isSchwarz()).isTrue();
	}

	@Test
	public void testParseDeclarerToken_Forehand() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseDeclarerToken(result, "d:0");
		assertThat(result.getDeclarer()).isEqualTo(Player.FOREHAND);
	}

	@Test
	public void testParseDeclarerToken_Middlehand() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseDeclarerToken(result, "d:1");
		assertThat(result.getDeclarer()).isEqualTo(Player.MIDDLEHAND);
	}

	@Test
	public void testParseDeclarerToken_Rearhand() {
		SkatGameData result = new SkatGameData();
		MessageParser.parseDeclarerToken(result, "d:2");
		assertThat(result.getDeclarer()).isEqualTo(Player.REARHAND);
	}

	@Test
	public void testGetTableChatMessage() {
		List<String> detailParams = new ArrayList<String>();
		detailParams.add("foo");
		detailParams.add("asdf");
		detailParams.add("jklö");

		ChatMessage message = MessageParser.getTableChatMessage("table1", detailParams);
		assertThat(message.getChatName()).isEqualTo("table1");
		assertThat(message.getMessage()).isEqualTo("foo: asdf jklö ");
	}
}
