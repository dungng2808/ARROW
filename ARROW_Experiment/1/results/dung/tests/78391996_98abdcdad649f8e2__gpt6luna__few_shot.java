package ealvatag.tag.id3.framebody;

import ealvatag.tag.id3.ID3v24Frames;
import ealvatag.tag.id3.valuepair.TextEncoding;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FrameBodyTMOO_ArrowTest {

    @Test
    public void defaultConstructorHasMoodIdentifier() {
        FrameBodyTMOO body = new FrameBodyTMOO();

        assertEquals(ID3v24Frames.FRAME_ID_MOOD, body.getIdentifier());
    }

    @Test
    public void textConstructorHasMoodIdentifier() {
        FrameBodyTMOO body = new FrameBodyTMOO(TextEncoding.ISO_8859_1, "mood");

        assertEquals(ID3v24Frames.FRAME_ID_MOOD, body.getIdentifier());
    }

    @Test
    public void copyConstructorHasMoodIdentifier() {
        FrameBodyTMOO original = new FrameBodyTMOO(TextEncoding.ISO_8859_1, "mood");
        FrameBodyTMOO copy = new FrameBodyTMOO(original);

        assertEquals(ID3v24Frames.FRAME_ID_MOOD, copy.getIdentifier());
    }
}
