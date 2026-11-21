package io.stardog.stardao.jackson;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JsonHelper_ArrowTest {

    public static class Person {
        public String name;
        public int age;

        public Person() {
        }
    }

    @Test
    public void objParsesSingleQuotedJsonWithUnquotedFieldNames() {
        Person person = JsonHelper.obj("{name:'Ada',age:37}", Person.class);

        assertEquals("Ada", person.name);
        assertEquals(37, person.age);
    }

    @Test
    public void objectParsesJson() {
        Person person = JsonHelper.object("{\"name\":\"Grace\",\"age\":85}", Person.class);

        assertEquals("Grace", person.name);
        assertEquals(85, person.age);
    }

    @Test
    public void jsonSerializesObject() {
        Person person = new Person();
        person.name = "Lin";
        person.age = 12;

        assertEquals("{\"name\":\"Lin\",\"age\":12}", JsonHelper.json(person));
    }

    @Test
    public void jsonSerializesNull() {
        assertEquals("null", JsonHelper.json(null));
    }

    @Test
    public void updateDeserializesUpdate() {
        assertNotNull(JsonHelper.update("{}", String.class));
    }

    @Test(expected = RuntimeException.class)
    public void objWrapsInvalidJsonFailure() {
        JsonHelper.obj("{", Person.class);
    }

    @Test(expected = RuntimeException.class)
    public void jsonWrapsSerializationFailure() {
        JsonHelper.json(new Object());
    }
}
