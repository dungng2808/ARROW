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

        assertSame(Special.BREAK, decoder.decode(0xff));
    }

    @Test
    public void decodesFalse() throws Exception {
        SpecialDecoder decoder = new SpecialDecoder(new CborDecoder(new ByteArrayInputStream(new byte[0])),
                new ByteArrayInputStream(new byte[0]));

        assertSame(SimpleValue.FALSE, decoder.decode(0xf4));
    }

    @Test
    public void decodesTrue() throws Exception {
        SpecialDecoder decoder = new SpecialDecoder(new CborDecoder(new ByteArrayInputStream(new byte[0])),
                new ByteArrayInputStream(new byte[0]));

        assertSame(SimpleValue.TRUE, decoder.decode(0xf5));
    }

    @Test
    public void decodesNull() throws Exception {
        SpecialDecoder decoder = new SpecialDecoder(new CborDecoder(new ByteArrayInputStream(new byte[0])),
                new ByteArrayInputStream(new byte[0]));

        assertSame(SimpleValue.NULL, decoder.decode(0xf6));
    }

    @Test
    public void decodesUndefined() throws Exception {
        SpecialDecoder decoder = new SpecialDecoder(new CborDecoder(new ByteArrayInputStream(new byte[0])),
                new ByteArrayInputStream(new byte[0]));

        assertSame(SimpleValue.UNDEFINED, decoder.decode(0xf7));
    }

    @Test
    public void decodesUnallocatedSimpleValue() throws Exception {
        SpecialDecoder decoder = new SpecialDecoder(new CborDecoder(new ByteArrayInputStream(new byte[0])),
                new ByteArrayInputStream(new byte[0]));

        assertEquals(new SimpleValue(0), decoder.decode(0xe0));
    }

    @Test
    public void decodesSimpleValueFromNextByte() throws Exception {
        SpecialDecoder decoder = new SpecialDecoder(new CborDecoder(new ByteArrayInputStream(new byte[0])),
                new ByteArrayInputStream(new byte[] { 42 }));

        assertEquals(new SimpleValue(42), decoder.decode(0xf8));
    }

    @Test(expected = CborException.class)
    public void rejectsReservedSimpleValue() throws Exception {
        SpecialDecoder decoder = new SpecialDecoder(new CborDecoder(new ByteArrayInputStream(new byte[0])),
                new ByteArrayInputStream(new byte[0]));

        decoder.decode(0xf0);
    }

    @Test(expected = CborException.class)
    public void rejectsUnsupportedSpecialType() throws Exception {
        SpecialDecoder decoder = new SpecialDecoder(new CborDecoder(new ByteArrayInputStream(new byte[0])),
                new ByteArrayInputStream(new byte[0]));

        decoder.decode(0xf9);
    }
}
