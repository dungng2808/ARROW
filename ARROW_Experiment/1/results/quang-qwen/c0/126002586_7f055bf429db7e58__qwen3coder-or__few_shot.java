package org.openjavacard.gp.scp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.smartcardio.CardException;

import org.junit.Test;

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
    public void testToString() {
        assertEquals("SCP*", SCPProtocolPolicy.PERMISSIVE.toString());
        assertEquals("SCP01", SCPProtocolPolicy.SCP01.toString());
        assertEquals("SCP02", SCPProtocolPolicy.SCP02.toString());
        assertEquals("SCP03", SCPProtocolPolicy.SCP03.toString());
        
        SCPProtocolPolicy policy = new SCPProtocolPolicy(1, 0x12345678);
        assertEquals("SCP01-12345678", policy.toString());
    }

    @Test
    public void testCheckProtocolWithValidProtocol() throws CardException {
        SCPProtocolPolicy policy = SCPProtocolPolicy.SCP02;
        SCPParameters params = new SCPParameters(2, 0);
        policy.checkProtocol(params);
        
        // Should not throw exception
        policy.checkProtocol(2, 0);
    }

    @Test(expected = CardException.class)
    public void testCheckProtocolWithInvalidProtocol() throws CardException {
        SCPProtocolPolicy policy = SCPProtocolPolicy.SCP02;
        SCPParameters params = new SCPParameters(1, 0);
        policy.checkProtocol(params);
    }

    @Test(expected = CardException.class)
    public void testCheckProtocolWithInvalidProtocolByVersion() throws CardException {
        SCPProtocolPolicy policy = SCPProtocolPolicy.SCP01;
        policy.checkProtocol(2, 0);
    }

    @Test(expected = CardException.class)
    public void testCheckProtocolWithInvalidProtocolByParameters() throws CardException {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(1, 0x12345678);
        policy.checkProtocol(1, 0);
    }

    @Test
    public void testCustomPolicy() {
        SCPProtocolPolicy policy = new SCPProtocolPolicy(2, 0x12345678);
        assertEquals(2, policy.mScpVersion);
        assertEquals(0x12345678, policy.mScpParameters);
        assertTrue(policy.isVersionAllowed(2));
        assertFalse(policy.isVersionAllowed(1));
        assertFalse(policy.isVersionAllowed(3));
        assertTrue(policy.isParametersAllowed(0x12345678));
        assertFalse(policy.isParametersAllowed(0));
        assertTrue(policy.isProtocolAllowed(2, 0x12345678));
        assertFalse(policy.isProtocolAllowed(1, 0x12345678));
        assertFalse(policy.isProtocolAllowed(2, 0));
    }
}
