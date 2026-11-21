package org.openjavacard.gp.scp;

import static org.junit.Assert.*;
import org.junit.Test;
import javax.smartcardio.CardException;

public class SCPProtocolPolicy_ArrowTest {

    @Test
    public void testPERMISSIVE() {
        assertEquals(0, SCPProtocolPolicy.PERMISSIVE.mScpVersion);
        assertEquals(0, SCPProtocolPolicy.PERMISSIVE.mScpParameters);
    }

    @Test
    public void testSCP01() {
        assertEquals(1, SCPProtocolPolicy.SCP01.mScpVersion);
        assertEquals(0, SCPProtocolPolicy.SCP01.mScpParameters);
    }

    @Test
    public void testSCP02() {
        assertEquals(2, SCPProtocolPolicy.SCP02.mScpVersion);
        assertEquals(0, SCPProtocolPolicy.SCP02.mScpParameters);
    }

    @Test
    public void testSCP03() {
        assertEquals(3, SCPProtocolPolicy.SCP03.mScpVersion);
        assertEquals(0, SCPProtocolPolicy.SCP03.mScpParameters);
    }

    @Test
    public void testConstructor() {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(2, 0x1234);
        assertEquals(2, policy.mScpVersion);
        assertEquals(0x1234, policy.mScpParameters);
    }

    @Test
    public void testToString_PERMISSIVE() {
        assertEquals("SCP*",
                SCPProtocolPolicy.PERMISSIVE.toString());
    }

    @Test
    public void testToString_SCP01() {
        assertEquals("SCP01",
                SCPProtocolPolicy.SCP01.toString());
    }

    @Test
    public void testToString_SCP02() {
        assertEquals("SCP02",
                SCPProtocolPolicy.SCP02.toString());
    }

    @Test
    public void testToString_SCP03() {
        assertEquals("SCP03",
                SCPProtocolPolicy.SCP03.toString());
    }

    @Test
    public void testToString_withParameters() {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(2, 0x1234);
        assertEquals("SCP02-1234",
                policy.toString());
    }

    @Test
    public void testIsVersionAllowed_PERMISSIVE() {
        assertTrue(SCPProtocolPolicy.PERMISSIVE.isVersionAllowed(1));
        assertTrue(SCPProtocolPolicy.PERMISSIVE.isVersionAllowed(2));
        assertTrue(SCPProtocolPolicy.PERMISSIVE.isVersionAllowed(3));
    }

    @Test
    public void testIsVersionAllowed_SCP01() {
        assertTrue(SCPProtocolPolicy.SCP01.isVersionAllowed(1));
        assertFalse(SCPProtocolPolicy.SCP01.isVersionAllowed(2));
        assertFalse(SCPProtocolPolicy.SCP01.isVersionAllowed(3));
    }

    @Test
    public void testIsVersionAllowed_SCP02() {
        assertFalse(SCPProtocolPolicy.SCP02.isVersionAllowed(1));
        assertTrue(SCPProtocolPolicy.SCP02.isVersionAllowed(2));
        assertFalse(SCPProtocolPolicy.SCP02.isVersionAllowed(3));
    }

    @Test
    public void testIsVersionAllowed_SCP03() {
        assertFalse(SCPProtocolPolicy.SCP03.isVersionAllowed(1));
        assertFalse(SCPProtocolPolicy.SCP03.isVersionAllowed(2));
        assertTrue(SCPProtocolPolicy.SCP03.isVersionAllowed(3));
    }

    @Test
    public void testIsParametersAllowed_PERMISSIVE() {
        assertTrue(SCPProtocolPolicy.PERMISSIVE.isParametersAllowed(0x1234));
        assertTrue(SCPProtocolPolicy.PERMISSIVE.isParametersAllowed(0));
    }

    @Test
    public void testIsParametersAllowed_withParameters() {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(0, 0x1234);
        assertTrue(policy.isParametersAllowed(0x1234));
        assertFalse(policy.isParametersAllowed(0x5678));
    }

