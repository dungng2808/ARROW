package io.stardog.stardao.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stardog.stardao.core.Update;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsonHelper_ArrowTest {

    public static class TestClass {
        private String name;
        private int age;

        public TestClass() {}

        public TestClass(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestClass testClass = (TestClass) o;
            return age == testClass.age && 
                   (name != null ? name.equals(testClass.name) : testClass.name == null);
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + age;
            return result;
        }
    }

    @Test
    public void testObj() {
        String json = "{'name': 'John', 'age': 30}";
        TestClass result = JsonHelper.obj(json, TestClass.class);
        assertNotNull(result);
        assertEquals("John", result.getName());
        assertEquals(30, result.getAge());
    }

    @Test
    public void testObject() {
        String json = "{'name': 'Jane', 'age': 25}";
        TestClass result = JsonHelper.object(json, TestClass.class);
        assertNotNull(result);
        assertEquals("Jane", result.getName());
        assertEquals(25, result.getAge());
    }

    @Test
    public void testUpdate() {
        String json = "{'value': {'name': 'Bob', 'age': 35}}";
        Update<TestClass> result = JsonHelper.update(json, TestClass.class);
        assertNotNull(result);
        TestClass value = result.getValue();
        assertNotNull(value);
        assertEquals("Bob", value.getName());
        assertEquals(35, value.getAge());
    }

    @Test
    public void testJson() {
        TestClass obj = new TestClass("Alice", 28);
        String json = JsonHelper.json(obj);
        assertNotNull(json);
        assertTrue(json.contains("Alice"));
        assertTrue(json.contains("28"));
    }

    @Test
    public void testMapperConfiguration() {
        ObjectMapper mapper = JsonHelper.MAPPER;
        assertNotNull(mapper);
        // Verify that the mapper has the expected configurations
        assertTrue(mapper.getFactory().isEnabled(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES));
        assertTrue(mapper.getFactory().isEnabled(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES));
    }
}
