package de.monticore.generating.templateengine;

import de.se_rwth.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ObjectFactory_ArrowTest {

    @Before
    public void setUp() {
        // Reset the factory to null before each test
        ObjectFactory.setClassLoader(null);
    }

    @Test
    public void testCreateObjectWithNoParams() {
        Object result = ObjectFactory.createObject("java.lang.String");
        assertNotNull(result);
        assertTrue(result instanceof String);
    }

    @Test
    public void testCreateObjectWithParams() {
        List<Object> params = new ArrayList<>();
        params.add("test");
        Object result = ObjectFactory.createObject("java.lang.String", params);
        assertNotNull(result);
        assertTrue(result instanceof String);
        assertEquals("test", result);
    }

    @Test
    public void testCreateObjectWithParamTypesAndParams() {
        List<Class<?>> paramTypes = new ArrayList<>();
        paramTypes.add(String.class);
        List<Object> params = new ArrayList<>();
        params.add("test");
        Object result = ObjectFactory.createObject("java.lang.String", paramTypes, params);
        assertNotNull(result);
        assertTrue(result instanceof String);
        assertEquals("test", result);
    }

    @Test
    public void testCreateObjectWithNonExistentClass() {
        Object result = ObjectFactory.createObject("non.existent.Class");
        assertNull(result);
    }

    @Test
    public void testGetTypes() {
        List<Object> params = new ArrayList<>();
        params.add("test");
        params.add(123);
        List<Class<?>> result = ObjectFactory.getTypes(params);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(String.class, result.get(0));
        assertEquals(Integer.class, result.get(1));
    }

    @Test
    public void testSetClassLoader() {
        ClassLoader mockClassLoader = new MockClassLoader();
        ObjectFactory.setClassLoader(mockClassLoader);
        // We can't easily verify the internal state, but we can ensure no exception is thrown
        assertNotNull(mockClassLoader);
    }

    @Test
    public void testDoCreateObjectWithMatchingConstructor() {
        ObjectFactory factory = new ObjectFactory();
        List<Class<?>> paramTypes = new ArrayList<>();
        paramTypes.add(String.class);
        List<Object> params = new ArrayList<>();
        params.add("test");
        Object result = factory.doCreateObject("java.lang.String", paramTypes, params);
        assertNotNull(result);
        assertTrue(result instanceof String);
        assertEquals("test", result);
    }

    @Test
    public void testDoCreateObjectWithNonExistentClass() {
        ObjectFactory factory = new ObjectFactory();
        List<Class<?>> paramTypes = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        Object result = factory.doCreateObject("non.existent.Class", paramTypes, params);
        assertNull(result);
    }

    @Test
    public void testDoGetTypes() {
        ObjectFactory factory = new ObjectFactory();
        List<Object> params = new ArrayList<>();
        params.add("test");
        params.add(123);
        List<Class<?>> result = factory.doGetTypes(params);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(String.class, result.get(0));
        assertEquals(Integer.class, result.get(1));
    }

    @Test
    public void testDoSetClassLoader() {
        ObjectFactory factory = new ObjectFactory();
        ClassLoader mockClassLoader = new MockClassLoader();
        factory.doSetClassLoader(mockClassLoader);
        // We can't easily verify the internal state, but we can ensure no exception is thrown
        assertNotNull(mockClassLoader);
    }

    // Simple mock class loader for testing purposes
    private static class MockClassLoader extends ClassLoader {
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return super.loadClass(name);
        }
    }
}
