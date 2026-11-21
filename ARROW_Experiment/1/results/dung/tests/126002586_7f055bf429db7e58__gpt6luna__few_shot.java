package org.openjavacard.gp.scp;

import javax.smartcardio.CardException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SCPProtocolPolicy_ArrowTest {

    @Test
    public void permissivePolicyAllowsAnyVersionAndParameters() {
        SCPProtocolPolicy policy = SCPProtocolPolicy.PERMISSIVE;

        assertTrue(policy.isVersionAllowed(1));
        assertTrue(policy.isParametersAllowed(255));
        assertTrue(policy.isProtocolAllowed(2, 3));
        assertEquals("SCP*", policy.toString());
    }

    @Test
    public void fixedVersionPolicyAllowsOnlyItsVersion() {
        SCPProtocolPolicy policy = SCPProtocolPolicy.SCP02;

        assertTrue(policy.isVersionAllowed(2));
        assertFalse(policy.isVersionAllowed(1));
        assertTrue(policy.isProtocolAllowed(2, 0));
        assertEquals("SCP02", policy.toString());
    }

    @Test
    public void fixedParametersPolicyAllowsOnlyItsParameters() {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(0, 0x15);

        assertTrue(policy.isVersionAllowed(3));
        assertTrue(policy.isParametersAllowed(0x15));
        assertFalse(policy.isParametersAllowed(0x14));
        assertEquals("SCP*-15", policy.toString());
    }

    @Test
    public void protocolCheckAllowsMatchingProtocol() throws CardException {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(3, 0x15);

        policy.checkProtocol(3, 0x15);
        assertTrue(policy.isProtocolAllowed(3, 0x15));
    }

    @Test
    public void protocolCheckRejectsMismatchedProtocol() {
        SCPProtocolPolicy policy = SCPProtocolPolicy.SCP03;

        try {
            policy.checkProtocol(2, 0x15);
            fail("Expected CardException");
        } catch (CardException e) {
            assertEquals("Protocol SCP02-15 denied by policy", e.getMessage());
        }
    }
}
