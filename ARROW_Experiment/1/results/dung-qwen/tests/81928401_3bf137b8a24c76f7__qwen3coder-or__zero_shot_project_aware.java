package io.syndesis.connector.sql.stored;

import org.json.JSONException;
import org.junit.Test;
import org.junit.Before;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class JSONBeanUtil_ArrowTest {

    private static final String TEST_JSON = "{\"name\":\"testName\",\"value\":123}";
    private static final String EMPTY_JSON = "{}";
    private static final String NULL_JSON = null;

    @Test
    public void testParsePropertiesFromJSONBean() throws JSONException {
        Properties props = JSONBeanUtil.parsePropertiesFromJSONBean(TEST_JSON);
        assertNotNull(props);
        assertEquals("testName", props.getProperty("name"));
        assertEquals("123", props.getProperty("value"));
    }

    @Test
    public void testParsePropertiesFromEmptyJSONBean() throws JSONException {
        Properties props = JSONBeanUtil.parsePropertiesFromJSONBean(EMPTY_JSON);
        assertNotNull(props);
        assertEquals(0, props.size());
    }

    @Test(expected = JSONException.class)
    public void testParsePropertiesFromInvalidJSONBean() throws JSONException {
        JSONBeanUtil.parsePropertiesFromJSONBean("{invalid json}");
    }

    @Test(expected = JSONException.class)
    public void testParsePropertiesFromNullJSONBean() throws JSONException {
        JSONBeanUtil.parsePropertiesFromJSONBean(NULL_JSON);
    }

    @Test
    public void testMapToJSONBean() {
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 42);
        map.put("#camelKey", "shouldNotBeIncluded");
        
        String result = JSONBeanUtil.mapToJSONBean(map);
        assertNotNull(result);
        assertTrue(result.contains("key1"));
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("key2"));
        assertTrue(result.contains("42"));
        assertFalse(result.contains("#camelKey"));
    }

    @Test
    public void testMapToJSONBeanEmptyMap() {
        Map<String, Object> map = new HashMap<>();
        String result = JSONBeanUtil.mapToJSONBean(map);
        assertNotNull(result);
        assertEquals("{}", result);
    }

    @Test
    public void testMapToJSONBeanWithCamelKeys() {
        Map<String, Object> map = new HashMap<>();
        map.put("#camelKey1", "value1");
        map.put("#camelKey2", "value2");
        map.put("normalKey", "normalValue");
        
        String result = JSONBeanUtil.mapToJSONBean(map);
        assertNotNull(result);
        assertTrue(result.contains("normalKey"));
        assertTrue(result.contains("normalValue"));
        assertFalse(result.contains("#camelKey1"));
        assertFalse(result.contains("#camelKey2"));
    }
}
