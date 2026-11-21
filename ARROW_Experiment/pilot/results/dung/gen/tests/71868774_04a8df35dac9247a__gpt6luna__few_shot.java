package com.addhen.serializer;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class Serializer_ArrowTest {

    @Test
    public void builderCreatesSerializerWithDefaultFactory() {
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
    public void settingFactoryReturnsBuilder() {
        Serializer.Builder builder = new Serializer.Builder();
        SerializationStrategy.Factory factory = builder.build().serializationStrategyFactory();

        assertSame(builder, builder.setSerializationStrategyFactory(factory));
    }

    @Test(expected = NullPointerException.class)
    public void settingNullFactoryThrows() {
        new Serializer.Builder().setSerializationStrategyFactory(null);
    }

    @Test
    public void strategyDelegatesToFactory() {
        Serializer serializer = new Serializer.Builder().build();

        assertNotNull(serializer.strategy(String.class));
    }
}
