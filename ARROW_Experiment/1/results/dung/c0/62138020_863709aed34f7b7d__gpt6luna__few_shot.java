package com.helger.phase4.profile.peppol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import com.helger.phase4.model.pmode.PModeReceptionAwareness;
import com.helger.phase4.model.pmode.leg.EPModeSendReceiptReplyPattern;
import com.helger.phase4.model.pmode.leg.PModeLeg;
import com.helger.phase4.model.pmode.leg.PModeLegBusinessInformation;
import com.helger.phase4.model.pmode.leg.PModeLegErrorHandling;
import com.helger.phase4.model.pmode.leg.PModeLegProtocol;
import com.helger.phase4.model.pmode.leg.PModeLegSecurity;
import com.helger.phase4.wss.EWSSVersion;
import org.junit.Test;

public class PeppolPMode_ArrowTest
{
  @Test
  public void generatePModeLegProtocolUsesAddress ()
  {
    final PModeLegProtocol protocol = PeppolPMode.generatePModeLegProtocol ("https://example.test/as4");
    assertNotNull (protocol);
    assertEquals ("https://example.test/as4", protocol.getAddress ());
  }

  @Test
  public void generatePModeLegProtocolAcceptsNullAddress ()
  {
    final PModeLegProtocol protocol = PeppolPMode.generatePModeLegProtocol (null);
    assertNotNull (protocol);
    assertNull (protocol.getAddress ());
  }

  @Test
  public void generatePModeLegBusinessInformationUsesDefaultMpc ()
  {
    final PModeLegBusinessInformation information = PeppolPMode.generatePModeLegBusinessInformation ();
    assertNotNull (information);
    assertEquals (CAS4.DEFAULT_MPC_ID, information.getMPCID ());
  }

  @Test
  public void generatePModeLegErrorHandlingEnablesNotifications ()
  {
    final PModeLegErrorHandling handling = PeppolPMode.generatePModeLegErrorHandling ();
    assertNotNull (handling);
    assertEquals (ETriState.TRUE, handling.getReportAsResponse ());
    assertEquals (ETriState.TRUE, handling.getReportProcessErrorNotifyConsumer ());
    assertEquals (ETriState.TRUE, handling.getReportProcessErrorNotifyProducer ());
    assertEquals (ETriState.TRUE, handling.getReportDeliveryFailuresNotifyProducer ());
  }

  @Test
  public void generatePModeLegSecurityUsesPeppolDefaults ()
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
  public void generatePModeLegIncludesConfiguredComponents ()
  {
    final PModeLeg leg = PeppolPMode.generatePModeLeg ("https://example.test/as4");
    assertNotNull (leg);
    assertNotNull (leg.getProtocol ());
    assertEquals ("https://example.test/as4", leg.getProtocol ().getAddress ());
    assertNotNull (leg.getBusinessInformation ());
    assertNotNull (leg.getErrorHandling ());
    assertNotNull (leg.getSecurity ());
    assertNull (leg.getReliability ());
  }

  @Test
  public void generatePModeReceptionAwarenessUsesRetryDefaults ()
  {
    final PModeReceptionAwareness awareness = PeppolPMode.generatePModeReceptionAwareness ();
    assertNotNull (awareness);
    assertEquals (ETriState.TRUE, awareness.getReceptionAwareness ());
    assertEquals (ETriState.TRUE, awareness.getRetry ());
    assertEquals (1, awareness.getMaxRetries ());
    assertEquals (10000, awareness.getRetryIntervalMS ());
    assertEquals (ETriState.TRUE, awareness.getDuplicateDetection ());
  }

  @Test
  public void createPartyUsesPeppolPartyType ()
  {
    final PModeParty party = PeppolPMode.createParty ("party-id", "party-role");
    assertNotNull (party);
    assertEquals (PeppolPMode.DEFAULT_PARTY_TYPE_ID, party.getPartyType ());
    assertEquals ("party-id", party.getPartyID ());
    assertEquals ("party-role", party.getRole ());
  }

  @Test
  public void createPeppolPModeCreatesOneWayPushMode ()
  {
    final IPModeIDProvider provider = new IPModeIDProvider ()
    {
      @Override
      public String getPModeID (final String initiatorID, final String responderID)
      {
        return initiatorID + "-" + responderID;
      }
    };

    final PMode mode = PeppolPMode.createPeppolPMode ("initiator", "responder", "https://example.test/as4", provider, false);
    assertNotNull (mode);
    assertEquals ("initiator-responder", mode.getID ());
    assertEquals (PeppolPMode.DEFAULT_AGREEMENT_ID, mode.getAgreement ());
    assertEquals (EMEP.ONE_WAY, mode.getMEP ());
    assertEquals (EMEPBinding.PUSH, mode.getMEPBinding ());
    assertNotNull (mode.getLeg1 ());
    assertEquals ("https://example.test/as4", mode.getLeg1 ().getProtocol ().getAddress ());
    assertNull (mode.getLeg2 ());
    assertNotNull (mode.getReceptionAwareness ());
  }
}
