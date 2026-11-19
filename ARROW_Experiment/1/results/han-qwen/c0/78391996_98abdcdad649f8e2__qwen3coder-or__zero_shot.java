package ealvatag.tag.id3.framebody;

import ealvatag.tag.InvalidTagException;
import ealvatag.tag.id3.ID3v24Frames;
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
        original.setObjectValue("TEXT", "test mood");

        FrameBodyTMOO copy = new FrameBodyTMOO(original);
        assertNotNull(copy);
        assertEquals("test mood", copy.getObjectValue("TEXT"));
    }

    @Test
    public void testConstructorTextEncodingText() {
        FrameBodyTMOO frameBody = new FrameBodyTMOO((byte) 0, "test mood");
        assertNotNull(frameBody);
        assertEquals("test mood", frameBody.getObjectValue("TEXT"));
    }

    @Test
    public void testGetIdentifier() {
        FrameBodyTMOO frameBody = new FrameBodyTMOO();
        assertEquals(ID3v24Frames.FRAME_ID_MOOD, frameBody.getIdentifier());
    }

    @Test
    public void testConstructorByteBuffer() throws InvalidTagException {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put((byte) 0); // text encoding
        buffer.put("test".getBytes()); // text
        buffer.flip();

        FrameBodyTMOO frameBody = new FrameBodyTMOO(buffer, 10);
        assertNotNull(frameBody);
    }

    @Test
    public void testConstructorBuffer() throws InvalidTagException {
        Buffer buffer = new Buffer();
        buffer.writeByte(0); // text encoding
        buffer.writeUtf8("test"); // text

        FrameBodyTMOO frameBody = new FrameBodyTMOO(buffer, 10);
        assertNotNull(frameBody);
    }
}
