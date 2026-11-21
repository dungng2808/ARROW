package io.syndesis.connector.sql.stored;

import org.json.JSONException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

public class JSONBeanUtil_ArrowTest {

    @Test
    public void testParsePropertiesFromJSONBean() throws JSONException {
        String json = "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}";
        Properties properties = JSONBeanUtil.parsePropertiesFromJSONBean(json);
        
        assertNotNull(properties);
        assertEquals("John", properties.getProperty("name"));
        assertEquals("30", properties.getProperty("age"));
        assertEquals("New York", properties.getProperty("city"));
    }

    @Test(expected = JSONException.class)
    public void testParsePropertiesFromJSONBeanInvalidJSON() throws JSONException {
        String json = "{invalid json}";
        JSONBeanUtil.parsePropertiesFromJSONBean(json);
    }

    @Test
    public void testMapToJSONBean() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "John");
        map.put("age", 30);
        map.put("city", "New York");
        map.put("#stats", "ignored");
        
        String json = JSONBeanUtil.mapToJSONBean(map);
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"John\""));
        assertTrue(json.contains("\"age\":30"));
        assertTrue(json.contains("\"city\":\"New York\""));
        assertFalse(json.contains("\"#stats\""));
    }

    @Test
    public void testMapToJSONBeanEmptyMap() {
        Map<String, Object> map = new HashMap<>();
        String json = JSONBeanUtil.mapToJSONBean(map);
        assertEquals("{}", json);
    }

    @Test
    public void testMapToJSONBeanNullValues() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", null);
        map.put("age", 30);
        
        String json = JSONBeanUtil.mapToJSONBean(map);
        assertNotNull(json);
        assertTrue(json.contains("\"name\":null"));
        assertTrue(json.contains("\"age\":30"));
    }
}
