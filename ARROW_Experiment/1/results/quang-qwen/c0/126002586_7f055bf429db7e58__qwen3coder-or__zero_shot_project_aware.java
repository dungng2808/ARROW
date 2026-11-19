package org.openjavacard.gp.scp;

import static org.junit.Assert.*;
import org.junit.Test;
import javax.smartcardio.CardException;

public class SCPProtocolPolicy_ArrowTest {

    @Test
    public void testPermissivePolicy() {
        SCPProtocolPolicy policy = SCPProtocolPolicy.PERMISSIVE;
        assertEquals(0, policy.mScpVersion);
        assertEquals(0, policy.mScpParameters);
        assertTrue(policy.isVersionAllowed(1));
        assertTrue(policy.isVersionAllowed(2));
        assertTrue(policy.isVersionAllowed(3));
        assertTrue(policy.isParametersAllowed(0));
        assertTrue(policy.isParametersAllowed(1));
        assertTrue(policy.isProtocolAllowed(1, 0));
        assertTrue(policy.isProtocolAllowed(2, 1));
        assertTrue(policy.isProtocolAllowed(3, 2));
    }

    @Test
    public void testSCP01Policy() {
        SCPProtocolPolicy policy = SCPProtocolPolicy.SCP01;
        assertEquals(1, policy.mScpVersion);
        assertEquals(0, policy.mScpParameters);
        assertTrue(policy.isVersionAllowed(1));
        assertFalse(policy.isVersionAllowed(2));
        assertFalse(policy.isVersionAllowed(3));
        assertTrue(policy.isParametersAllowed(0));
        assertTrue(policy.isParametersAllowed(1));
        assertTrue(policy.isProtocolAllowed(1, 0));
        assertTrue(policy.isProtocolAllowed(1, 1));
        assertFalse(policy.isProtocolAllowed(2, 0));
        assertFalse(policy.isProtocolAllowed(3, 1));
    }

    @Test
    public void testSCP02Policy() {
        SCPProtocolPolicy policy = SCPProtocolPolicy.SCP02;
        assertEquals(2, policy.mScpVersion);
        assertEquals(0, policy.mScpParameters);
        assertFalse(policy.isVersionAllowed(1));
        assertTrue(policy.isVersionAllowed(2));
        assertFalse(policy.isVersionAllowed(3));
        assertTrue(policy.isParametersAllowed(0));
        assertTrue(policy.isParametersAllowed(1));
        assertTrue(policy.isProtocolAllowed(2, 0));
        assertTrue(policy.isProtocolAllowed(2, 1));
        assertFalse(policy.isProtocolAllowed(1, 0));
        assertFalse(policy.isProtocolAllowed(3, 1));
    }

    @Test
    public void testSCP03Policy() {
        SCPProtocolPolicy policy = SCPProtocolPolicy.SCP03;
        assertEquals(3, policy.mScpVersion);
        assertEquals(0, policy.mScpParameters);
        assertFalse(policy.isVersionAllowed(1));
        assertFalse(policy.isVersionAllowed(2));
        assertTrue(policy.isVersionAllowed(3));
        assertTrue(policy.isParametersAllowed(0));
        assertTrue(policy.isParametersAllowed(1));
        assertTrue(policy.isProtocolAllowed(3, 0));
        assertTrue(policy.isProtocolAllowed(3, 1));
        assertFalse(policy.isProtocolAllowed(1, 0));
        assertFalse(policy.isProtocolAllowed(2, 1));
    }

    @Test
    public void testCustomPolicy() {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(2, 0x1234);
        assertEquals(2, policy.mScpVersion);
        assertEquals(0x1234, policy.mScpParameters);
        assertTrue(policy.isVersionAllowed(2));
        assertFalse(policy.isVersionAllowed(1));
        assertFalse(policy.isVersionAllowed(3));
        assertTrue(policy.isParametersAllowed(0x1234));
        assertFalse(policy.isParametersAllowed(0x5678));
        assertTrue(policy.isProtocolAllowed(2, 0x1234));
        assertFalse(policy.isProtocolAllowed(2, 0x5678));
        assertFalse(policy.isProtocolAllowed(1, 0x1234));
    }

    @Test
    public void testToString() {
        assertEquals("SCP*", SCPProtocolPolicy.PERMISSIVE.toString());
        assertEquals("SCP01", SCPProtocolPolicy.SCP01.toString());
        assertEquals("SCP02", SCPProtocolPolicy.SCP02.toString());
        assertEquals("SCP03", SCPProtocolPolicy.SCP03.toString());
        assertEquals("SCP02-1234", new SCPProtocolPolicy(2, 0x1234).toString());
    }

    @Test
    public void testCheckProtocolWithValidProtocol() throws CardException {
        SCPProtocolPolicy policy = SCPProtocolPolicy.PERMISSIVE;
        policy.checkProtocol(1, 0);
        policy.checkProtocol(2, 1);
        policy.checkProtocol(3, 2);
        
        policy = SCPProtocolPolicy.SCP01;
        policy.checkProtocol(1, 0);
        policy.checkProtocol(1, 1);
        
        policy = SCPProtocolPolicy.SCP02;
        policy.checkProtocol(2, 0);
        policy.checkProtocol(2, 1);
        
        policy = SCPProtocolPolicy.SCP03;
        policy.checkProtocol(3, 0);
        policy.checkProtocol(3, 1);
        
        policy = new SCPProtocolPolicy(2, 0x1234);
        policy.checkProtocol(2, 0x1234);
    }

    @Test(expected = CardException.class)
    public void testCheckProtocolWithInvalidVersion() throws CardException {
        SCPProtocolPolicy policy = SCPProtocolPolicy.SCP01;
        policy.checkProtocol(2, 0);
    }

    @Test(expected = CardException.class)
    public void testCheckProtocolWithInvalidParameters() throws CardException {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(2, 0x1234);
        policy.checkProtocol(2, 0x5678);
    }

    @Test(expected = CardException.class)
    public void testCheckProtocolWithInvalidVersionAndParameters() throws CardException {
        SCPProtocolPolicy policy = SCPProtocolPolicy.SCP02;
        policy.checkProtocol(1, 0);
    }

    @Test
    public void testCheckProtocolWithSCPParametersObject() throws CardException {
        SCPProtocolPolicy policy = SCPProtocolPolicy.PERMISSIVE;
        SCPParameters params = new SCPParameters(1, 0);
        policy.checkProtocol(params);
        
        params = new SCPParameters(2, 1);
        policy.checkProtocol(params);
        
        policy = SCPProtocolPolicy.SCP01;
        params = new SCPParameters(1, 0);
        policy.checkProtocol(params);
        
        params = new SCPParameters(1, 1);
        policy.checkProtocol(params);
    }

    @Test(expected = CardException.class)
    public void testCheckProtocolWithSCPParametersObjectInvalid() throws CardException {
        SCPProtocolPolicy policy = SCPProtocolPolicy.SCP01;
        SCPParameters params = new SCPParameters(2, 0);
        policy.checkProtocol(params);
    }

    public static class SCPParameters {
        public final int scpVersion;
        public final int scpParameters;
        
        public SCPParameters(int version, int parameters) {
            this.scpVersion = version;
            this.scpParameters = parameters;
        }
    }
}
