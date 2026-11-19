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
import com.helger.phase4.model.pmode.PModeBusinessInformation;
import com.helger.phase4.model.pmode.PModeParty;
import com.helger.phase4.model.pmode.PModeReceptionAwareness;
import com.helger.phase4.model.pmode.leg.EPModeSendReceiptReplyPattern;
import com.helger.phase4.model.pmode.leg.PModeLegBusinessInformation;
import com.helger.phase4.model.pmode.leg.PModeLegErrorHandling;
import com.helger.phase4.model.pmode.leg.PModeLegProtocol;
import com.helger.phase4.model.pmode.leg.PModeLegSecurity;
import com.helger.phase4.wss.EWSSVersion;

public class PeppolPMode_ArrowTest
{
  @Test
  public void testConstants ()
  {
    assertEquals ("urn:fdc:peppol.eu:2017:agreements:tia:ap_provider", PeppolPMode.DEFAULT_AGREEMENT_ID);
    assertEquals ("urn:fdc:peppol.eu:2017:identifiers:ap", PeppolPMode.DEFAULT_PARTY_TYPE_ID);
  }

  @Test
  public void testGeneratePModeLegProtocol ()
  {
    final PModeLegProtocol aProtocol = PeppolPMode.generatePModeLegProtocol ("https://example.org/as4");
    assertNotNull (aProtocol);
  }

  @Test
  public void testGeneratePModeLegProtocolWithNullAddress ()
  {
    final PModeLegProtocol aProtocol = PeppolPMode.generatePModeLegProtocol (null);
    assertNotNull (aProtocol);
  }

  @Test
  public void testGeneratePModeLegBusinessInformation ()
  {
    final PModeLegBusinessInformation aInfo = PeppolPMode.generatePModeLegBusinessInformation ();
    assertNotNull (aInfo);
  }

  @Test
  public void testGeneratePModeLegErrorHandling ()
  {
    final PModeLegErrorHandling aHandling = PeppolPMode.generatePModeLegErrorHandling ();
    assertNotNull (aHandling);
  }

  @Test
  public void testGeneratePModeLegSecurity ()
  {
    final PModeLegSecurity aSecurity = PeppolPMode.generatePModeLegSecurity ();
    assertNotNull (aSecurity);
  }

  @Test
  public void testGeneratePModeLeg ()
  {
    assertNotNull (PeppolPMode.generatePModeLeg ("https://example.org/as4"));
  }

  @Test
  public void testGeneratePModeReceptionAwareness ()
  {
    final PModeReceptionAwareness aAwareness = PeppolPMode.generatePModeReceptionAwareness ();
    assertNotNull (aAwareness);
  }

  @Test
  public void testCreateParty ()
  {
    final PModeParty aParty = PeppolPMode.createParty ("party-id", "role");
    assertNotNull (aParty);
  }
}
