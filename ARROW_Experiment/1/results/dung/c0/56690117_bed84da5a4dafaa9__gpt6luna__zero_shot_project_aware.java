package de.rub.nds.tlsattacker.core.protocol.message;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import de.rub.nds.tlsattacker.core.config.Config;
import org.junit.Test;

public class UnknownHandshakeMessage_ArrowTest {

    @Test
    public void defaultConstructorCreatesMessageWithNullData() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();

        assertNull(message.getData());
        assertNull(message.getDataConfig());
        assertEquals("UnknownHandshakeMessage:\n  Data: null", message.toString());
    }

    @Test
    public void configConstructorCreatesMessageWithNullData() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage((Config) null);

        assertNull(message.getData());
        assertNull(message.getDataConfig());
        assertEquals("UnknownHandshakeMessage:\n  Data: null", message.toString());
    }

    @Test
    public void setAndGetDataConfig() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        byte[] expected = new byte[] {1, 2, 3};

        message.setDataConfig(expected);

        assertArrayEquals(expected, message.getDataConfig());
    }

    @Test
    public void setDataBytesAndFormatAsHex() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        byte[] expected = new byte[] {0x01, (byte) 0xAB, 0x00};

        message.setData(expected);

        assertNotNull(message.getData());
        assertArrayEquals(expected, message.getData().getValue());
        assertEquals("UnknownHandshakeMessage:\n  Data: 01AB00", message.toString());
    }

    @Test
    public void setNullDataBytesFormatsAsNull() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();

        message.setData((byte[]) null);

        assertEquals("UnknownHandshakeMessage:\n  Data: null", message.toString());
    }
}
