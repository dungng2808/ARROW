package com.helger.phase4.profile.peppol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.helger.commons.state.ETriState;
import com.helger.phase4.CAS4;
import com.helger.phase4.crypto.ECryptoAlgorithmCrypt;
import com.helger.phase4.crypto.ECryptoAlgorithmSign;
import com.helger.phase4.crypto.ECryptoAlgorithmSignDigest;
import com.helger.phase4.model.pmode.PMode;
import com.helger.phase4.model.pmode.PModeParty;
import com.helger.phase4.model.pmode.PModeReceptionAwareness;
import com.helger.phase4.model.pmode.leg.EPModeSendReceiptReplyPattern;
import com.helger.phase4.model.pmode.leg.PModeLeg;
import com.helger.phase4.model.pmode.leg.PModeLegBusinessInformation;
import com.helger.phase4.model.pmode.leg.PModeLegErrorHandling;
import com.helger.phase4.model.pmode.leg.PModeLegProtocol;
import com.helger.phase4.model.pmode.leg.PModeLegSecurity;
import com.helger.phase4.wss.EWSSVersion;

public class PeppolPMode_ArrowTest
{
  @Test
  public void testGeneratePModeLegProtocol ()
  {
    final PModeLegProtocol protocol = PeppolPMode.generatePModeLegProtocol ("https://example.org/as4");
    assertNotNull (protocol);
    assertEquals ("https://example.org/as4", protocol.getAddress ());
  }

  @Test
  public void testGeneratePModeLegProtocolWithNullAddress ()
  {
    final PModeLegProtocol protocol = PeppolPMode.generatePModeLegProtocol (null);
    assertNotNull (protocol);
    assertNull (protocol.getAddress ());
  }

  @Test
  public void testGeneratePModeLegBusinessInformation ()
  {
    final PModeLegBusinessInformation information = PeppolPMode.generatePModeLegBusinessInformation ();
    assertNotNull (information);
    assertEquals (CAS4.DEFAULT_MPC_ID, information.getMPCID ());
  }

  @Test
  public void testGeneratePModeLegErrorHandling ()
  {
    final PModeLegErrorHandling handling = PeppolPMode.generatePModeLegErrorHandling ();
    assertNotNull (handling);
    assertSame (ETriState.TRUE, handling.getReportAsResponse ());
    assertSame (ETriState.TRUE, handling.getReportProcessErrorNotifyConsumer ());
    assertSame (ETriState.TRUE, handling.getReportProcessErrorNotifyProducer ());
    assertSame (ETriState.TRUE, handling.getReportDeliveryFailuresNotifyProducer ());
  }

  @Test
  public void testGeneratePModeLegSecurity ()
  {
    final PModeLegSecurity security = PeppolPMode.generatePModeLegSecurity ();
    assertNotNull (security);
    assertEquals (EWSSVersion.WSS_111, security.getWSSVersion ());
    assertEquals (ECryptoAlgorithmSign.RSA_SHA_256, security.getX509SignatureAlgorithm ());
    assertEquals (ECryptoAlgorithmSignDigest.DIGEST_SHA_256, security.getX509SignatureHashFunction ());
    assertEquals (ECryptoAlgorithmCrypt.AES_128_GCM, security.getX509EncryptionAlgorithm ());
    assertEquals (Integer.valueOf (128), security.getX509EncryptionMinimumStrength ());
    assertTrue (!security.isPModeAuthorize ());
    assertTrue (security.isSendReceipt ());
    assertTrue (security.isSendReceiptNonRepudiation ());
    assertEquals (EPModeSendReceiptReplyPattern.RESPONSE, security.getSendReceiptReplyPattern ());
  }

  @Test
  public void testGeneratePModeLeg ()
  {
    final PModeLeg leg = PeppolPMode.generatePModeLeg ("https://example.org/as4");
    assertNotNull (leg);
    assertNotNull (leg.getProtocol ());
    assertNotNull (leg.getBusinessInformation ());
    assertNotNull (leg.getErrorHandling ());
    assertNull (leg.getReliability ());
    assertNotNull (leg.getSecurity ());
  }

  @Test
  public void testGeneratePModeReceptionAwareness ()
  {
    final PModeReceptionAwareness awareness = PeppolPMode.generatePModeReceptionAwareness ();
    assertNotNull (awareness);
    assertSame (ETriState.TRUE, awareness.getReceptionAwareness ());
    assertSame (ETriState.TRUE, awareness.getRetry ());
    assertEquals (1, awareness.getMaxRetries ());
    assertEquals (10000, awareness.getRetryIntervalMS ());
    assertSame (ETriState.TRUE, awareness.getDuplicateDetection ());
  }

  @Test
  public void testCreateParty ()
  {
    final PModeParty party = PeppolPMode.createParty ("party-id", "party-role");
    assertNotNull (party);
    assertEquals (PeppolPMode.DEFAULT_PARTY_TYPE_ID, party.getPartyType ());
    assertEquals ("party-id", party.getPartyID ());
    assertEquals ("party-role", party.getRole ());
  }

  @Test
  public void testCreatePeppolPMode ()
  {
    final PMode mode = PeppolPMode.createPeppolPMode ("initiator-id",
                                                       "responder-id",
                                                       "https://example.org/as4",
                                                       (initiatorID, responderID) -> "test-pmode",
                                                       false);
    assertNotNull (mode);
    assertEquals ("test-pmode", mode.getID ());
    assertEquals (PeppolPMode.DEFAULT_AGREEMENT_ID, mode.getAgreement ());
    assertEquals ("initiator-id", mode.getInitiator ().getPartyID ());
    assertEquals ("responder-id", mode.getResponder ().getPartyID ());
    assertNotNull (mode.getLeg1 ());
    assertNull (mode.getLeg2 ());
    assertNotNull (mode.getReceptionAwareness ());
  }
}
