package com.helger.phase4.profile.peppol;

import static org.junit.Assert.*;

import org.junit.Test;

import com.helger.commons.state.ETriState;
import com.helger.phase4.CAS4;
import com.helger.phase4.crypto.ECryptoAlgorithmCrypt;
import com.helger.phase4.crypto.ECryptoAlgorithmSign;
import com.helger.phase4.crypto.ECryptoAlgorithmSignDigest;
import com.helger.phase4.model.EMEP;
import com.helger.phase4.model.EMEPBinding;
import com.helger.phase4.model.pmode.IPModeIDProvider;
import com.helger.phase4.model.pmode.PMode;
import com.helger.phase4.model.pmode.PModeParty;
import com.helger.phase4.model.pmode.PModePayloadService;
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
    final String address = "http://example.com";
    final PModeLegProtocol protocol = PeppolPMode.generatePModeLegProtocol (address);
    assertNotNull (protocol);
    assertEquals (address, protocol.getEndpoint ());
  }

  @Test
  public void testGeneratePModeLegBusinessInformation ()
  {
    final PModeLegBusinessInformation businessInfo = PeppolPMode.generatePModeLegBusinessInformation ();
    assertNotNull (businessInfo);
    assertNull (businessInfo.getService ());
    assertNull (businessInfo.getServiceType ());
    assertNull (businessInfo.getAction ());
    assertNull (businessInfo.getPayloadProfile ());
    assertNull (businessInfo.getPayloadProfileMaxKB ());
    assertEquals (CAS4.DEFAULT_MPC_ID, businessInfo.getMPCID ());
  }

  @Test
  public void testGeneratePModeLegErrorHandling ()
  {
    final PModeLegErrorHandling errorHandling = PeppolPMode.generatePModeLegErrorHandling ();
    assertNotNull (errorHandling);
    assertNull (errorHandling.getReportSenderErrorsTo ());
    assertNull (errorHandling.getReportReceiverErrorsTo ());
    assertEquals (ETriState.TRUE, errorHandling.getReportAsResponse ());
    assertEquals (ETriState.TRUE, errorHandling.getReportProcessErrorNotifyConsumer ());
    assertEquals (ETriState.TRUE, errorHandling.getReportProcessErrorNotifyProducer ());
    assertEquals (ETriState.TRUE, errorHandling.getReportDeliveryFailuresNotifyProducer ());
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
    assertFalse (security.isPModeAuthorize ());
    assertTrue (security.isSendReceipt ());
    assertTrue (security.isSendReceiptNonRepudiation ());
    assertEquals (EPModeSendReceiptReplyPattern.RESPONSE, security.getSendReceiptReplyPattern ());
  }

  @Test
  public void testGeneratePModeLeg ()
  {
    final String responderAddress = "http://responder.example.com";
    final PModeLeg leg = PeppolPMode.generatePModeLeg (responderAddress);
    assertNotNull (leg);
    assertEquals (responderAddress, leg.getProtocol ().getEndpoint ());
    assertNotNull (leg.getBusinessInformation ());
    assertNotNull (leg.getErrorHandling ());
    assertNotNull (leg.getSecurity ());
  }

  @Test
  public void testGeneratePModeReceptionAwareness ()
  {
    final PModeReceptionAwareness receptionAwareness = PeppolPMode.generatePModeReceptionAwareness ();
    assertNotNull (receptionAwareness);
    assertEquals (ETriState.TRUE, receptionAwareness.getReceptionAwareness ());
    assertEquals (ETriState.TRUE, receptionAwareness.getRetry ());
    assertEquals (1, receptionAwareness.getMaxRetries ());
    assertEquals (10_000, receptionAwareness.getRetryIntervalMS ());
    assertEquals (ETriState.TRUE, receptionAwareness.getDuplicateDetection ());
  }

  @Test
  public void testCreateParty ()
  {
    final String partyID = "party123";
    final String role = "role456";
    final PModeParty party = PeppolPMode.createParty (partyID, role);
    assertNotNull (party);
    assertEquals (PeppolPMode.DEFAULT_PARTY_TYPE_ID, party.getPartyTypeID ());
    assertEquals (partyID, party.getPartyID ());
    assertEquals (role, party.getRole ());
    assertNull (party.getPartyName ());
    assertNull (party.getPartyAddress ());
  }

  @Test
  public void testCreatePeppolPMode ()
  {
    final String initiatorID = "initiator123";
    final String responderID = "responder456";
    final String responderAddress = "http://responder.example.com";
    final IPModeIDProvider idProvider = new IPModeIDProvider ()
    {
      @Override
      public String getPModeID (final String sInitiatorID, final String sResponderID)
      {
        return "pmode-" + sInitiatorID + "-" + sResponderID;
      }
    };

    final PMode pmode = PeppolPMode.createPeppolPMode (initiatorID, responderID, responderAddress, idProvider, false);
    assertNotNull (pmode);
    assertEquals ("pmode-initiator123-responder456", pmode.getID ());
    assertNotNull (pmode.getInitiator ());
    assertNotNull (pmode.getResponder ());
    assertEquals (PeppolPMode.DEFAULT_AGREEMENT_ID, pmode.getAgreementID ());
    assertEquals (EMEP.ONE_WAY, pmode.getMEP ());
    assertEquals (EMEPBinding.PUSH, pmode.getMEPBinding ());
    assertNotNull (pmode.getLeg1 ());
    assertNull (pmode.getLeg2 ());
    assertNull (pmode.getPayloadService ());
    assertNotNull (pmode.getReceptionAwareness ());
  }
}
