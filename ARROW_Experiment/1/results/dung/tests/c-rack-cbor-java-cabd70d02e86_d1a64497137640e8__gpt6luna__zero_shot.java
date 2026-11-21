package co.nstant.in.cbor.decoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayInputStream;

import org.junit.Test;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.SimpleValue;
import co.nstant.in.cbor.model.Special;

public class SpecialDecoder_ArrowTest {

    @Test
    public void decodesBreak() throws Exception {
        SpecialDecoder decoder = new SpecialDecoder(new CborDecoder(new ByteArrayInputStream(new byte[0])),
                new ByteArrayInputStream(new byte[0]));

        assertSame(Special.BREAK, decoder.decode(0xFF));
    }

    @Test
    public void decodesFalse() throws Exception {
        SpecialDecoder decoder = new SpecialDecoder(new CborDecoder(new ByteArrayInputStream(new byte[0])),
                new ByteArrayInputStream(new byte[0]));

        assertSame(SimpleValue.FALSE, decoder.decode(0xF4));
    }

    @Test
    public void decodesTrue() throws Exception {
        SpecialDecoder decoder = new SpecialDecoder(new CborDecoder(new ByteArrayInputStream(new byte[0])),
                new ByteArrayInputStream(new byte[0]));

        assertSame(SimpleValue.TRUE, decoder.decode(0xF5));
    }

    @Test
    public void decodesNull() throws Exception {
        SpecialDecoder decoder = new SpecialDecoder(new CborDecoder(new ByteArrayInputStream(new byte[0])),
                new ByteArrayInputStream(new byte[0]));

        assertSame(SimpleValue.NULL, decoder.decode(0xF6));
    }

    @Test
    public void decodesUndefined() throws Exception {
        SpecialDecoder decoder = new SpecialDecoder(new CborDecoder(new ByteArrayInputStream(new byte[0])),
                new ByteArrayInputStream(new byte[0]));

        assertSame(SimpleValue.UNDEFINED, decoder.decode(0xF7));
    }

    @Test
    public void decodesUnallocatedSimpleValue() throws Exception {
        SpecialDecoder decoder = new SpecialDecoder(new CborDecoder(new ByteArrayInputStream(new byte[0])),
                new ByteArrayInputStream(new byte[0]));

        assertEquals(new SimpleValue(0), decoder.decode(0xE0));
    }

    @Test
    public void decodesSimpleValueFromNextByte() throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[] { 42 });
        SpecialDecoder decoder = new SpecialDecoder(new CborDecoder(input), input);

        assertEquals(new SimpleValue(42), decoder.decode(0xF8));
    }

    @Test
    public void rejectsReservedSimpleValue() throws Exception {
        SpecialDecoder decoder = new SpecialDecoder(new CborDecoder(new ByteArrayInputStream(new byte[0])),
                new ByteArrayInputStream(new byte[0]));

        try {
            decoder.decode(0xF0);
        } catch (CborException expected) {
            return;
        }
        throw new AssertionError("Expected CborException");
    }

    @Test
    public void rejectsUnsupportedSpecialType() throws Exception {
        SpecialDecoder decoder = new SpecialDecoder(new CborDecoder(new ByteArrayInputStream(new byte[0])),
                new ByteArrayInputStream(new byte[0]));

        try {
            decoder.decode(0xE1);
        } catch (CborException expected) {
            return;
        }
        throw new AssertionError("Expected CborException");
    }
}
