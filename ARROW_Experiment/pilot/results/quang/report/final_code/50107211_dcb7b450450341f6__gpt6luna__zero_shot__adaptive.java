package org.configfacade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
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

    @Test(expected = NullPointerException.class)
    public void validatePathRejectsNull() {
        ConfigFactory.validatePath(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatePathRejectsLeadingDot() {
        ConfigFactory.validatePath(".server");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatePathRejectsTrailingDot() {
        ConfigFactory.validatePath("server.");
    }

    @Test
    public void isKeyRecognizesKeyWithoutDot() {
        assertTrue(ConfigFactory.isKey("port"));
        assertFalse(ConfigFactory.isKey("server.port"));
    }

    @Test
    public void isSubPathRequiresDifferentPathWithParentPrefix() {
        assertTrue(ConfigFactory.isSubPath("server", "server.port"));
        assertFalse(ConfigFactory.isSubPath("server", "server"));
        assertFalse(ConfigFactory.isSubPath("serv", "server.port"));
    }

    @Test
    public void joinPathJoinsElementsWithDots() {
        assertEquals("server.http.port",
                ConfigFactory.joinPath(Arrays.asList("server", "http", "port")));
    }

    @Test
    public void splitPathSplitsOnDots() {
        assertEquals(Arrays.asList("server", "http", "port"),
                ConfigFactory.splitPath("server.http.port"));
    }

    @Test
    public void parentPathReturnsPathBeforeLastSegment() {
        assertEquals("server.http", ConfigFactory.parentPath("server.http.port"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parentPathRejectsSingleKey() {
        ConfigFactory.parentPath("port");
    }

    @Test
    public void filterParentPathsReturnsUniqueParentsInEncounterOrder() {
        Iterator<String> paths = Arrays.asList(
                "server.http.port", "server.http.host", "database.url", "port").iterator();

        Set<String> result = ConfigFactory.filterParentPaths(paths);

        assertEquals(new java.util.LinkedHashSet<String>(
                Arrays.asList("server.http", "database")), result);
    }

    @Test
    public void toConfigMapExposesMapValuesAndKeys() {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        values.put("name", "Ada");
        values.put("count", Integer.valueOf(3));

        ConfigMap map = ConfigFactory.toConfigMap(values);

        assertEquals("Ada", map.get("name"));
        assertTrue(map.containsKey("count"));
        assertEquals(Arrays.asList("name", "count"), toList(map.getRawKeys()));
    }

    @Test
    public void fromMapReadsAndConvertsProperties() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("name", "Ada");
        values.put("count", "3");

        Config config = ConfigFactory.fromMap(values);

        assertEquals("Ada", config.getString("name").get());
        assertEquals(Integer.valueOf(3), config.getInteger("count").get());
    }

    @Test
    public void fromPropertiesReadsPropertyValues() {
        Properties properties = new Properties();
        properties.setProperty("mode", "safe");

        Config config = ConfigFactory.fromProperties(properties);

        assertEquals("safe", config.getString("mode").get());
    }

    @Test
    public void fromConfigMapCreatesConfig() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("enabled", "true");

        Config config = ConfigFactory.from(ConfigFactory.toConfigMap(values));

        assertEquals(Boolean.TRUE, config.getBoolean("enabled").get());
    }

    @Test
    public void chainUsesFirstNonNullValueAndCombinesKeys() {
        Map<String, Object> first = new LinkedHashMap<String, Object>();
        first.put("shared", "first");
        first.put("one", "1");
        Map<String, Object> second = new LinkedHashMap<String, Object>();
        second.put("shared", "second");
        second.put("two", "2");

        ConfigMap chained = ConfigFactory.chain(
                ConfigFactory.toConfigMap(first), ConfigFactory.toConfigMap(second));

        assertEquals("first", chained.get("shared"));
        assertEquals("2", chained.get("two"));
        assertTrue(chained.containsKey("one"));
        assertEquals(Arrays.asList("shared", "one", "shared", "two"),
                toList(chained.getRawKeys()));
    }

    @Test
    public void replaceableWrapsNonReplaceableMap() {
        ConfigMap map = ConfigFactory.toConfigMap(new HashMap<String, Object>());

        ConfigMap.ReplaceableConfigMap replaceable = ConfigFactory.replaceable(map);

        assertNotNull(replaceable);
        assertTrue(replaceable != map);
    }

    @Test
    public void bindReadsInterfaceGetterProperties() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("name", "Ada");
        Config config = ConfigFactory.fromMap(values);

        SampleSettings settings = ConfigFactory.bind(config, SampleSettings.class);

        assertEquals("Ada", settings.getName());
    }

    @Test
    public void bindWithAllowMissingReturnsNullForMissingReferenceProperty() {
        Config config = ConfigFactory.fromMap(new HashMap<String, Object>());
        ConfigFactory.BindConfig bindConfig = new ConfigFactory.BindConfig();
        bindConfig.setAllowMissing(true);

        SampleSettings settings = ConfigFactory.bind(config, SampleSettings.class, bindConfig);

        assertEquals(null, settings.getName());
    }

    @Test
    public void prettyPrintListsPathsAndValues() {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        values.put("name", "Ada");
        values.put("age", "37");

        assertEquals("\nname=Ada\nage=37\n",
                ConfigFactory.prettyPrint(ConfigFactory.fromMap(values)));
    }

    private static List<String> toList(Iterable<String> values) {
        java.util.ArrayList<String> result = new java.util.ArrayList<String>();
        for (String value : values) {
            result.add(value);
        }
        return result;
    }

    public interface SampleSettings {
        String getName();
    }
}
