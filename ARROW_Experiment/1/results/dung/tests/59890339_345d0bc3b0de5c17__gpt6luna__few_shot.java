package me.vertretungsplan.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ColorProvider_ArrowTest {

    @Test
    public void returnsDefaultColorForUnknownType() {
        ColorProvider provider = new ColorProvider();
        assertEquals("#9C27B0", provider.getColor("unknown"));
    }

    @Test
    public void returnsNullForNullType() {
        ColorProvider provider = new ColorProvider();
        assertNull(provider.getColor(null));
    }

    @Test
    public void returnsDefaultColorForRecognizedTypeRegardlessOfCase() {
        ColorProvider provider = new ColorProvider();
        assertEquals("#F44336", provider.getColor("ENTFALL"));
    }

    @Test
    public void returnsBlueForSubstitutionType() {
        ColorProvider provider = new ColorProvider();
        assertEquals("#2196F3", provider.getColor("Vertretung"));
    }

    @Test
    public void returnsYellowForScheduleChange() {
        ColorProvider provider = new ColorProvider();
        assertEquals("#FFA000", provider.getColor("Verlegung"));
    }

    @Test
    public void returnsGreenForRoomChange() {
        ColorProvider provider = new ColorProvider();
        assertEquals("#4CAF50", provider.getColor("Raumwechsel"));
    }

    @Test
    public void returnsBrownForSpecialEvent() {
        ColorProvider provider = new ColorProvider();
        assertEquals("#795548", provider.getColor("Veranstaltung"));
    }

    @Test
    public void returnsOrangeForExam() {
        ColorProvider provider = new ColorProvider();
        assertEquals("#FF9800", provider.getColor("Klausur"));
    }

    @Test
    public void returnsGrayForBreakSupervision() {
        ColorProvider provider = new ColorProvider();
        assertEquals("#9E9E9E", provider.getColor("Pausenaufsicht"));
    }
}
