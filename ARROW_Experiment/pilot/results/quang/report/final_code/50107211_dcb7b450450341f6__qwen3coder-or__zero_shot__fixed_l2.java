package org.configfacade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.configfacade.Config.Property;
import org.configfacade.Config.Type;
import org.configfacade.ConfigMap.ReplaceableConfigMap;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.FutureCallback;

public class ConfigFactory_ArrowTest {

    @Test
    public void testFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 42);
        
        Config config = ConfigFactory.fromMap(map);
        assertNotNull(config);
        
        Property<String> stringProp = config.getString("key1");
        assertEquals("value1", stringProp.get());
        
        Property<Integer> intProp = config.getInteger("key2");
        assertEquals(Integer.valueOf(42), intProp.get());
    }
    
    @Test
    public void testFromProperties() {
        Properties props = new Properties();
        props.setProperty("prop1", "value1");
        props.setProperty("prop2", "123");
        
        Config config = ConfigFactory.fromProperties(props);
        assertNotNull(config);
        
        Property<String> stringProp = config.getString("prop1");
        assertEquals("value1", stringProp.get());
        
        Property<Integer> intProp = config.getInteger("prop2");
        assertEquals(Integer.valueOf(123), intProp.get());
    }
    
    @Test
    public void testBind() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "test");
        map.put("count", "5");
        
        Config config = ConfigFactory.fromMap(map);
        
        TestInterface proxy = ConfigFactory.bind(config, TestInterface.class);
        assertNotNull(proxy);
        
        assertEquals("test", proxy.getName());
        assertEquals(5, proxy.getCount());
    }
    
    @Test
    public void testBindWithMissing() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "test");
        
        Config config = ConfigFactory.fromMap(map);
        ConfigFactory.BindConfig bindConfig = new ConfigFactory.BindConfig();
        bindConfig.setAllowMissing(true);
        
        TestInterface proxy = ConfigFactory.bind(config, TestInterface.class, bindConfig);
        assertNotNull(proxy);
        
        assertEquals("test", proxy.getName());
        assertNull(proxy.getCount()); // Should be null since count is missing and allowMissing is true
    }
    
    @Test
    public void testPropertyMethodResolve() {
        Method method = null;
        try {
            method = TestInterface.class.getMethod("getName");
        } catch (Exception e) {
            // Should not happen
        }
        
        // Accessing PropertyMethod through reflection since it's private
        try {
            Class<?> propertyMethodClass = ConfigFactory.class.getDeclaredClasses()[0];
            Method resolveMethod = propertyMethodClass.getDeclaredMethod("resolve", Method.class);
            resolveMethod.setAccessible(true);
            Object pm = resolveMethod.invoke(null, method);
            
            assertNotNull(pm);
            // Get name field
            java.lang.reflect.Field nameField = propertyMethodClass.getDeclaredField("name");
            nameField.setAccessible(true);
            assertEquals("name", nameField.get(pm));
            
            // Get type field
            java.lang.reflect.Field typeField = propertyMethodClass.getDeclaredField("type");
            typeField.setAccessible(true);
            assertEquals(Type.STRING, typeField.get(pm));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void testPropertyMethodResolveWithIs() {
        Method method = null;
        try {
            method = TestInterface.class.getMethod("isEnabled");
        } catch (Exception e) {
            // Should not happen
        }
        
        // Accessing PropertyMethod through reflection since it's private
        try {
            Class<?> propertyMethodClass = ConfigFactory.class.getDeclaredClasses()[0];
            Method resolveMethod = propertyMethodClass.getDeclaredMethod("resolve", Method.class);
            resolveMethod.setAccessible(true);
            Object pm = resolveMethod.invoke(null, method);
            
            assertNotNull(pm);
            // Get name field
            java.lang.reflect.Field nameField = propertyMethodClass.getDeclaredField("name");
            nameField.setAccessible(true);
            assertEquals("enabled", nameField.get(pm));
            
            // Get type field
            java.lang.reflect.Field typeField = propertyMethodClass.getDeclaredField("type");
            typeField.setAccessible(true);
            assertEquals(Type.BOOLEAN, typeField.get(pm));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void testPropertyMethodResolveInvalid() {
        Method method = null;
        try {
            method = TestInterface.class.getMethod("toString");
        } catch (Exception e) {
            // Should not happen
        }
        
        // Accessing PropertyMethod through reflection since it's private
        try {
            Class<?> propertyMethodClass = ConfigFactory.class.getDeclaredClasses()[0];
            Method resolveMethod = propertyMethodClass.getDeclaredMethod("resolve", Method.class);
            resolveMethod.setAccessible(true);
            Object pm = resolveMethod.invoke(null, method);
            assertNull(pm);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void testChain() {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("key1", "value1");
        
        Map<String, Object> map2 = new HashMap<>();
        map2.put("key2", "value2");
        
        ConfigMap chained = ConfigFactory.chain(
            ConfigFactory.toConfigMap(map1),
            ConfigFactory.toConfigMap(map2)
        );
        
        assertEquals("value1", chained.get("key1"));
        assertEquals("value2", chained.get("key2"));
    }
    
    @Test
    public void testPrettyPrint() {
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        
        Config config = ConfigFactory.fromMap(map);
        String pretty = ConfigFactory.prettyPrint(config);
        assertNotNull(pretty);
        assertTrue(pretty.contains("key1=value1"));
        assertTrue(pretty.contains("key2=value2"));
    }
    
    @Test
    public void testValidatePath() {
        String path = "valid.path";
        assertEquals(path, ConfigFactory.validatePath(path));
        
        try {
            ConfigFactory.validatePath(null);
            assertTrue(false); // Should not reach here
        } catch (Exception e) {
            // Expected
        }
        
        try {
            ConfigFactory.validatePath("invalid.");
            assertTrue(false); // Should not reach here
        } catch (Exception e) {
            // Expected
        }
        
        try {
            ConfigFactory.validatePath(".invalid");
            assertTrue(false); // Should not reach here
        } catch (Exception e) {
            // Expected
        }
    }
    
    @Test
    public void testIsKey() {
        assertTrue(ConfigFactory.isKey("key"));
        assertFalse(ConfigFactory.isKey("key.subkey"));
    }
    
    @Test
    public void testJoinPath() {
        List<String> elements = java.util.Arrays.asList("a", "b", "c");
        assertEquals("a.b.c", ConfigFactory.joinPath(elements));
    }
    
    @Test
    public void testSplitPath() {
        String path = "a.b.c";
        List<String> result = ConfigFactory.splitPath(path);
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
    }
    
    @Test
    public void testParentPath() {
        String path = "parent.child";
        assertEquals("parent", ConfigFactory.parentPath(path));
    }
    
    @Test
    public void testFilterParentPaths() {
        java.util.List<String> paths = java.util.Arrays.asList("a.b", "a.c", "d.e.f");
        java.util.Iterator<String> iterator = paths.iterator();
        java.util.Set<String> result = ConfigFactory.filterParentPaths(iterator);
        assertEquals(2, result.size());
        assertTrue(result.contains("a"));
        assertTrue(result.contains("d.e"));
    }
    
    @Test
    public void testFromBean() {
        TestBean bean = new TestBean();
        bean.setName("testBean");
        bean.setCount(100);
        
        Config config = ConfigFactory.fromBean(TestBean.class, bean);
        assertNotNull(config);
        
        Property<String> stringProp = config.getString("name");
        assertEquals("testBean", stringProp.get());
        
        Property<Integer> intProp = config.getInteger("count");
        assertEquals(Integer.valueOf(100), intProp.get());
    }
    
    public static interface TestInterface {
        String getName();
        int getCount();
        boolean isEnabled();
    }
    
    public static class TestBean {
        private String name;
        private int count;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
        
        public boolean isEnabled() {
            return true;
        }
    }
}
