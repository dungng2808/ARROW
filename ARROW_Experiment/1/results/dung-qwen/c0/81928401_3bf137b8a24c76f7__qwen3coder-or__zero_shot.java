package io.syndesis.connector.sql.stored;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.json.JSONException;
import org.junit.Test;

public class JSONBeanUtil_ArrowTest {

    @Test
    public void testParsePropertiesFromJSONBean() throws JSONException {
        String json = "{\"name\":\"test\",\"value\":123}";
        Properties result = JSONBeanUtil.parsePropertiesFromJSONBean(json);
        
        assertNotNull(result);
        assertEquals("test", result.getProperty("name"));
        assertEquals("123", result.getProperty("value"));
    }

    @Test
    public void testParsePropertiesFromJSONBeanEmpty() throws JSONException {
        String json = "{}";
        Properties result = JSONBeanUtil.parsePropertiesFromJSONBean(json);
        
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test(expected = JSONException.class)
    public void testParsePropertiesFromJSONBeanInvalidJSON() throws JSONException {
        String json = "{invalid json}";
        JSONBeanUtil.parsePropertiesFromJSONBean(json);
    }

    @Test
    public void testMapToJSONBean() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "test");
        map.put("value", 123);
        map.put("#stats", "ignored");
        
        String result = JSONBeanUtil.mapToJSONBean(map);
        
        assertNotNull(result);
        // Check that the result contains the expected keys and values
        // Note: We're not doing exact string comparison due to JSONObject's ordering
        // but we can verify the content is there
        assertTrueContains(result, "\"name\":\"test\"");
        assertTrueContains(result, "\"value\":123");
        assertFalseContains(result, "\"#stats\"");
    }

    @Test
    public void testMapToJSONBeanEmpty() {
        Map<String, Object> map = new HashMap<>();
        String result = JSONBeanUtil.mapToJSONBean(map);
        
        assertNotNull(result);
        assertEquals("{}", result);
    }

    @Test
    public void testMapToJSONBeanNull() {
        String result = JSONBeanUtil.mapToJSONBean(null);
        
        assertNotNull(result);
        assertEquals("{}", result);
    }

    // Helper method to check if string contains substring
    private void assertTrueContains(String actual, String expectedSubstring) {
        if (actual == null || !actual.contains(expectedSubstring)) {
            throw new AssertionError("Expected string containing: " + expectedSubstring + ", but got: " + actual);
        }
    }

    // Helper method to check if string does NOT contain substring
    private void assertFalseContains(String actual, String unexpectedSubstring) {
        if (actual != null && actual.contains(unexpectedSubstring)) {
            throw new AssertionError("Did not expect string containing: " + unexpectedSubstring + ", but got: " + actual);
        }
    }
}
