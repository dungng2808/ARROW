package org.fuin.esc.spi;

import jakarta.json.JsonStructure;
import org.fuin.esc.api.ToJsonCapable;
import org.fuin.objects4j.common.Contract;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DataWrapper_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        final DataWrapper wrapper = new DataWrapper();
        assertThat(wrapper).isNotNull();
    }

    @Test
    public void testConstructorWithNull() {
        assertThatThrownBy(() -> new DataWrapper(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("obj");
    }

    @Test
    public void testConstructorWithValidObject() {
        final Object testObj = new Object();
        final DataWrapper wrapper = new DataWrapper(testObj);
        assertThat(wrapper.getObj()).isSameAs(testObj);
    }

    @Test
    public void testGetObj() {
        final Object testObj = "test";
        final DataWrapper wrapper = new DataWrapper(testObj);
        assertThat(wrapper.getObj()).isSameAs(testObj);
    }

    @Test
    public void testToJsonWithToJsonCapable() {
        final ToJsonCapable mockToJson = new ToJsonCapable() {
            @Override
            public JsonStructure toJson() {
                return new JsonStructure() {};
            }
        };

        final DataWrapper wrapper = new DataWrapper(mockToJson);
        final JsonStructure result = wrapper.toJson();
        assertThat(result).isNotNull();
    }

    @Test
    public void testToJsonWithJsonStructure() {
        final JsonStructure mockJsonStructure = new JsonStructure() {};

        final DataWrapper wrapper = new DataWrapper(mockJsonStructure);
        final JsonStructure result = wrapper.toJson();
        assertThat(result).isSameAs(mockJsonStructure);
    }

    @Test
    public void testToJsonWithOtherObject() {
        final Object testObj = new Object();

        final DataWrapper wrapper = new DataWrapper(testObj);
        assertThatThrownBy(() -> wrapper.toJson())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Wrapped object is not an instance of")
                .hasMessageContaining(ToJsonCapable.class.getSimpleName())
                .hasMessageContaining(JsonStructure.class.getSimpleName());
    }

}
