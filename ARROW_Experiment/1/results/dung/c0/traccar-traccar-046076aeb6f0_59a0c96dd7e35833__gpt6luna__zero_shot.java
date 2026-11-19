package org.traccar.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import org.traccar.Protocol;
import org.traccar.model.Position;

import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Gt06ProtocolDecoder_ArrowTest {

    @Test
    public void decodeGpsDecodesCoordinatesAndFlags() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(24);
        buf.writeByte(1);
        buf.writeByte(2);
        buf.writeByte(3);
        buf.writeByte(4);
        buf.writeByte(5);
        buf.writeByte(0xA3);
        buf.writeInt(1800000);
        buf.writeInt(3600000);
        buf.writeByte(10);
        buf.writeShort(0xD405);

        Position position = new Position("gt06");
        boolean decoded = Gt06ProtocolDecoder.decodeGps(position, buf, true, TimeZone.getTimeZone("UTC"));

        assertTrue(decoded);
        assertEquals(1.0, position.getLatitude(), 0.000001);
        assertEquals(2.0, position.getLongitude(), 0.000001);
        assertEquals(5.0, position.getCourse(), 0.000001);
        assertTrue(position.getValid());
        assertEquals(10, position.get(Position.KEY_SATELLITES));
        assertEquals(10.0 * 0.539957, position.getSpeed(), 0.0001);
        buf.release();
    }

    @Test
    public void decodeGpsReturnsFalseWhenLengthIsZero() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(24);
        buf.writeByte(1);
        buf.writeByte(2);
        buf.writeByte(3);
        buf.writeByte(4);
        buf.writeByte(5);
        buf.writeByte(0);

        Position position = new Position("gt06");
        boolean decoded = Gt06ProtocolDecoder.decodeGps(position, buf, true, TimeZone.getTimeZone("UTC"));

        assertFalse(decoded);
        assertEquals(0, buf.readableBytes());
        buf.release();
    }

    @Test
    public void decodeGpsSupportsDisabledOptionalFields() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(24);
        buf.writeByte(1);
        buf.writeByte(2);
        buf.writeByte(3);
        buf.writeByte(4);
        buf.writeByte(5);
        buf.writeInt(1800000);
        buf.writeInt(3600000);
        buf.writeShort(0x0C00);

        Position position = new Position("gt06");
        boolean decoded = Gt06ProtocolDecoder.decodeGps(
                position, buf, false, false, false, TimeZone.getTimeZone("UTC"));

        assertTrue(decoded);
        assertEquals(1.0, position.getLatitude(), 0.000001);
        assertEquals(2.0, position.getLongitude(), 0.000001);
        assertTrue(position.getValid());
        buf.release();
    }
}
