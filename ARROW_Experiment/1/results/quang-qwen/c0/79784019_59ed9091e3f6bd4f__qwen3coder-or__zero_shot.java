package io.stardog.stardao.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stardog.stardao.core.Update;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsonHelper_ArrowTest {

    public static class TestClass {
        private String name;
        private int value;

        public TestClass() {}

        public TestClass(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestClass testClass = (TestClass) o;
            return value == testClass.value && 
                   (name != null ? name.equals(testClass.name) : testClass.name == null);
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + value;
            return result;
        }
    }

    @Test
    public void testObjWithValidJson() {
        String json = "{'name': 'test', 'value': 42}";
        TestClass result = JsonHelper.obj(json, TestClass.class);
        assertNotNull(result);
        assertEquals("test", result.getName());
        assertEquals(42, result.getValue());
    }

    @Test
    public void testObjectWithValidJson() {
        String json = "{'name': 'test', 'value': 42}";
        TestClass result = JsonHelper.object(json, TestClass.class);
        assertNotNull(result);
        assertEquals("test", result.getName());
        assertEquals(42, result.getValue());
    }

    @Test
    public void testUpdateWithValidJson() {
        String json = "{'value': {'name': 'test', 'value': 42}}";
        Update<TestClass> result = JsonHelper.update(json, TestClass.class);
        assertNotNull(result);
        TestClass updateValue = result.getValue();
        assertNotNull(updateValue);
        assertEquals("test", updateValue.getName());
        assertEquals(42, updateValue.getValue());
    }

    @Test
    public void testJsonWithObject() {
        TestClass testObj = new TestClass("test", 42);
        String result = JsonHelper.json(testObj);
        assertNotNull(result);
        assertTrue(result.contains("test"));
        assertTrue(result.contains("42"));
    }

    @Test
    public void testMapperIsInitialized() {
        assertNotNull(JsonHelper.MAPPER);
        ObjectMapper mapper = JsonHelper.MAPPER;
        assertTrue(mapper.getFactory().isEnabled(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES));
        assertTrue(mapper.getFactory().isEnabled(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES));
    }
}
