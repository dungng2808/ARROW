package de.rub.nds.tlsattacker.core.protocol.message;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import de.rub.nds.modifiablevariable.bytearray.ModifiableByteArray;
import org.junit.Test;

public class UnknownHandshakeMessage_ArrowTest {

    @Test
    public void defaultConstructorHasNoData() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();

        assertNull(message.getDataConfig());
        assertNull(message.getData());
        assertEquals("UnknownHandshakeMessage:\n  Data: null", message.toString());
    }

    @Test
    public void dataConfigCanBeSetAndRetrieved() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        byte[] expected = new byte[] {1, 2, 3};

        message.setDataConfig(expected);

        assertArrayEquals(expected, message.getDataConfig());
    }

    @Test
    public void byteArrayDataCanBeSetAndFormatted() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        byte[] expected = new byte[] {0x01, 0x0A, (byte) 0xFF};

        message.setData(expected);

        assertNotNull(message.getData());
        assertArrayEquals(expected, message.getData().getValue());
        assertEquals("UnknownHandshakeMessage:\n  Data: 010AFF", message.toString());
    }

    @Test
    public void nullByteArrayDataIsFormattedAsNull() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();

        message.setData(new byte[] {1});
        message.setData((byte[]) null);

        assertEquals("UnknownHandshakeMessage:\n  Data: null", message.toString());
    }

    @Test
    public void modifiableDataCanBeSetAndRetrieved() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        ModifiableByteArray data = new ModifiableByteArray();
        data.setValue(new byte[] {0x12, 0x34});

        message.setData(data);

        assertEquals(data, message.getData());
        assertEquals("UnknownHandshakeMessage:\n  Data: 1234", message.toString());
    }
}
