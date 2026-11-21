package de.rub.nds.tlsattacker.core.protocol.message;

import de.rub.nds.modifiablevariable.ModifiableVariableFactory;
import de.rub.nds.modifiablevariable.bytearray.ModifiableByteArray;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.HandshakeMessageType;
import de.rub.nds.tlsattacker.core.protocol.handler.UnknownHandshakeHandler;
import de.rub.nds.tlsattacker.core.state.TlsContext;
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
        
        byte[] testData = {1, 2, 3, 4};
        message.setDataConfig(testData);
        assertArrayEquals(testData, message.getDataConfig());
    }

    @Test
    public void testSetDataConfig() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        byte[] testData = {5, 6, 7, 8};
        message.setDataConfig(testData);
        assertArrayEquals(testData, message.getDataConfig());
    }

    @Test
    public void testGetData() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        assertNull(message.getData());
        
        ModifiableByteArray testData = ModifiableVariableFactory.safelySetValue(null, new byte[]{1, 2, 3});
        message.setData(testData);
        assertEquals(testData, message.getData());
    }

    @Test
    public void testSetDataByteArray() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        byte[] testData = {9, 10, 11, 12};
        message.setData(testData);
        assertNotNull(message.getData());
        assertArrayEquals(testData, message.getData().getValue());
    }

    @Test
    public void testSetDataModifiableByteArray() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        ModifiableByteArray testData = ModifiableVariableFactory.safelySetValue(null, new byte[]{13, 14, 15});
        message.setData(testData);
        assertEquals(testData, message.getData());
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
        
        byte[] testData = {1, 2, 3, 4};
        message.setData(testData);
        result = message.toString();
        assertTrue(result.contains("Data: "));
    }
}
