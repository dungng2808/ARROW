package org.openjavacard.gp.scp;

import org.junit.Test;

import javax.smartcardio.CardException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SCPProtocolPolicy_ArrowTest {

    @Test
    public void permissivePolicyAllowsAnyProtocol() {
        SCPProtocolPolicy policy = SCPProtocolPolicy.PERMISSIVE;

        assertTrue(policy.isVersionAllowed(1));
        assertTrue(policy.isParametersAllowed(0xFF));
        assertTrue(policy.isProtocolAllowed(3, 0xFF));
    }

    @Test
    public void fixedVersionAllowsOnlyMatchingVersion() {
        SCPProtocolPolicy policy = SCPProtocolPolicy.SCP02;

        assertTrue(policy.isVersionAllowed(2));
        assertFalse(policy.isVersionAllowed(1));
    }

    @Test
    public void fixedParametersAllowOnlyMatchingParameters() {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(0, 0x15);

        assertTrue(policy.isParametersAllowed(0x15));
        assertFalse(policy.isParametersAllowed(0x14));
    }

    @Test
    public void protocolRequiresBothConfiguredFieldsToMatch() {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(2, 0x15);

        assertTrue(policy.isProtocolAllowed(2, 0x15));
        assertFalse(policy.isProtocolAllowed(2, 0x14));
        assertFalse(policy.isProtocolAllowed(1, 0x15));
    }

    @Test
    public void stringifiesWildcardAndFixedFields() {
        assertEquals("SCP*", SCPProtocolPolicy.PERMISSIVE.toString());
        assertEquals("SCP02", SCPProtocolPolicy.SCP02.toString());
        assertEquals("SCP02-15", new SCPProtocolPolicy(2, 0x15).toString());
    }

    @Test
    public void checkProtocolAllowsMatchingProtocol() throws CardException {
        new SCPProtocolPolicy(2, 0x15).checkProtocol(2, 0x15);
    }

    @Test(expected = CardException.class)
    public void checkProtocolRejectsMismatchingProtocol() throws CardException {
        new SCPProtocolPolicy(2, 0x15).checkProtocol(2, 0x14);
    }
}
