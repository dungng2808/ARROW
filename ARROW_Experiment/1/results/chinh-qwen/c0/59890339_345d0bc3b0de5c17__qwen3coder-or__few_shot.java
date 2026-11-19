package me.vertretungsplan.parser;

import me.vertretungsplan.objects.SubstitutionScheduleData;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class ColorProvider_ArrowTest {

    @Test
    public void testGetColorWithNullType() {
        ColorProvider colorProvider = new ColorProvider();
        String result = colorProvider.getColor(null);
        assertNull(result);
    }

    @Test
    public void testGetColorWithDefaultColorMapMatch() {
        ColorProvider colorProvider = new ColorProvider();
        String result = colorProvider.getColor("Entfall");
        assertEquals("#F44336", result);
    }

    @Test
    public void testGetColorWithCustomColorMapMatch() {
        JSONObject data = new JSONObject();
        JSONObject colors = new JSONObject();
        colors.put("red", new String[]{"CustomEntfall"});
        data.put("colors", colors);
        
        SubstitutionScheduleData scheduleData = new SubstitutionScheduleData();
        scheduleData.setData(data);
        
        ColorProvider colorProvider = new ColorProvider(scheduleData);
        String result = colorProvider.getColor("CustomEntfall");
        assertEquals("#F44336", result);
    }

    @Test
    public void testGetColorWithDefaultColor() {
        ColorProvider colorProvider = new ColorProvider();
        String result = colorProvider.getColor("UnknownType");
        assertEquals("#9C27B0", result);
    }

    @Test
    public void testGetColorWithCustomDefaultColor() {
        JSONObject data = new JSONObject();
        JSONObject colors = new JSONObject();
        colors.put("red", new String[]{"_default"});
        data.put("colors", colors);
        
        SubstitutionScheduleData scheduleData = new SubstitutionScheduleData();
        scheduleData.setData(data);
        
        ColorProvider colorProvider = new ColorProvider(scheduleData);
        String result = colorProvider.getColor("SomeType");
        assertEquals("#F44336", result);
    }

    @Test
    public void testGetColorWithCustomColorAndDefaultColor() {
        JSONObject data = new JSONObject();
        JSONObject colors = new JSONObject();
        colors.put("red", new String[]{"CustomEntfall", "_default"});
        data.put("colors", colors);
        
        SubstitutionScheduleData scheduleData = new SubstitutionScheduleData();
        scheduleData.setData(data);
        
        ColorProvider colorProvider = new ColorProvider(scheduleData);
        String result = colorProvider.getColor("SomeType");
        assertEquals("#F44336", result);
    }

    @Test
    public void testGetColorWithCustomColorMapNoMatch() {
        JSONObject data = new JSONObject();
        JSONObject colors = new JSONObject();
        colors.put("blue", new String[]{"CustomVertretung"});
        data.put("colors", colors);
        
        SubstitutionScheduleData scheduleData = new SubstitutionScheduleData();
        scheduleData.setData(data);
        
        ColorProvider colorProvider = new ColorProvider(scheduleData);
        String result = colorProvider.getColor("Entfall");
        assertEquals("#2196F3", result);
    }

    @Test
    public void testGetColorWithInvalidJson() {
        JSONObject data = new JSONObject();
        data.put("colors", "invalid");
        
        SubstitutionScheduleData scheduleData = new SubstitutionScheduleData();
        scheduleData.setData(data);
        
        try {
            ColorProvider colorProvider = new ColorProvider(scheduleData);
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertNotNull(e.getCause());
        }
    }
}
