package org.configfacade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
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

    @Test
    public void validatePathRejectsNull() {
        try {
            ConfigFactory.validatePath(null);
            fail("Expected an exception");
        } catch (Exception expected) {
            assertTrue(expected instanceof NullPointerException);
        }
    }

    @Test
    public void validatePathRejectsLeadingDot() {
        try {
            ConfigFactory.validatePath(".server");
            fail("Expected an exception");
        } catch (Exception expected) {
            assertTrue(expected instanceof IllegalArgumentException);
        }
    }

    @Test
    public void validatePathRejectsTrailingDot() {
        try {
            ConfigFactory.validatePath("server.");
            fail("Expected an exception");
        } catch (Exception expected) {
            assertTrue(expected instanceof IllegalArgumentException);
        }
    }

    @Test
    public void isKeyRecognizesKeyAndPath() {
        assertTrue(ConfigFactory.isKey("port"));
        assertFalse(ConfigFactory.isKey("server.port"));
    }

    @Test
    public void isSubPathChecksPrefixAndInequality() {
        assertTrue(ConfigFactory.isSubPath("server", "server.port"));
        assertFalse(ConfigFactory.isSubPath("server", "server"));
        assertFalse(ConfigFactory.isSubPath("serv", "server"));
    }

    @Test
    public void joinPathJoinsElementsWithDots() {
        assertEquals("server.database.port",
                ConfigFactory.joinPath(Arrays.asList("server", "database", "port")));
    }

    @Test
    public void splitPathSplitsOnDots() {
        assertEquals(Arrays.asList("server", "database", "port"),
                ConfigFactory.splitPath("server.database.port"));
    }

    @Test
    public void parentPathReturnsPathBeforeLastSegment() {
        assertEquals(".port", ConfigFactory.parentPath("server.database.port"));
    }

    @Test
    public void parentPathRejectsSingleKey() {
        try {
            ConfigFactory.parentPath("port");
            fail("Expected an exception");
        } catch (Exception expected) {
            assertTrue(expected instanceof IllegalArgumentException);
        }
    }

    @Test
    public void filterParentPathsReturnsUniqueParentsInEncounterOrder() {
        List<String> paths = Arrays.asList(
                "server.port", "server.host", "database.url", "enabled");
        Set<String> parents = ConfigFactory.filterParentPaths(paths.iterator());
        assertEquals(Arrays.asList("server", "database"),
                Arrays.asList(parents.toArray(new String[0])));
    }

    @Test
    public void fromMapReadsAndConvertsValues() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("server.port", "8080");
        Config config = ConfigFactory.fromMap(values);

        assertEquals(Integer.valueOf(8080), config.getInteger("server.port").get());
        assertEquals("8080", config.getString("server.port").get());
    }

    @Test
    public void fromPropertiesReadsValues() {
        Properties properties = new Properties();
        properties.setProperty("enabled", "true");
        Config config = ConfigFactory.fromProperties(properties);

        assertEquals(Boolean.TRUE, config.getBoolean("enabled").get());
    }

    @Test
    public void chainUsesFirstNonNullValueAndCombinesKeys() {
        Map<String, Object> first = new LinkedHashMap<String, Object>();
        first.put("shared", "first");
        first.put("firstOnly", "one");
        Map<String, Object> second = new LinkedHashMap<String, Object>();
        second.put("shared", "second");
        second.put("secondOnly", "two");

        ConfigMap chained = ConfigFactory.chain(
                ConfigFactory.toConfigMap(first), ConfigFactory.toConfigMap(second));

        assertEquals("first", chained.get("shared"));
        assertEquals("two", chained.get("secondOnly"));
        assertTrue(chained.containsKey("firstOnly"));
        assertEquals(Arrays.asList("shared", "firstOnly", "shared", "secondOnly"),
                Arrays.asList(toArray(chained.getRawKeys())));
    }

    @Test
    public void toConfigMapExposesMapKeysAndValues() {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        values.put("alpha", "a");
        values.put("beta", "b");
        ConfigMap map = ConfigFactory.toConfigMap(values);

        assertEquals("a", map.get("alpha"));
        assertTrue(map.containsKey("beta"));
        assertEquals(Arrays.asList("alpha", "beta"),
                Arrays.asList(toArray(map.getRawKeys())));
    }

    @Test
    public void replaceableReturnsReplaceableMap() {
        Config config = ConfigFactory.fromMap(new HashMap<String, Object>());
        ConfigMap map = ConfigFactory.replaceable(config);
        assertTrue(map instanceof ConfigMap.ReplaceableConfigMap);
    }

    private String[] toArray(Iterable<String> values) {
        java.util.ArrayList<String> result = new java.util.ArrayList<String>();
        for (String value : values) {
            result.add(value);
        }
        return result.toArray(new String[result.size()]);
    }
}
