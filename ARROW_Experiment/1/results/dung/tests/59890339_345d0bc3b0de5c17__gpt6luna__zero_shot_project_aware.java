package me.vertretungsplan.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ColorProvider_ArrowTest {

    @Test
    public void returnsNullForNullType() {
        ColorProvider provider = new ColorProvider();

        assertNull(provider.getColor(null));
    }

    @Test
    public void returnsDefaultColorForRecognizedTypeIgnoringCase() {
        ColorProvider provider = new ColorProvider();

        assertEquals("#F44336", provider.getColor("ENTFALL"));
    }

    @Test
    public void returnsPurpleForUnknownType() {
        ColorProvider provider = new ColorProvider();

        assertEquals("#9C27B0", provider.getColor("unrecognized"));
    }
}
