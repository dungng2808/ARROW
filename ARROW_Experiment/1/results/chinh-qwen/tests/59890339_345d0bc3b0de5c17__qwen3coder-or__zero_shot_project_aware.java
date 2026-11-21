package me.vertretungsplan.parser;

import me.vertretungsplan.objects.SubstitutionScheduleData;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class ColorProvider_ArrowTest {

    @Test
    public void testGetColor_NullType() {
        ColorProvider provider = new ColorProvider();
        assertNull(provider.getColor(null));
    }

    @Test
    public void testGetColor_CustomColorMatch() {
        SubstitutionScheduleData data = new SubstitutionScheduleData();
        JSONObject colors = new JSONObject();
        colors.put("red", new String[]{"CustomEntfall"});
        data.getData().put("colors", colors);

        ColorProvider provider = new ColorProvider(data);
        assertEquals("#F44336", provider.getColor("CustomEntfall"));
    }

    @Test
    public void testGetColor_DefaultColorMatch() {
        ColorProvider provider = new ColorProvider();
        assertEquals("#F44336", provider.getColor("Entfall"));
    }

    @Test
    public void testGetColor_NoMatchReturnsDefault() {
        ColorProvider provider = new ColorProvider();
        assertEquals("#9C27B0", provider.getColor("UnknownType"));
    }

    @Test
    public void testGetColor_CustomDefaultColor() {
        SubstitutionScheduleData data = new SubstitutionScheduleData();
        JSONObject colors = new JSONObject();
        colors.put("blue", new String[]{"_default"});
        data.getData().put("colors", colors);

        ColorProvider provider = new ColorProvider(data);
        assertEquals("#2196F3", provider.getColor("SomeType"));
    }

    @Test
    public void testGetColor_CaseInsensitive() {
        ColorProvider provider = new ColorProvider();
        assertEquals("#F44336", provider.getColor("ENTFALL"));
    }

    @Test
    public void testGetColor_CustomColorOverridesDefault() {
        SubstitutionScheduleData data = new SubstitutionScheduleData();
        JSONObject colors = new JSONObject();
        colors.put("green", new String[]{"Entfall"});
        data.getData().put("colors", colors);

        ColorProvider provider = new ColorProvider(data);
        assertEquals("#4CAF50", provider.getColor("Entfall"));
    }

    @Test
    public void testConstructor_InvalidJson() {
        SubstitutionScheduleData data = new SubstitutionScheduleData();
        data.getData().put("colors", "invalid json");

        try {
            ColorProvider provider = new ColorProvider(data);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertNotNull(e.getCause());
        }
    }

    @Test
    public void testConstructor_NoColors() {
        SubstitutionScheduleData data = new SubstitutionScheduleData();

        ColorProvider provider = new ColorProvider(data);
        assertEquals("#9C27B0", provider.getColor("AnyType"));
    }

    @Test
    public void testConstructor_EmptyColors() {
        SubstitutionScheduleData data = new SubstitutionScheduleData();
        JSONObject colors = new JSONObject();
        data.getData().put("colors", colors);

        ColorProvider provider = new ColorProvider(data);
        assertEquals("#9C27B0", provider.getColor("AnyType"));
    }

    @Test
    public void testGetColor_BlueDefault() {
        ColorProvider provider = new ColorProvider();
        assertEquals("#2196F3", provider.getColor("Vertretung"));
    }

    @Test
    public void testGetColor_YellowDefault() {
        ColorProvider provider = new ColorProvider();
        assertEquals("#FFA000", provider.getColor("Tausch"));
    }

    @Test
    public void testGetColor_GreenDefault() {
        ColorProvider provider = new ColorProvider();
        assertEquals("#4CAF50", provider.getColor("Raum"));
    }

    @Test
    public void testGetColor_BrownDefault() {
        ColorProvider provider = new ColorProvider();
        assertEquals("#795548", provider.getColor("Veranst."));
    }

    @Test
    public void testGetColor_OrangeDefault() {
        ColorProvider provider = new ColorProvider();
        assertEquals("#FF9800", provider.getColor("Klausur"));
    }

    @Test
    public void testGetColor_GrayDefault() {
        ColorProvider provider = new ColorProvider();
        assertEquals("#9E9E9E", provider.getColor("Pausenaufsicht"));
    }
}
