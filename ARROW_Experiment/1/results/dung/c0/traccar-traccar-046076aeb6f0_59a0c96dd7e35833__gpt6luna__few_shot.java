package org.traccar.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.TimeZone;
import org.junit.Test;
import org.traccar.Protocol;
import org.traccar.model.Position;

public class Gt06ProtocolDecoder_ArrowTest {

    @Test
    public void decodeGpsReturnsFalseWhenLengthIsZero() {
        Position position = new Position("gt06");
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(24);
        buffer.writeByte(1);
        buffer.writeByte(2);
        buffer.writeByte(3);
        buffer.writeByte(4);
        buffer.writeByte(5);
        buffer.writeByte(0);

        boolean result = Gt06ProtocolDecoder.decodeGps(
                position, buffer, true, TimeZone.getTimeZone("UTC"));

        assertFalse(result);
        assertEquals(6, buffer.readableBytes());
    }

    @Test
    public void decodeGpsDecodesCoordinatesAndCourse() {
        Position position = new Position("gt06");
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(24);
        buffer.writeByte(1);
        buffer.writeByte(2);
        buffer.writeByte(3);
        buffer.writeByte(4);
        buffer.writeByte(5);
        buffer.writeByte(8);
        buffer.writeInt(1800000);
        buffer.writeInt(3600000);
        buffer.writeByte(60);
        buffer.writeShort(0x5400);

        boolean result = Gt06ProtocolDecoder.decodeGps(
                position, buffer, false, TimeZone.getTimeZone("UTC"));

        assertTrue(result);
        assertEquals(1.0, position.getLatitude(), 0.000001);
        assertEquals(2.0, position.getLongitude(), 0.000001);
        assertEquals(0.0, position.getCourse(), 0.000001);
        assertEquals(8, position.get(Position.KEY_SATELLITES));
        assertEquals(60.0 * 0.539957, position.getSpeed(), 0.0001);
    }

    @Test
    public void decodeGpsAppliesCoordinateSignsFromFlags() {
        Position position = new Position("gt06");
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(24);
        buffer.writeByte(1);
        buffer.writeByte(2);
        buffer.writeByte(3);
        buffer.writeByte(4);
        buffer.writeByte(5);
        buffer.writeByte(0);
        buffer.writeInt(1800000);
        buffer.writeInt(3600000);
        buffer.writeByte(0);
        buffer.writeShort(0);

        Gt06ProtocolDecoder.decodeGps(position, buffer, false, false, false, TimeZone.getTimeZone("UTC"));

        assertEquals(-1.0, position.getLatitude(), 0.000001);
        assertEquals(2.0, position.getLongitude(), 0.000001);
        assertFalse(position.getValid());
    }

    @Test
    public void defaultOptionsAreNotApplicable() {
        assertEquals(Gt06ProtocolDecoder.MSG_LOGIN, 1);
    }
}
