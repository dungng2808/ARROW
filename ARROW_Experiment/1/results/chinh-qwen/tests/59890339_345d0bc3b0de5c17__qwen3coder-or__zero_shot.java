package me.vertretungsplan.parser;

import me.vertretungsplan.objects.SubstitutionScheduleData;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class ColorProvider_ArrowTest {

    @Test
    public void testGetColor_NullType() {
        ColorProvider provider = new ColorProvider();
        String result = provider.getColor(null);
        assertNull(result);
    }

    @Test
    public void testGetColor_WithCustomColorMap() throws Exception {
        JSONObject dataJson = new JSONObject();
        JSONObject colorsJson = new JSONObject();
        colorsJson.put("red", new String[]{"Test"});
        dataJson.put("colors", colorsJson);
        SubstitutionScheduleData data = new SubstitutionScheduleData();
        data.setData(dataJson);

        ColorProvider provider = new ColorProvider(data);
        String result = provider.getColor("Test");
        assertEquals("#F44336", result);
    }

    @Test
    public void testGetColor_WithDefaultColorMap() {
        ColorProvider provider = new ColorProvider();
        String result = provider.getColor("Entfall");
        assertEquals("#F44336", result);
    }

    @Test
    public void testGetColor_UnknownType() {
        ColorProvider provider = new ColorProvider();
        String result = provider.getColor("UnknownType");
        assertEquals("#9C27B0", result);
    }

    @Test
    public void testGetColor_DefaultColorOverride() throws Exception {
        JSONObject dataJson = new JSONObject();
        JSONObject colorsJson = new JSONObject();
        colorsJson.put("red", new String[]{"_default"});
        dataJson.put("colors", colorsJson);
        SubstitutionScheduleData data = new SubstitutionScheduleData();
        data.setData(dataJson);

        ColorProvider provider = new ColorProvider(data);
        String result = provider.getColor("SomeType");
        assertEquals("#F44336", result);
    }

    @Test
    public void testGetColor_CaseInsensitive() {
        ColorProvider provider = new ColorProvider();
        String result = provider.getColor("ENTFALL");
        assertEquals("#F44336", result);
    }

    @Test
    public void testGetColor_CustomColorOverridesDefault() throws Exception {
        JSONObject dataJson = new JSONObject();
        JSONObject colorsJson = new JSONObject();
        colorsJson.put("blue", new String[]{"Entfall"});
        dataJson.put("colors", colorsJson);
        SubstitutionScheduleData data = new SubstitutionScheduleData();
        data.setData(dataJson);

        ColorProvider provider = new ColorProvider(data);
        String result = provider.getColor("Entfall");
        assertEquals("#2196F3", result);
    }
}
