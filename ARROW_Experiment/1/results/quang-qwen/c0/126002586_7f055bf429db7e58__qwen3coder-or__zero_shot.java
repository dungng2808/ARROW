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
    public void testToStringPermissive() {
        assertEquals("SCP*", SCPProtocolPolicy.PERMISSIVE.toString());
    }

    @Test
    public void testToStringSCP01() {
        assertEquals("SCP01", SCPProtocolPolicy.SCP01.toString());
    }

    @Test
    public void testToStringSCP02() {
        assertEquals("SCP02", SCPProtocolPolicy.SCP02.toString());
    }

    @Test
    public void testToStringSCP03() {
        assertEquals("SCP03", SCPProtocolPolicy.SCP03.toString());
    }

    @Test
    public void testToStringWithParameters() {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(2, 0x1234);
        assertEquals("SCP02-1234", policy.toString());
    }

    @Test
    public void testIsVersionAllowedPermissive() {
        assertTrue(SCPProtocolPolicy.PERMISSIVE.isVersionAllowed(1));
        assertTrue(SCPProtocolPolicy.PERMISSIVE.isVersionAllowed(2));
        assertTrue(SCPProtocolPolicy.PERMISSIVE.isVersionAllowed(3));
    }

    @Test
    public void testIsVersionAllowedSCP01() {
        assertTrue(SCPProtocolPolicy.SCP01.isVersionAllowed(1));
        assertFalse(SCPProtocolPolicy.SCP01.isVersionAllowed(2));
        assertFalse(SCPProtocolPolicy.SCP01.isVersionAllowed(3));
    }

    @Test
    public void testIsVersionAllowedSCP02() {
        assertFalse(SCPProtocolPolicy.SCP02.isVersionAllowed(1));
        assertTrue(SCPProtocolPolicy.SCP02.isVersionAllowed(2));
        assertFalse(SCPProtocolPolicy.SCP02.isVersionAllowed(3));
    }

    @Test
    public void testIsVersionAllowedSCP03() {
        assertFalse(SCPProtocolPolicy.SCP03.isVersionAllowed(1));
        assertFalse(SCPProtocolPolicy.SCP03.isVersionAllowed(2));
        assertTrue(SCPProtocolPolicy.SCP03.isVersionAllowed(3));
    }

    @Test
    public void testIsParametersAllowedPermissive() {
        assertTrue(SCPProtocolPolicy.PERMISSIVE.isParametersAllowed(0x1234));
        assertTrue(SCPProtocolPolicy.PERMISSIVE.isParametersAllowed(0));
    }

    @Test
    public void testIsParametersAllowedSpecific() {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(0, 0x1234);
        assertTrue(SCPProtocolPolicy.SCP01.isParametersAllowed(0x1234));
        assertFalse(SCPProtocolPolicy.SCP01.isParametersAllowed(0x5678));
    }

    @Test
    public void testIsProtocolAllowedPermissive() {
        assertTrue(SCPProtocolPolicy.PERMISSIVE.isProtocolAllowed(1, 0x1234));
        assertTrue(SCPProtocolPolicy.PERMISSIVE.isProtocolAllowed(2, 0));
        assertTrue(SCPProtocolPolicy.PERMISSIVE.isProtocolAllowed(3, 0x5678));
    }

    @Test
    public void testIsProtocolAllowedSCP01() {
        assertTrue(SCPProtocolPolicy.SCP01.isProtocolAllowed(1, 0x1234));
        assertFalse(SCPProtocolPolicy.SCP01.isProtocolAllowed(2, 0x1234));
        assertFalse(SCPProtocolPolicy.SCP01.isProtocolAllowed(1, 0));
    }

    @Test
    public void testIsProtocolAllowedSCP02() {
        assertTrue(SCPProtocolPolicy.SCP02.isProtocolAllowed(2, 0x1234));
        assertFalse(SCPProtocolPolicy.SCP02.isProtocolAllowed(1, 0x1234));
        assertFalse(SCPProtocolPolicy.SCP02.isProtocolAllowed(2, 0));
    }

    @Test
    public void testIsProtocolAllowedSCP03() {
        assertTrue(SCPProtocolPolicy.SCP03.isProtocolAllowed(3, 0x1234));
        assertFalse(SCPProtocolPolicy.SCP03.isProtocolAllowed(1, 0x1234));
        assertFalse(SCPProtocolPolicy.SCP03.isProtocolAllowed(3, 0));
    }

    @Test
    public void testCheckProtocolPermissive() throws CardException {
        SCPProtocolPolicy.PERMISSIVE.checkProtocol(1, 0x1234);
        SCPProtocolPolicy.PERMISSIVE.checkProtocol(2, 0);
        SCPProtocolPolicy.PERMISSIVE.checkProtocol(3, 0x5678);
    }

    @Test(expected = CardException.class)
    public void testCheckProtocolDeniedVersion() throws CardException {
        SCPProtocolPolicy.SCP01.checkProtocol(2, 0x1234);
    }

    @Test(expected = CardException.class)
    public void testCheckProtocolDeniedParameters() throws CardException {
        SCPProtocolPolicy.SCP01.checkProtocol(1, 0x5678);
    }

    @Test
    public void testCheckProtocolWithSCPParameters() throws CardException {
        SCPParameters params = new SCPParameters();
        params.scpVersion = 1;
        params.scpParameters = 0x1234;
        SCPProtocolPolicy.SCP01.checkProtocol(params);
    }

    @Test(expected = CardException.class)
    public void testCheckProtocolWithSCPParametersDenied() throws CardException {
        SCPParameters params = new SCPParameters();
        params.scpVersion = 2;
        params.scpParameters = 0x1234;
        SCPProtocolPolicy.SCP01.checkProtocol(params);
    }

    // Helper class to provide SCPParameters for testing
    private static class SCPParameters {
        public int scpVersion;
        public int scpParameters;
    }
}
