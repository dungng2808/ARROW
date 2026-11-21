package de.rub.nds.tlsattacker.core.protocol.message;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import de.rub.nds.modifiablevariable.bytearray.ModifiableByteArray;
import org.junit.Test;

public class UnknownHandshakeMessage_ArrowTest {

    @Test
    public void defaultConstructorInitializesDataAsNull() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();

        assertNull(message.getData());
        assertNull(message.getDataConfig());
        assertEquals("UnknownHandshakeMessage:\n  Data: null", message.toString());
    }

    @Test
    public void setDataConfigStoresArray() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        byte[] data = new byte[] {1, 2, 3};

        message.setDataConfig(data);

        assertArrayEquals(data, message.getDataConfig());
    }

    @Test
    public void setDataBytesStoresValueAndFormatsHex() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        byte[] data = new byte[] {0x01, (byte) 0xAB, 0x00};

        message.setData(data);

        assertNotNull(message.getData());
        assertArrayEquals(data, message.getData().getValue());
        assertEquals("UnknownHandshakeMessage:\n  Data: 01AB00", message.toString());
    }

    @Test
    public void setDataModifiableByteArrayStoresObject() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();
        ModifiableByteArray data = new ModifiableByteArray();

        message.setData(data);

        assertEquals(data, message.getData());
        assertEquals("UnknownHandshakeMessage:\n  Data: null", message.toString());
    }

    @Test
    public void setDataBytesNullFormatsAsNull() {
        UnknownHandshakeMessage message = new UnknownHandshakeMessage();

        message.setData((byte[]) null);

        assertEquals("UnknownHandshakeMessage:\n  Data: null", message.toString());
    }
}
