package ealvatag.tag.id3.framebody;

import ealvatag.tag.InvalidTagException;
import ealvatag.tag.id3.ID3v24Frames;
import ealvatag.tag.id3.framebody.FrameBodyTMOO;
import ealvatag.tag.id3.framebody.FrameBodyTXXX;
import ealvatag.tag.id3.valuepair.TextEncoding;
import okio.Buffer;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FrameBodyTMOO_ArrowTest {

    @Test
    public void testConstructorDefault() {
        FrameBodyTMOO frameBody = new FrameBodyTMOO();
        assertNotNull(frameBody);
    }

    @Test
    public void testConstructorCopy() {
        FrameBodyTMOO original = new FrameBodyTMOO();
        original.setObjectValue("TEXT_ENCODING", (byte) 0);
        original.setObjectValue("TEXT", "test text");

        FrameBodyTMOO copy = new FrameBodyTMOO(original);
        assertNotNull(copy);
        assertEquals(original.getObjectValue("TEXT_ENCODING"), copy.getObjectValue("TEXT_ENCODING"));
        assertEquals(original.getObjectValue("TEXT"), copy.getObjectValue("TEXT"));
    }

    @Test
    public void testConstructorTextEncodingText() {
        byte encoding = TextEncoding.ISO_8859_1;
        String text = "test mood";

        FrameBodyTMOO frameBody = new FrameBodyTMOO(encoding, text);
        assertNotNull(frameBody);
        assertEquals(encoding, frameBody.getObjectValue("TEXT_ENCODING"));
        assertEquals(text, frameBody.getObjectValue("TEXT"));
    }

    @Test
    public void testConstructorFromTXXX() {
        FrameBodyTXXX txxx = new FrameBodyTXXX();
        txxx.setObjectValue("TEXT_ENCODING", (byte) 1);
        txxx.setObjectValue("TEXT", "test text");

        FrameBodyTMOO frameBody = new FrameBodyTMOO(txxx);
        assertNotNull(frameBody);
        assertEquals(TextEncoding.ISO_8859_1, frameBody.getObjectValue("TEXT_ENCODING"));
        assertEquals("test text", frameBody.getObjectValue("TEXT"));
    }

    @Test
    public void testConstructorByteBuffer() throws InvalidTagException {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put((byte) 0);
        buffer.put("test".getBytes());
        buffer.flip();

        FrameBodyTMOO frameBody = new FrameBodyTMOO(buffer, 10);
        assertNotNull(frameBody);
    }

    @Test
    public void testConstructorBuffer() throws InvalidTagException {
        Buffer buffer = new Buffer();
        buffer.writeByte(0);
        buffer.writeUtf8("test");

        FrameBodyTMOO frameBody = new FrameBodyTMOO(buffer, 10);
        assertNotNull(frameBody);
    }

    @Test
    public void testGetIdentifier() {
        FrameBodyTMOO frameBody = new FrameBodyTMOO();
        assertEquals(ID3v24Frames.FRAME_ID_MOOD, frameBody.getIdentifier());
    }
}
