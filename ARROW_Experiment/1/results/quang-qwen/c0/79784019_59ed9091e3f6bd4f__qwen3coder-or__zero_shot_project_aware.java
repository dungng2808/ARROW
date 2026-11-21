package io.stardog.stardao.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stardog.stardao.core.Update;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsonHelper_ArrowTest {

    public static class TestObject {
        private String name;
        private int age;

        public TestObject() {}

        public TestObject(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestObject that = (TestObject) o;
            return age == that.age && name.equals(that.name);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + age;
            return result;
        }
    }

    @Test
    public void testObjWithValidJson() {
        String json = "{'name': 'John', 'age': 30}";
        TestObject result = JsonHelper.obj(json, TestObject.class);
        assertEquals("John", result.getName());
        assertEquals(30, result.getAge());
    }

    @Test
    public void testObjectWithValidJson() {
        String json = "{'name': 'Jane', 'age': 25}";
        TestObject result = JsonHelper.object(json, TestObject.class);
        assertEquals("Jane", result.getName());
        assertEquals(25, result.getAge());
    }

    @Test
    public void testUpdateWithValidJson() {
        String json = "{'value': {'name': 'Bob', 'age': 35}}";
        Update<TestObject> result = JsonHelper.update(json, TestObject.class);
        assertNotNull(result);
        TestObject value = result.getValue();
        assertNotNull(value);
        assertEquals("Bob", value.getName());
        assertEquals(35, value.getAge());
    }

    @Test
    public void testJsonWithObject() {
        TestObject obj = new TestObject("Alice", 28);
        String result = JsonHelper.json(obj);
        assertNotNull(result);
        assertTrue(result.contains("Alice"));
        assertTrue(result.contains("28"));
    }

    @Test
    public void testMapperIsInitializedCorrectly() {
        assertNotNull(JsonHelper.MAPPER);
        ObjectMapper mapper = JsonHelper.MAPPER;
        assertTrue(mapper.getFactory().isEnabled(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES));
        assertTrue(mapper.getFactory().isEnabled(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES));
    }
}
