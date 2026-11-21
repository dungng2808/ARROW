package io.stardog.stardao.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class JsonHelper_ArrowTest {

    public static class Sample {
        public String name;
        public int count;

        public Sample() {
        }
    }

    @Test
    public void objReadsSingleQuotedJsonWithUnquotedFieldNames() {
        Sample sample = JsonHelper.obj("{name:'Ada',count:3}", Sample.class);

        assertEquals("Ada", sample.name);
        assertEquals(3, sample.count);
    }

    @Test
    public void objectReadsJson() {
        Sample sample = JsonHelper.object("{\"name\":\"Grace\",\"count\":2}", Sample.class);

        assertEquals("Grace", sample.name);
        assertEquals(2, sample.count);
    }

    @Test
    public void jsonSerializesObject() {
        Sample sample = new Sample();
        sample.name = "Lin";
        sample.count = 4;

        String json = JsonHelper.json(sample);

        assertEquals("{\"name\":\"Lin\",\"count\":4}", json);
    }

    @Test
    public void jsonSerializesNull() {
        assertEquals("null", JsonHelper.json(null));
    }

    @Test
    public void objWrapsInvalidJsonInRuntimeException() {
        try {
            JsonHelper.obj("{", Sample.class);
        } catch (RuntimeException expected) {
            assertNotNull(expected.getCause());
            return;
        }
        throw new AssertionError("Expected RuntimeException");
    }
}
