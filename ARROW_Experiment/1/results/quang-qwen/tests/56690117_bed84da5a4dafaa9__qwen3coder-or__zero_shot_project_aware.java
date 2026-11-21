package de.rub.nds.tlsattacker.core.protocol.message;

import de.rub.nds.modifiablevariable.ModifiableVariableFactory;
import de.rub.nds.modifiablevariable.bytearray.ModifiableByteArray;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.HandshakeMessageType;
import de.rub.nds.tlsattacker.core.protocol.handler.UnknownHandshakeHandler;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class UnknownHandshakeMessage_ArrowTest {

    private UnknownHandshakeMessage unknownHandshakeMessage;

    @Before
    public void setUp() {
        unknownHandshakeMessage = new UnknownHandshakeMessage();
    }

    @Test
    public void testConstructorNoArgs() {
        assertEquals(HandshakeMessageType.UNKNOWN, unknownHandshakeMessage.getMessageType());
    }

    @Test
    public void testConstructorWithConfig() {
        Config config = mock(Config.class);
        UnknownHandshakeMessage message = new UnknownHandshakeMessage(config);
        assertEquals(HandshakeMessageType.UNKNOWN, message.getMessageType());
    }

    @Test
    public void testGetDataConfig() {
        assertNull(unknownHandshakeMessage.getDataConfig());
    }

    @Test
    public void testSetDataConfig() {
        byte[] testData = {1, 2, 3};
        unknownHandshakeMessage.setDataConfig(testData);
        assertArrayEquals(testData, unknownHandshakeMessage.getDataConfig());
    }

    @Test
    public void testGetData() {
        assertNull(unknownHandshakeMessage.getData());
    }

    @Test
    public void testSetDataByteArray() {
        byte[] testData = {4, 5, 6};
        unknownHandshakeMessage.setData(testData);
        assertNotNull(unknownHandshakeMessage.getData());
        assertArrayEquals(testData, unknownHandshakeMessage.getData().getValue());
    }

    @Test
    public void testSetDataModifiableByteArray() {
        ModifiableByteArray testModifiableByteArray = ModifiableVariableFactory.safelySetValue(null, new byte[]{7, 8, 9});
        unknownHandshakeMessage.setData(testModifiableByteArray);
        assertEquals(testModifiableByteArray, unknownHandshakeMessage.getData());
    }

    @Test
    public void testGetHandler() {
        TlsContext context = mock(TlsContext.class);
        assertNotNull(unknownHandshakeMessage.getHandler(context));
        assertTrue(unknownHandshakeMessage.getHandler(context) instanceof UnknownHandshakeHandler);
    }

    @Test
    public void testToString() {
        String result = unknownHandshakeMessage.toString();
        assertNotNull(result);
        assertTrue(result.contains("UnknownHandshakeMessage"));
        assertTrue(result.contains("Data: null"));
    }

    @Test
    public void testToStringWithData() {
        byte[] testData = {10, 11, 12};
        unknownHandshakeMessage.setData(testData);
        String result = unknownHandshakeMessage.toString();
        assertNotNull(result);
        assertTrue(result.contains("UnknownHandshakeMessage"));
        assertTrue(result.contains("Data: "));
    }
}
