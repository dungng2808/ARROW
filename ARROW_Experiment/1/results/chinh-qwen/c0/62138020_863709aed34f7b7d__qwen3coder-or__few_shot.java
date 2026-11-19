package com.helger.phase4.profile.peppol;

import static org.junit.Assert.*;

import org.junit.Test;

import com.helger.commons.state.ETriState;
import com.helger.phase4.CAS4;
import com.helger.phase4.model.pmode.PMode;
import com.helger.phase4.model.pmode.PModeParty;
import com.helger.phase4.model.pmode.leg.PModeLeg;
import com.helger.phase4.model.pmode.leg.PModeLegBusinessInformation;
import com.helger.phase4.model.pmode.leg.PModeLegErrorHandling;
import com.helger.phase4.model.pmode.leg.PModeLegProtocol;
import com.helger.phase4.model.pmode.leg.PModeLegSecurity;
import com.helger.phase4.model.pmode.leg.PModeReceptionAwareness;

public class PeppolPMode_ArrowTest {

    @Test
    public void testGeneratePModeLegProtocol() {
        String address = "http://example.com";
        PModeLegProtocol protocol = PeppolPMode.generatePModeLegProtocol(address);
        assertNotNull(protocol);
        assertEquals(address, protocol.getEndpoint());
    }

    @Test
    public void testGeneratePModeLegProtocolNullAddress() {
        PModeLegProtocol protocol = PeppolPMode.generatePModeLegProtocol(null);
        assertNotNull(protocol);
        assertNull(protocol.getEndpoint());
    }

    @Test
    public void testGeneratePModeLegBusinessInformation() {
        PModeLegBusinessInformation businessInfo = PeppolPMode.generatePModeLegBusinessInformation();
        assertNotNull(businessInfo);
        assertNull(businessInfo.getService());
        assertNull(businessInfo.getServiceType());
        assertNull(businessInfo.getAction());
        assertNull(businessInfo.getPayloadProfile());
        assertNull(businessInfo.getPayloadProfileMaxKB());
        assertEquals(CAS4.DEFAULT_MPC_ID, businessInfo.getMPCID());
    }

    @Test
    public void testGeneratePModeLegErrorHandling() {
        PModeLegErrorHandling errorHandling = PeppolPMode.generatePModeLegErrorHandling();
        assertNotNull(errorHandling);
        assertNull(errorHandling.getReportSenderErrorsTo());
        assertNull(errorHandling.getReportReceiverErrorsTo());
        assertEquals(ETriState.TRUE, errorHandling.getReportAsResponse());
        assertEquals(ETriState.TRUE, errorHandling.getReportProcessErrorNotifyConsumer());
        assertEquals(ETriState.TRUE, errorHandling.getReportProcessErrorNotifyProducer());
        assertEquals(ETriState.TRUE, errorHandling.getReportDeliveryFailuresNotifyProducer());
    }

    @Test
    public void testGeneratePModeLegSecurity() {
        PModeLegSecurity security = PeppolPMode.generatePModeLegSecurity();
        assertNotNull(security);
        assertEquals(com.helger.phase4.wss.EWSSVersion.WSS_111, security.getWSSVersion());
        assertEquals(com.helger.phase4.crypto.ECryptoAlgorithmSign.RSA_SHA_256, security.getX509SignatureAlgorithm());
        assertEquals(com.helger.phase4.crypto.ECryptoAlgorithmSignDigest.DIGEST_SHA_256, security.getX509SignatureHashFunction());
        assertEquals(com.helger.phase4.crypto.ECryptoAlgorithmCrypt.AES_128_GCM, security.getX509EncryptionAlgorithm());
        assertEquals(Integer.valueOf(128), security.getX509EncryptionMinimumStrength());
        assertFalse(security.isPModeAuthorize());
        assertTrue(security.isSendReceipt());
        assertTrue(security.isSendReceiptNonRepudiation());
        assertEquals(com.helger.phase4.model.pmode.leg.EPModeSendReceiptReplyPattern.RESPONSE, security.getSendReceiptReplyPattern());
    }

    @Test
    public void testGeneratePModeLeg() {
        String responderAddress = "http://responder.example.com";
        PModeLeg leg = PeppolPMode.generatePModeLeg(responderAddress);
        assertNotNull(leg);
        assertNotNull(leg.getProtocol());
        assertNotNull(leg.getBusinessInformation());
        assertNotNull(leg.getErrorHandling());
        assertNotNull(leg.getSecurity());
        assertNull(leg.getReliability());
    }

    @Test
    public void testGeneratePModeReceptionAwareness() {
        PModeReceptionAwareness receptionAwareness = PeppolPMode.generatePModeReceptionAwareness();
        assertNotNull(receptionAwareness);
        assertEquals(ETriState.TRUE, receptionAwareness.getReceptionAwareness());
        assertEquals(ETriState.TRUE, receptionAwareness.getRetry());
        assertEquals(1, receptionAwareness.getMaxRetries());
        assertEquals(10000, receptionAwareness.getRetryIntervalMS());
        assertEquals(ETriState.TRUE, receptionAwareness.getDuplicateDetection());
    }

    @Test
    public void testCreateParty() {
        String partyID = "party123";
        String role = "role456";
        PModeParty party = PeppolPMode.createParty(partyID, role);
        assertNotNull(party);
        assertEquals(PeppolPMode.DEFAULT_PARTY_TYPE_ID, party.getPartyTypeID());
        assertEquals(partyID, party.getPartyID());
        assertEquals(role, party.getRole());
        assertNull(party.getPartyName());
        assertNull(party.getPartyAddress());
    }

    @Test
    public void testCreatePeppolPMode() {
        String initiatorID = "initiator123";
        String responderID = "responder456";
        String responderAddress = "http://responder.example.com";
        
        // Using a simple mock implementation of IPModeIDProvider
        com.helger.phase4.model.pmode.IPModeIDProvider idProvider = 
            new com.helger.phase4.model.pmode.IPModeIDProvider() {
                @Override
                public String getPModeID(String sInitiatorID, String sResponderID) {
                    return "test-pmode-id";
                }
            };
        
        PMode pmode = PeppolPMode.createPeppolPMode(initiatorID, responderID, responderAddress, idProvider, false);
        assertNotNull(pmode);
        assertEquals("test-pmode-id", pmode.getID());
        assertNotNull(pmode.getInitiator());
        assertNotNull(pmode.getResponder());
        assertEquals(PeppolPMode.DEFAULT_AGREEMENT_ID, pmode.getAgreementID());
        assertEquals(com.helger.phase4.model.EMEP.ONE_WAY, pmode.getMEP());
        assertEquals(com.helger.phase4.model.EMEPBinding.PUSH, pmode.getMEPBinding());
        assertNotNull(pmode.getLeg1());
        assertNull(pmode.getLeg2());
        assertNull(pmode.getPayloadService());
        assertNotNull(pmode.getReceptionAwareness());
    }
}
