package io.syndesis.connector.sql.stored;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.json.JSONException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JSONBeanUtil_ArrowTest {

    @Test
    public void parsesStringAndNumericProperties() throws JSONException {
        Properties properties = JSONBeanUtil.parsePropertiesFromJSONBean("{\"name\":\"Ada\",\"count\":3}");

        assertEquals("Ada", properties.getProperty("name"));
        assertEquals("3", properties.getProperty("count"));
    }

    @Test
    public void parsesNullPropertyAsStringNull() throws JSONException {
        Properties properties = JSONBeanUtil.parsePropertiesFromJSONBean("{\"value\":null}");

        assertEquals("null", properties.getProperty("value"));
    }

    @Test(expected = JSONException.class)
    public void rejectsInvalidJson() throws JSONException {
        JSONBeanUtil.parsePropertiesFromJSONBean("{");
    }

    @Test
    public void convertsMapToJsonAndOmitsCamelStatistics() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", "Ada");
        map.put("count", 3);
        map.put("#exchangeId", "ignored");

        String json = JSONBeanUtil.mapToJSONBean(map);

        assertEquals("{\"name\":\"Ada\",\"count\":3}", json);
    }

    @Test
    public void convertsEmptyMapToEmptyJsonObject() {
        String json = JSONBeanUtil.mapToJSONBean(new HashMap<String, Object>());

        assertEquals("{}", json);
    }
}
