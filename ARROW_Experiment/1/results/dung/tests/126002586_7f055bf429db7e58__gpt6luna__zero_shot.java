package org.openjavacard.gp.scp;

import javax.smartcardio.CardException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SCPProtocolPolicy_ArrowTest {

    @Test
    public void permissivePolicyAllowsAnyProtocol() {
        SCPProtocolPolicy policy = SCPProtocolPolicy.PERMISSIVE;

        assertTrue(policy.isVersionAllowed(1));
        assertTrue(policy.isParametersAllowed(255));
        assertTrue(policy.isProtocolAllowed(2, 3));
        assertEquals("SCP*", policy.toString());
    }

    @Test
    public void versionSpecificPolicyAllowsOnlyItsVersion() {
        SCPProtocolPolicy policy = SCPProtocolPolicy.SCP02;

        assertTrue(policy.isVersionAllowed(2));
        assertFalse(policy.isVersionAllowed(1));
        assertTrue(policy.isParametersAllowed(7));
        assertTrue(policy.isProtocolAllowed(2, 7));
        assertFalse(policy.isProtocolAllowed(1, 7));
        assertEquals("SCP02", policy.toString());
    }

    @Test
    public void parameterSpecificPolicyAllowsOnlyItsParameters() {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(0, 0x0A);

        assertTrue(policy.isVersionAllowed(3));
        assertTrue(policy.isParametersAllowed(0x0A));
        assertFalse(policy.isParametersAllowed(0x0B));
        assertTrue(policy.isProtocolAllowed(3, 0x0A));
        assertFalse(policy.isProtocolAllowed(3, 0x0B));
        assertEquals("SCP*-0A", policy.toString());
    }

    @Test
    public void policyCanRestrictBothVersionAndParameters() {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(3, 0x15);

        assertTrue(policy.isProtocolAllowed(3, 0x15));
        assertFalse(policy.isProtocolAllowed(3, 0x14));
        assertFalse(policy.isProtocolAllowed(2, 0x15));
        assertEquals("SCP03-15", policy.toString());
    }

    @Test
    public void protocolObjectIsCheckedAgainstPolicy() {
        SCPProtocolPolicy policy = SCPProtocolPolicy.SCP01;
        SCPParameters protocol = new SCPParameters(1, 0);

        assertTrue(policy.isProtocolAllowed(protocol));
    }

    @Test
    public void checkProtocolAllowsMatchingValues() throws CardException {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(2, 0x0B);

        policy.checkProtocol(2, 0x0B);
    }

    @Test(expected = CardException.class)
    public void checkProtocolRejectsMismatchedValues() throws CardException {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(2, 0x0B);

        policy.checkProtocol(2, 0x0A);
    }

    @Test
    public void checkProtocolAllowsMatchingProtocolObject() throws CardException {
        SCPProtocolPolicy policy = SCPProtocolPolicy.SCP03;
        SCPParameters protocol = new SCPParameters(3, 0);

        policy.checkProtocol(protocol);
    }
}
