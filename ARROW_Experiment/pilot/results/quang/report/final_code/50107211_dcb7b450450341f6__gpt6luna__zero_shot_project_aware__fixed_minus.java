package org.configfacade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;

public class ConfigFactory_ArrowTest {

    @Test
    public void validatePathReturnsValidPath() {
        assertEquals("server.port", ConfigFactory.validatePath("server.port"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatePathRejectsLeadingDot() {
        ConfigFactory.validatePath(".server");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatePathRejectsTrailingDot() {
        ConfigFactory.validatePath("server.");
    }

    @Test(expected = NullPointerException.class)
    public void validatePathRejectsNull() {
        ConfigFactory.validatePath(null);
    }

    @Test
    public void isKeyRecognizesLeafPath() {
        assertTrue(ConfigFactory.isKey("port"));
        assertFalse(ConfigFactory.isKey("server.port"));
    }

    @Test
    public void isSubPathRequiresUnequalPrefixMatch() {
        assertTrue(ConfigFactory.isSubPath("server", "server.port"));
        assertFalse(ConfigFactory.isSubPath("server", "server"));
        assertFalse(ConfigFactory.isSubPath("serv", "server"));
    }

    @Test
    public void joinPathJoinsElementsWithDots() {
        assertEquals("server.port", ConfigFactory.joinPath(Arrays.asList("server", "port")));
    }

    @Test
    public void splitPathSplitsOnDots() {
        assertEquals(Arrays.asList("server", "port"), ConfigFactory.splitPath("server.port"));
    }

    @Test
    public void parentPathReturnsSuffixStartingAtLastDot() {
        assertEquals(".port", ConfigFactory.parentPath("server.port"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parentPathRejectsLeafPath() {
        ConfigFactory.parentPath("port");
    }

    @Test
    public void filterParentPathsReturnsDistinctParentsInEncounterOrder() {
        Iterator<String> paths = Arrays.asList("server.port", "server.host", "database.url", "port").iterator();
        Set<String> parents = ConfigFactory.filterParentPaths(paths);
        assertEquals(Arrays.asList(".port", ".host", ".url"),
                Arrays.asList(parents.toArray(new String[parents.size()])));
    }

    @Test
    public void toConfigMapReadsMapValuesAndKeys() {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        values.put("name", "Ada");
        ConfigMap map = ConfigFactory.toConfigMap(values);

        assertEquals("Ada", map.get("name"));
        assertTrue(map.containsKey("name"));
        assertFalse(map.containsKey("missing"));
        assertEquals(Collections.singletonList("name"), toList(map.getRawKeys()));
    }

    @Test
    public void fromMapConvertsValuesToProperties() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("port", "8080");
        Config config = ConfigFactory.fromMap(values);

        assertEquals(Integer.valueOf(8080), config.getInteger("port").get());
    }

    @Test
    public void fromPropertiesConvertsValuesToProperties() {
        Properties values = new Properties();
        values.setProperty("enabled", "true");
        Config config = ConfigFactory.fromProperties(values);

        assertEquals(Boolean.TRUE, config.getBoolean("enabled").get());
    }

    @Test
    public void configPathsAndKeysReflectMapEntries() {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        values.put("server.port", "8080");
        values.put("name", "Ada");
        Config config = ConfigFactory.fromMap(values);

        assertEquals(Arrays.asList("server.port", "name"), toList(config.getPaths()));
        assertEquals(Collections.singletonList("name"), toList(config.getKeys()));
        assertTrue(config.hasPath("name"));
        assertFalse(config.hasPath("missing"));
    }

    @Test
    public void atPathScopesConfigToPrefix() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("server.port", "8080");
        values.put("other", "x");
        Config config = ConfigFactory.fromMap(values).atPath("server");

        assertEquals("server.", config.getCurrentPath());
        assertEquals(Integer.valueOf(8080), config.getInteger("port").get());
        assertEquals(Collections.singletonList("port"), toList(config.getPaths()));
    }

    @Test
    public void chainUsesFirstNonNullValueAndCombinesKeys() {
        Map<String, Object> firstValues = new LinkedHashMap<String, Object>();
        firstValues.put("shared", null);
        firstValues.put("first", "one");
        Map<String, Object> secondValues = new LinkedHashMap<String, Object>();
        secondValues.put("shared", "two");
        secondValues.put("second", "three");

        ConfigMap chained = ConfigFactory.chain(
                ConfigFactory.toConfigMap(firstValues),
                ConfigFactory.toConfigMap(secondValues));

        assertEquals("two", chained.get("shared"));
        assertTrue(chained.containsKey("shared"));
        assertEquals(Arrays.asList("shared", "first", "shared", "second"), toList(chained.getRawKeys()));
    }

    @Test
    public void replaceableWrapsNonReplaceableMap() {
        ConfigMap map = ConfigFactory.toConfigMap(Collections.<String, Object>singletonMap("key", "value"));
        ConfigMap.ReplaceableConfigMap replaceable = ConfigFactory.replaceable(map);

        assertEquals("value", replaceable.get("key"));
        assertTrue(replaceable.containsKey("key"));
    }

    @Test
    public void prettyPrintFormatsPathsAndValues() {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        values.put("name", "Ada");
        values.put("server.port", "8080");

        assertEquals("\nname=Ada\nserver.port=8080\n", ConfigFactory.prettyPrint(ConfigFactory.fromMap(values)));
    }

    private static List<String> toList(Iterable<String> values) {
        java.util.ArrayList<String> result = new java.util.ArrayList<String>();
        for (String value : values) {
            result.add(value);
        }
        return result;
    }
}
