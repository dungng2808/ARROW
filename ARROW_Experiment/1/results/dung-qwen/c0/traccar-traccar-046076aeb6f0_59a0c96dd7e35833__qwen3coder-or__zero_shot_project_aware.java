package org.traccar.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import org.traccar.BaseProtocolDecoder;
import org.traccar.model.Position;
import java.nio.charset.StandardCharsets;
import java.util.TimeZone;
import static org.junit.Assert.*;

public class Gt06ProtocolDecoder_ArrowTest {

    @Test
    public void testHasGps() {
        assertTrue(Gt06ProtocolDecoder.hasGps(Gt06ProtocolDecoder.MSG_GPS));
        assertTrue(Gt06ProtocolDecoder.hasGps(Gt06ProtocolDecoder.MSG_GPS_LBS_1));
        assertFalse(Gt06ProtocolDecoder.hasGps(Gt06ProtocolDecoder.MSG_STATUS));
    }

    @Test
    public void testHasLbs() {
        assertTrue(Gt06ProtocolDecoder.hasLbs(Gt06ProtocolDecoder.MSG_LBS_STATUS));
        assertTrue(Gt06ProtocolDecoder.hasLbs(Gt06ProtocolDecoder.MSG_GPS_LBS_1));
        assertFalse(Gt06ProtocolDecoder.hasLbs(Gt06ProtocolDecoder.MSG_GPS));
    }

    @Test
    public void testHasStatus() {
        assertTrue(Gt06ProtocolDecoder.hasStatus(Gt06ProtocolDecoder.MSG_STATUS));
        assertTrue(Gt06ProtocolDecoder.hasStatus(Gt06ProtocolDecoder.MSG_GPS_LBS_STATUS_1));
        assertFalse(Gt06ProtocolDecoder.hasStatus(Gt06ProtocolDecoder.MSG_GPS));
    }

    @Test
    public void testIsSupported() {
        assertTrue(Gt06ProtocolDecoder.isSupported(Gt06ProtocolDecoder.MSG_GPS));
        assertTrue(Gt06ProtocolDecoder.isSupported(Gt06ProtocolDecoder.MSG_LBS_STATUS));
        assertFalse(Gt06ProtocolDecoder.isSupported(Gt06ProtocolDecoder.MSG_INFO));
    }

    @Test
    public void testDecodeGps() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(20); // year
        buf.writeByte(12); // month
        buf.writeByte(25); // day
        buf.writeByte(10); // hour
        buf.writeByte(30); // minute
        buf.writeByte(45); // second
        buf.writeByte(0); // length
        buf.writeByte(4); // satellites
        buf.writeInt(0x12345678); // latitude
        buf.writeInt(0x87654321); // longitude
        buf.writeByte(60); // speed
        buf.writeShort(0x1234); // flags

        Position position = new Position();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        boolean result = Gt06ProtocolDecoder.decodeGps(position, buf, true, true, true, tz);

