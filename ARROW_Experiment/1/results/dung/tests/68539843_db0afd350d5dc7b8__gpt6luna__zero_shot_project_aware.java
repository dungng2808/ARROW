package org.microbule.config.core;

import org.junit.Test;
import org.microbule.config.api.Config;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class CompositeConfig_ArrowTest {

    private static class StubConfig implements Config {
        private final Optional<String> result;
        private final Config filteredResult;

        StubConfig(Optional<String> result, Config filteredResult) {
            this.result = result;
            this.filteredResult = filteredResult;
        }

        @Override
        public Config filtered(String... paths) {
            return filteredResult;
        }

        @Override
        public Optional<String> value(String key) {
            return result;
        }
    }

    @Test
    public void valueReturnsFirstPresentValue() {
        Config first = new StubConfig(Optional.<String>empty(), null);
        Config second = new StubConfig(Optional.of("found"), null);
        Config third = new StubConfig(Optional.of("later"), null);

        CompositeConfig config = new CompositeConfig(first, second, third);

        assertEquals(Optional.of("found"), config.value("key"));
    }

    @Test
    public void valueReturnsEmptyWhenNoMemberHasValue() {
        CompositeConfig config = new CompositeConfig(
                new StubConfig(Optional.<String>empty(), null),
                new StubConfig(Optional.<String>empty(), null));

        assertEquals(Optional.<String>empty(), config.value("missing"));
    }

    @Test
    public void createReturnsEmptyConfigForEmptyMembers() {
        Config config = CompositeConfig.create(Collections.<Config>emptyList());

        assertEquals(Optional.<String>empty(), config.value("key"));
    }

    @Test
    public void createReturnsOnlyMemberForSingleMember() {
        Config member = new StubConfig(Optional.of("value"), null);

        assertSame(member, CompositeConfig.create(Collections.singletonList(member)));
    }

    @Test
    public void createComposesMultipleMembers() {
        Config first = new StubConfig(Optional.<String>empty(), null);
        Config second = new StubConfig(Optional.of("value"), null);

        Config config = CompositeConfig.create(Arrays.asList(first, second));

        assertEquals(Optional.of("value"), config.value("key"));
    }

    @Test
    public void filteredFiltersEveryMemberAndReturnsComposite() {
        Config filteredFirst = new StubConfig(Optional.of("first"), null);
        Config filteredSecond = new StubConfig(Optional.of("second"), null);
        Config first = new StubConfig(Optional.<String>empty(), filteredFirst);
        Config second = new StubConfig(Optional.<String>empty(), filteredSecond);

        Config filtered = new CompositeConfig(first, second).filtered("path");

        assertEquals(Optional.of("first"), filtered.value("key"));
    }
}
