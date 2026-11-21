package io.syndesis.connector.sql.stored;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.json.JSONException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class JSONBeanUtil_ArrowTest {

    @Test
    public void parsesStringAndNumericProperties() throws Exception {
        Properties properties = JSONBeanUtil.parsePropertiesFromJSONBean("{\"name\":\"Ada\",\"age\":37}");

        assertEquals(2, properties.size());
        assertEquals("Ada", properties.getProperty("name"));
        assertEquals("37", properties.getProperty("age"));
    }

    @Test
    public void parsesNullPropertyAsStringNull() throws Exception {
        Properties properties = JSONBeanUtil.parsePropertiesFromJSONBean("{\"value\":null}");

        assertEquals("null", properties.getProperty("value"));
    }

    @Test
    public void parsesEmptyObject() throws Exception {
        Properties properties = JSONBeanUtil.parsePropertiesFromJSONBean("{}");

        assertEquals(0, properties.size());
    }

    @Test(expected = JSONException.class)
    public void rejectsInvalidJson() throws Exception {
        JSONBeanUtil.parsePropertiesFromJSONBean("{");
    }

    @Test
    public void convertsMapToJsonAndOmitsCamelStatistics() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", "Ada");
        map.put("count", 3);
        map.put("#camelStat", "ignored");

        String json = JSONBeanUtil.mapToJSONBean(map);

        assertEquals("{\"name\":\"Ada\",\"count\":3}", json);
        assertFalse(json.contains("#camelStat"));
    }

    @Test
    public void convertsEmptyMapToEmptyJsonObject() {
        String json = JSONBeanUtil.mapToJSONBean(new HashMap<String, Object>());

        assertEquals("{}", json);
    }
}
