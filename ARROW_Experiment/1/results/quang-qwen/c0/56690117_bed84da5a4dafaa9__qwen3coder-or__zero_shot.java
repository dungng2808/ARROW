package de.rub.nds.tlsattacker.core.protocol.message;

import de.rub.nds.modifiablevariable.ModifiableVariableFactory;
import de.rub.nds.modifiablevariable.bytearray.ModifiableByteArray;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.HandshakeMessageType;
import org.junit.Test;
import static org.junit.Assert.*;

public class UnknownHandshakeMessage_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        assertEquals(HandshakeMessageType.UNKNOWN, message.getMessageType());
    }

    @Test
    public void testConfigConstructor() {
        Config config = new Config();
        UnknownHandshakeMessage message = new UnknownHandshakeMessage(config);
        assertEquals(HandshakeMessageType.UNKNOWN, message.getMessageType());
    }

    @Test
    public void testGetDataConfig() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        assertNull(message.getDataConfig());
    }

    @Test
    public void testSetDataConfig() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        byte[] testData = {1, 2, 3, 4};
        message.setDataConfig(testData);
        assertArrayEquals(testData, message.getDataConfig());
    }

    @Test
    public void testGetData() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        assertNull(message.getData());
    }

    @Test
    public void testSetDataByteArray() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        byte[] testData = {1, 2, 3, 4};
        ModifiableByteArray modifiableData = ModifiableVariableFactory.safelySetValue(null, testData);
        message.setData(modifiableData);
        assertEquals(modifiableData, message.getData());
    }

    @Test
    public void testSetDataBytes() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        byte[] testData = {1, 2, 3, 4};
        message.setData(testData);
        assertNotNull(message.getData());
        assertArrayEquals(testData, message.getData().getValue());
    }

    @Test
    public void testGetHandler() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        TlsContext context = new TlsContext();
        assertNotNull(message.getHandler(context));
        assertTrue(message.getHandler(context) instanceof UnknownHandshakeHandler);
    }

    @Test
    public void testToString() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        String result = message.toString();
        assertNotNull(result);
        assertTrue(result.contains("UnknownHandshakeMessage"));
        assertTrue(result.contains("Data: null"));
    }

    @Test
    public void testToStringWithData() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        byte[] testData = {(byte) 0xFF, (byte) 0xFE, (byte) 0xFD};
        message.setData(testData);
        String result = message.toString();
        assertNotNull(result);
        assertTrue(result.contains("UnknownHandshakeMessage"));
        assertTrue(result.contains("Data: "));
    }
}
