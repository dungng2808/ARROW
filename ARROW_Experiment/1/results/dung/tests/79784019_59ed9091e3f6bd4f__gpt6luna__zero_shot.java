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
    public void objParsesSingleQuotedJsonWithUnquotedFieldNames() {
        Sample result = JsonHelper.obj("{name:'Ada',count:3}", Sample.class);

        assertEquals("Ada", result.name);
        assertEquals(3, result.count);
    }

    @Test
    public void objectParsesJson() {
        Sample result = JsonHelper.object("{\"name\":\"Grace\",\"count\":2}", Sample.class);

        assertEquals("Grace", result.name);
        assertEquals(2, result.count);
    }

    @Test
    public void jsonSerializesObject() {
        Sample sample = new Sample();
        sample.name = "Lin";
        sample.count = 4;

        assertEquals("{\"name\":\"Lin\",\"count\":4}", JsonHelper.json(sample));
    }

    @Test
    public void updateDeserializesUpdate() {
        io.stardog.stardao.core.Update<Sample> result =
                JsonHelper.update("{\"value\":{\"name\":\"Ada\",\"count\":3}}", Sample.class);

        assertNotNull(result);
    }
}
