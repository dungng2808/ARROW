package co.nstant.in.cbor.decoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.SimpleValue;
import co.nstant.in.cbor.model.Special;
import java.io.ByteArrayInputStream;
import org.junit.Test;

public class SpecialDecoder_ArrowTest {

    private SpecialDecoder decoder(byte... bytes) {
        return new SpecialDecoder(new CborDecoder(new ByteArrayInputStream(bytes)),
                new ByteArrayInputStream(bytes));
    }

    @Test
    public void decodesBreak() throws Exception {
        SpecialDecoder decoder = decoder();
        assertSame(Special.BREAK, decoder.decode(0xff));
    }

    @Test
    public void decodesFalse() throws Exception {
        assertSame(SimpleValue.FALSE, decoder().decode(0xf4));
    }

    @Test
    public void decodesTrue() throws Exception {
        assertSame(SimpleValue.TRUE, decoder().decode(0xf5));
    }

    @Test
    public void decodesNull() throws Exception {
        assertSame(SimpleValue.NULL, decoder().decode(0xf6));
    }

    @Test
    public void decodesUndefined() throws Exception {
        assertSame(SimpleValue.UNDEFINED, decoder().decode(0xf7));
    }

    @Test
    public void decodesUnallocatedSimpleValue() throws Exception {
        assertEquals(new SimpleValue(0), decoder().decode(0xe0));
    }

    @Test
    public void decodesSimpleValueFromNextByte() throws Exception {
        assertEquals(new SimpleValue(42), decoder((byte) 42).decode(0xf8));
    }

    @Test
    public void rejectsReservedSimpleValue() throws Exception {
        try {
            decoder().decode(0xf0);
        } catch (CborException expected) {
            assertEquals("Not implemented", expected.getMessage());
            return;
        }
        assertTrue(false);
    }

    @Test
    public void rejectsUnsupportedSpecialType() throws Exception {
        try {
            decoder().decode(0xf9);
        } catch (CborException expected) {
            assertEquals("Not implemented", expected.getMessage());
            return;
        }
        assertTrue(false);
    }
}
