package co.nstant.in.cbor.decoder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.SimpleValue;
import co.nstant.in.cbor.model.Special;
import co.nstant.in.cbor.model.SpecialType;

import static org.junit.Assert.*;

public class SpecialDecoder_ArrowTest {

    @Test
    public void testDecodeBreak() throws CborException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{(byte) 0xff});
        CborDecoder decoder = new CborDecoder(inputStream);
        SpecialDecoder specialDecoder = new SpecialDecoder(decoder, inputStream);
        
        Special result = specialDecoder.decode(0xff);
        assertEquals(Special.BREAK, result);
    }

    @Test
    public void testDecodeFalse() throws CborException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{(byte) 0xf4});
        CborDecoder decoder = new CborDecoder(inputStream);
        SpecialDecoder specialDecoder = new SpecialDecoder(decoder, inputStream);
        
        Special result = specialDecoder.decode(0xf4);
        assertEquals(SimpleValue.FALSE, result);
    }

    @Test
    public void testDecodeTrue() throws CborException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{(byte) 0xf5});
        CborDecoder decoder = new CborDecoder(inputStream);
        SpecialDecoder specialDecoder = new SpecialDecoder(decoder, inputStream);
        
        Special result = specialDecoder.decode(0xf5);
        assertEquals(SimpleValue.TRUE, result);
    }

    @Test
    public void testDecodeNull() throws CborException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{(byte) 0xf6});
        CborDecoder decoder = new CborDecoder(inputStream);
        SpecialDecoder specialDecoder = new SpecialDecoder(decoder, inputStream);
        
        Special result = specialDecoder.decode(0xf6);
        assertEquals(SimpleValue.NULL, result);
    }

    @Test
    public void testDecodeUndefined() throws CborException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{(byte) 0xf7});
        CborDecoder decoder = new CborDecoder(inputStream);
        SpecialDecoder specialDecoder = new SpecialDecoder(decoder, inputStream);
        
        Special result = specialDecoder.decode(0xf7);
        assertEquals(SimpleValue.UNDEFINED, result);
    }

    @Test
    public void testDecodeUnallocated() throws CborException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{(byte) 0xf8});
        CborDecoder decoder = new CborDecoder(inputStream);
        SpecialDecoder specialDecoder = new SpecialDecoder(decoder, inputStream);
        
        Special result = specialDecoder.decode(0xf8);
        assertNotNull(result);
        assertTrue(result instanceof SimpleValue);
        assertEquals(0, ((SimpleValue) result).getValue());
    }

    @Test(expected = CborException.class)
    public void testDecodeReserved() throws CborException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{(byte) 0xf9});
        CborDecoder decoder = new CborDecoder(inputStream);
        SpecialDecoder specialDecoder = new SpecialDecoder(decoder, inputStream);
        
        specialDecoder.decode(0xf9);
    }

    @Test(expected = CborException.class)
    public void testDecodeNotImplemented() throws CborException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{(byte) 0xfa});
        CborDecoder decoder = new CborDecoder(inputStream);
        SpecialDecoder specialDecoder = new SpecialDecoder(decoder, inputStream);
        
        specialDecoder.decode(0xfa);
    }

    @Test
    public void testDecodeHalfPrecisionFloat() throws CborException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{(byte) 0xf9, 0x00, 0x00});
        CborDecoder decoder = new CborDecoder(inputStream);
        SpecialDecoder specialDecoder = new SpecialDecoder(decoder, inputStream);
        
        Special result = specialDecoder.decode(0xf9);
        assertNotNull(result);
    }

    @Test
    public void testDecodeSinglePrecisionFloat() throws CborException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{(byte) 0xfa, 0x00, 0x00, 0x00, 0x00});
        CborDecoder decoder = new CborDecoder(inputStream);
        SpecialDecoder specialDecoder = new SpecialDecoder(decoder, inputStream);
        
        Special result = specialDecoder.decode(0xfa);
        assertNotNull(result);
    }

    @Test
    public void testDecodeDoublePrecisionFloat() throws CborException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{(byte) 0xfb, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        CborDecoder decoder = new CborDecoder(inputStream);
        SpecialDecoder specialDecoder = new SpecialDecoder(decoder, inputStream);
        
        Special result = specialDecoder.decode(0xfb);
        assertNotNull(result);
    }

    @Test
    public void testDecodeSimpleValueNextByte() throws CborException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{(byte) 0x18, 0x05});
        CborDecoder decoder = new CborDecoder(inputStream);
        SpecialDecoder specialDecoder = new SpecialDecoder(decoder, inputStream);
        
        Special result = specialDecoder.decode(0x18);
        assertNotNull(result);
        assertTrue(result instanceof SimpleValue);
        assertEquals(5, ((SimpleValue) result).getValue());
    }
}
