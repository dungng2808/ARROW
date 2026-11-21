package ealvatag.tag.id3.framebody;

import ealvatag.tag.id3.valuepair.TextEncoding;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FrameBodyTMOO_ArrowTest {

    @Test
    public void defaultConstructorHasMoodIdentifier() {
        FrameBodyTMOO body = new FrameBodyTMOO();

        assertEquals("TMOO", body.getIdentifier());
    }

    @Test
    public void textConstructorHasMoodIdentifier() {
        FrameBodyTMOO body = new FrameBodyTMOO(TextEncoding.ISO_8859_1, "mellow");

        assertEquals("TMOO", body.getIdentifier());
    }

    @Test
    public void copyConstructorHasMoodIdentifier() {
        FrameBodyTMOO original = new FrameBodyTMOO(TextEncoding.ISO_8859_1, "mellow");
        FrameBodyTMOO copy = new FrameBodyTMOO(original);

        assertEquals("TMOO", copy.getIdentifier());
    }
}
