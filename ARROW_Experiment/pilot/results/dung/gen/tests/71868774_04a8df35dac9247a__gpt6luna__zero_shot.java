package com.addhen.serializer;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class Serializer_ArrowTest {

    @Test
    public void builderCreatesSerializerWithFactory() {
        Serializer serializer = new Serializer.Builder().build();

        assertNotNull(serializer.serializationStrategyFactory());
    }

    @Test
    public void builderUsesConfiguredFactory() {
        SerializationStrategy.Factory factory = new Serializer.Builder()
                .build()
                .serializationStrategyFactory();

        Serializer serializer = new Serializer.Builder()
                .setSerializationStrategyFactory(factory)
                .build();

        assertSame(factory, serializer.serializationStrategyFactory());
    }

    @Test
    public void setSerializationStrategyFactoryReturnsBuilder() {
        Serializer.Builder builder = new Serializer.Builder();

        assertSame(builder, builder.setSerializationStrategyFactory(
                new Serializer.Builder().build().serializationStrategyFactory()));
    }

    @Test
    public void setSerializationStrategyFactoryRejectsNull() {
        Serializer.Builder builder = new Serializer.Builder();

        try {
            builder.setSerializationStrategyFactory(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            // Expected.
        }
    }
}
