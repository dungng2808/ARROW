package org.microbule.config.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.microbule.config.api.Config;

public class CompositeConfig_ArrowTest {

    @Test
    public void testCreateWithEmptyList() {
        Config result = CompositeConfig.create(Arrays.asList());
        assertSame(EmptyConfig.INSTANCE, result);
    }

    @Test
    public void testCreateWithSingleConfig() {
        Config singleConfig = mock(Config.class);
        List<Config> members = Arrays.asList(singleConfig);
        Config result = CompositeConfig.create(members);
        assertSame(singleConfig, result);
    }

    @Test
    public void testCreateWithMultipleConfigs() {
        Config config1 = mock(Config.class);
        Config config2 = mock(Config.class);
        List<Config> members = Arrays.asList(config1, config2);
        Config result = CompositeConfig.create(members);
        
        assertEquals(CompositeConfig.class, result.getClass());
    }

    @Test
    public void testFiltered() {
        Config config1 = mock(Config.class);
        Config config2 = mock(Config.class);
        CompositeConfig composite = new CompositeConfig(config1, config2);
        
        Config filtered1 = mock(Config.class);
        Config filtered2 = mock(Config.class);
        when(config1.filtered(new String[]{"path"})).thenReturn(filtered1);
        when(config2.filtered(new String[]{"path"})).thenReturn(filtered2);
        
        Config result = composite.filtered("path");
        assertEquals(CompositeConfig.class, result.getClass());
    }

    @Test
    public void testValueReturnsFirstPresent() {
        Config config1 = mock(Config.class);
        Config config2 = mock(Config.class);
        CompositeConfig composite = new CompositeConfig(config1, config2);
        
        Optional<String> value1 = Optional.empty();
        Optional<String> value2 = Optional.of("value2");
        when(config1.value("key")).thenReturn(value1);
        when(config2.value("key")).thenReturn(value2);
        
        Optional<String> result = composite.value("key");
        assertEquals(Optional.of("value2"), result);
    }

    @Test
    public void testValueReturnsEmptyWhenNoPresent() {
        Config config1 = mock(Config.class);
        Config config2 = mock(Config.class);
        CompositeConfig composite = new CompositeConfig(config1, config2);
        
        Optional<String> value1 = Optional.empty();
        Optional<String> value2 = Optional.empty();
        when(config1.value("key")).thenReturn(value1);
        when(config2.value("key")).thenReturn(value2);
        
        Optional<String> result = composite.value("key");
        assertEquals(Optional.empty(), result);
    }

    @Test
    public void testValueReturnsFirstValue() {
        Config config1 = mock(Config.class);
        Config config2 = mock(Config.class);
        CompositeConfig composite = new CompositeConfig(config1, config2);
        
        Optional<String> value1 = Optional.of("value1");
        Optional<String> value2 = Optional.of("value2");
        when(config1.value("key")).thenReturn(value1);
        when(config2.value("key")).thenReturn(value2);
        
        Optional<String> result = composite.value("key");
        assertEquals(Optional.of("value1"), result);
    }
}
