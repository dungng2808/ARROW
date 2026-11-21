package org.microbule.config.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
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
        assertThat(result, equalTo(EmptyConfig.INSTANCE));
    }

    @Test
    public void testCreateWithSingleConfig() {
        Config mockConfig = mock(Config.class);
        List<Config> members = Arrays.asList(mockConfig);
        Config result = CompositeConfig.create(members);
        assertThat(result, equalTo(mockConfig));
    }

    @Test
    public void testCreateWithMultipleConfigs() {
        Config mockConfig1 = mock(Config.class);
        Config mockConfig2 = mock(Config.class);
        List<Config> members = Arrays.asList(mockConfig1, mockConfig2);
        Config result = CompositeConfig.create(members);
        assertThat(result, notNullValue());
        assertThat(result.getClass(), equalTo(CompositeConfig.class));
    }

    @Test
    public void testFiltered() {
        Config mockConfig1 = mock(Config.class);
        Config mockConfig2 = mock(Config.class);
        CompositeConfig compositeConfig = new CompositeConfig(Arrays.asList(mockConfig1, mockConfig2));
        
        Config filteredConfig = compositeConfig.filtered("path1", "path2");
        assertThat(filteredConfig, notNullValue());
        assertThat(filteredConfig.getClass(), equalTo(CompositeConfig.class));
    }

    @Test
    public void testValueReturnsFirstPresent() {
        Config mockConfig1 = mock(Config.class);
        Config mockConfig2 = mock(Config.class);
        when(mockConfig1.value("key")).thenReturn(Optional.empty());
        when(mockConfig2.value("key")).thenReturn(Optional.of("value"));
        
        CompositeConfig compositeConfig = new CompositeConfig(Arrays.asList(mockConfig1, mockConfig2));
        Optional<String> result = compositeConfig.value("key");
        
        assertThat(result.isPresent(), equalTo(true));
        assertThat(result.get(), equalTo("value"));
    }

    @Test
    public void testValueReturnsEmptyWhenNoPresent() {
        Config mockConfig1 = mock(Config.class);
        Config mockConfig2 = mock(Config.class);
        when(mockConfig1.value("key")).thenReturn(Optional.empty());
        when(mockConfig2.value("key")).thenReturn(Optional.empty());
        
        CompositeConfig compositeConfig = new CompositeConfig(Arrays.asList(mockConfig1, mockConfig2));
        Optional<String> result = compositeConfig.value("key");
        
        assertThat(result.isPresent(), equalTo(false));
    }

    @Test
    public void testConstructorWithVarargs() {
        Config mockConfig1 = mock(Config.class);
        Config mockConfig2 = mock(Config.class);
        CompositeConfig compositeConfig = new CompositeConfig(mockConfig1, mockConfig2);
        assertThat(compositeConfig, notNullValue());
    }
}
