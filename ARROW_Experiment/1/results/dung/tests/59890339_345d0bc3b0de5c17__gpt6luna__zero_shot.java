package me.vertretungsplan.parser;

import me.vertretungsplan.objects.SubstitutionScheduleData;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ColorProvider_ArrowTest {

    @Test
    public void returnsDefaultPurpleForUnknownType() {
        ColorProvider provider = new ColorProvider();
        assertEquals("#9C27B0", provider.getColor("unknown"));
    }

    @Test
    public void returnsNullForNullType() {
        ColorProvider provider = new ColorProvider();
        assertNull(provider.getColor(null));
    }

    @Test
    public void recognizesDefaultColorsWithoutCaseSensitivity() {
        ColorProvider provider = new ColorProvider();
        assertEquals("#F44336", provider.getColor("ENTFALL"));
        assertEquals("#2196F3", provider.getColor("vertretung"));
        assertEquals("#FFA000", provider.getColor("Tausch"));
        assertEquals("#4CAF50", provider.getColor("raum"));
        assertEquals("#795548", provider.getColor("Veranstaltung"));
        assertEquals("#FF9800", provider.getColor("Klausur"));
        assertEquals("#9E9E9E", provider.getColor("Pausenaufsicht"));
    }

    @Test
    public void customColorsAndDefaultAreApplied() throws Exception {
        JSONObject dataJson = new JSONObject();
        JSONObject colors = new JSONObject();
        colors.put("red", new org.json.JSONArray().put("Custom"));
        colors.put("#123456", new org.json.JSONArray().put("Special"));
        colors.put("blue", new org.json.JSONArray().put("_default"));
        dataJson.put("colors", colors);

        SubstitutionScheduleData data = new SubstitutionScheduleData();
        data.setData(dataJson);
        ColorProvider provider = new ColorProvider(data);

        assertEquals("#F44336", provider.getColor("custom"));
        assertEquals("#123456", provider.getColor("SPECIAL"));
        assertEquals("#2196F3", provider.getColor("unknown"));
    }
}