    @Test
    public void testIsProtocolAllowed_PERMISSIVE() {
        assertTrue(SCPProtocolPolicy.PERMISSIVE.isProtocolAllowed(1, 0x1234));
        assertTrue(SCPProtocolPolicy.PERMISSIVE.isProtocolAllowed(2, 0));
        assertTrue(SCPProtocolPolicy.PERMISSIVE.isProtocolAllowed(3, 0x5678));
    }

    @Test
    public void testIsProtocolAllowed_SCP01() {
        assertTrue(SCPProtocolPolicy.SCP01.isProtocolAllowed(1, 0x1234));
        assertFalse(SCPProtocolPolicy.SCP01.isProtocolAllowed(2, 0x1234));
        assertFalse(SCPProtocolPolicy.SCP01.isProtocolAllowed(1, 0));
    }

    @Test
    public void testIsProtocolAllowed_SCP02() {
        assertTrue(SCPProtocolPolicy.SCP02.isProtocolAllowed(2, 0x1234));
        assertFalse(SCPProtocolPolicy.SCP02.isProtocolAllowed(1, 0x1234));
        assertFalse(SCPProtocolPolicy.SCP02.isProtocolAllowed(2, 0));
    }

    @Test
    public void testIsProtocolAllowed_SCP03() {
        assertTrue(SCPProtocolPolicy.SCP03.isProtocolAllowed(3, 0x1234));
        assertFalse(SCPProtocolPolicy.SCP03.isProtocolAllowed(1, 0x1234));
        assertFalse(SCPProtocolPolicy.SCP03.isProtocolAllowed(3, 0));
    }

    @Test
    public void testCheckProtocol_PERMISSIVE() throws CardException {
        SCPProtocolPolicy.PERMISSIVE.checkProtocol(1, 0x1234);
        SCPProtocolPolicy.PERMISSIVE.checkProtocol(2, 0);
        SCPProtocolPolicy.PERMISSIVE.checkProtocol(3, 0x5678);
    }

    @Test(expected = CardException.class)
    public void testCheckProtocol_SCP01_denied() throws CardException {
        SCPProtocolPolicy.SCP01.checkProtocol(2, 0x1234);
    }

    @Test(expected = CardException.class)
    public void testCheckProtocol_SCP02_denied() throws CardException {
        SCPProtocolPolicy.SCP02.checkProtocol(1, 0x1234);
    }

    @Test(expected = CardException.class)
    public void testCheckProtocol_SCP03_denied() throws CardException {
        SCPProtocolPolicy.SCP03.checkProtocol(1, 0x1234);
    }

    @Test
    public void testCheckProtocol_withParameters() throws CardException {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(2, 0x1234);
        policy.checkProtocol(2, 0x1234);
    }

    @Test(expected = CardException.class)
    public void testCheckProtocol_withParameters_denied() throws CardException {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(2, 0x1234);
        policy.checkProtocol(2, 0x5678);
    }

    @Test(expected = CardException.class)
    public void testCheckProtocol_withParameters_version_denied() throws CardException {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(2, 0x1234);
        policy.checkProtocol(3, 0x1234);
    }

    @Test
    public void testIsProtocolAllowed_SCPPolicy() {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(2, 0x1234);
        SCPParameters params = new SCPParameters(2, 0x1234);
        assertTrue(policy.isProtocolAllowed(params));
    }

    @Test
    public void testIsProtocolAllowed_SCPPolicy_denied() {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(2, 0x1234);
        SCPParameters params = new SCPParameters(3, 0x1234);
        assertFalse(policy.isProtocolAllowed(params));
    }

    @Test
    public void testCheckProtocol_SCPPolicy() throws CardException {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(2, 0x1234);
        SCPParameters params = new SCPParameters(2, 0x1234);
        policy.checkProtocol(params);
    }

    @Test(expected = CardException.class)
    public void testCheckProtocol_SCPPolicy_denied() throws CardException {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(2, 0x1234);
        SCPParameters params = new SCPParameters(3, 0x1234);
        policy.checkProtocol(params);
    }

    // Helper class to simulate SCPParameters
    private static class SCPParameters {
        public final int scpVersion;
        public final int scpParameters;

        public SCPParameters(int version, int parameters) {
            this.scpVersion = version;
            this.scpParameters = parameters;
        }
    }
}