        assertTrue(result);
        assertEquals(2020, position.getTime().getYear() + 1900);
        assertEquals(12, position.getTime().getMonth() + 1);
        assertEquals(25, position.getTime().getDate());
        assertEquals(10, position.getTime().getHours());
        assertEquals(30, position.getTime().getMinutes());
        assertEquals(45, position.getTime().getSeconds());
        assertEquals(4, position.get(Position.KEY_SATELLITES));
        assertEquals(-12.345678, position.getLatitude(), 0.000001);
        assertEquals(-87.654321, position.getLongitude(), 0.000001);
        assertEquals(60.0, position.getSpeed(), 0.000001);
        assertEquals(12.0, position.getCourse(), 0.000001);
        assertTrue(position.getValid());
    }

    @Test
    public void testDecodeBasicLogin() {
        Gt06ProtocolDecoder decoder = new Gt06ProtocolDecoder(null);
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(15); // length
        buf.writeByte(Gt06ProtocolDecoder.MSG_LOGIN);
        buf.writeBytes("12345678".getBytes(StandardCharsets.US_ASCII)); // IMEI
        buf.writeShort(0x1234); // reserved

        EmbeddedChannel channel = new EmbeddedChannel();
        Object result = decoder.decode(channel, null, buf);

        assertNull(result);
        assertTrue(channel.outboundMessages().isEmpty());
    }

    @Test
    public void testDecodeBasicHeartbeat() {
        Gt06ProtocolDecoder decoder = new Gt06ProtocolDecoder(null);
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(10); // length
        buf.writeByte(Gt06ProtocolDecoder.MSG_HEARTBEAT);
        buf.writeByte(0x07); // status
        buf.writeShort(1200); // battery voltage
        buf.writeByte(80); // RSSI

        EmbeddedChannel channel = new EmbeddedChannel();
        Object result = decoder.decode(channel, null, buf);

        assertNotNull(result);
        assertTrue(result instanceof Position);
        Position position = (Position) result;
        assertEquals(12.0, position.get(Position.KEY_BATTERY), 0.000001);
        assertEquals(80, position.get(Position.KEY_RSSI));
    }

    @Test
    public void testDecodeBasicGps() {
        Gt06ProtocolDecoder decoder = new Gt06ProtocolDecoder(null);
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(20); // length
        buf.writeByte(Gt06ProtocolDecoder.MSG_GPS);
        buf.writeByte(20); // year
        buf.writeByte(12); // month
        buf.writeByte(25); // day
        buf.writeByte(10); // hour
        buf.writeByte(30); // minute
        buf.writeByte(45); // second
        buf.writeByte(0); // length
        buf.writeByte(4); // satellites
        buf.writeInt(0x12345678); // latitude
        buf.writeInt(0x87654321); // longitude
        buf.writeByte(60); // speed
        buf.writeShort(0x1234); // flags

        EmbeddedChannel channel = new EmbeddedChannel();
        Object result = decoder.decode(channel, null, buf);

        assertNotNull(result);
        assertTrue(result instanceof Position);
        Position position = (Position) result;
        assertEquals(2020, position.getTime().getYear() + 1900);
        assertEquals(12, position.getTime().getMonth() + 1);
        assertEquals(25, position.getTime().getDate());
        assertEquals(10, position.getTime().getHours());
        assertEquals(30, position.getTime().getMinutes());
        assertEquals(45, position.getTime().getSeconds());
        assertEquals(4, position.get(Position.KEY_SATELLITES));
        assertEquals(-12.345678, position.getLatitude(), 0.000001);
        assertEquals(-87.654321, position.getLongitude(), 0.000001);
        assertEquals(60.0, position.getSpeed(), 0.000001);
        assertEquals(12.0, position.getCourse(), 0.000001);
        assertTrue(position.getValid());
    }

    @Test
    public void testDecodeBasicUnknownType() {
        Gt06ProtocolDecoder decoder = new Gt06ProtocolDecoder(null);
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(10); // length
        buf.writeByte(0xFF); // unknown type
        buf.writeByte(0x00); // some data

        EmbeddedChannel channel = new EmbeddedChannel();
        Object result = decoder.decode(channel, null, buf);

        assertNull(result);
        assertFalse(channel.outboundMessages().isEmpty());
    }

    @Test
    public void testDecodeVariant() {
        Gt06ProtocolDecoder decoder = new Gt06ProtocolDecoder(null);
        ByteBuf buf = Unpooled.buffer();
        buf.writeShort(0x7878); // header
        buf.writeByte(15); // length
        buf.writeByte(Gt06ProtocolDecoder.MSG_GPS_LBS_1); // type
        buf.writeByte(0x24); // length

        decoder.decodeVariant(buf);
        assertEquals(Gt06ProtocolDecoder.Variant.VXT01, decoder.variant);
    }
}
