package org.microbule.config.core;

import org.junit.Test;
import org.microbule.config.api.Config;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class CompositeConfig_ArrowTest {

    @Test
    public void valueReturnsFirstPresentValue() {
        Config first = new Config() {
            @Override
            public Config filtered(String... paths) {
                return this;
            }

            @Override
            public Optional<String> value(String key) {
                return Optional.empty();
            }
        };
        Config second = new Config() {
            @Override
            public Config filtered(String... paths) {
                return this;
            }

            @Override
            public Optional<String> value(String key) {
                return Optional.of("second");
            }
        };
        Config third = new Config() {
            @Override
            public Config filtered(String... paths) {
                return this;
            }

            @Override
            public Optional<String> value(String key) {
                return Optional.of("third");
            }
        };

        CompositeConfig config = new CompositeConfig(first, second, third);

        assertEquals(Optional.of("second"), config.value("key"));
    }

    @Test
    public void valueReturnsEmptyWhenNoMemberHasValue() {
        Config empty = new Config() {
            @Override
            public Config filtered(String... paths) {
                return this;
            }

            @Override
            public Optional<String> value(String key) {
                return Optional.empty();
            }
        };

        CompositeConfig config = new CompositeConfig(Collections.singletonList(empty));

        assertEquals(Optional.empty(), config.value("missing"));
    }

    @Test
    public void createReturnsEmptyConfigForEmptyList() {
        Config result = CompositeConfig.create(Collections.<Config>emptyList());

        assertEquals(Optional.empty(), result.value("key"));
    }

    @Test
    public void createReturnsOnlyMemberForSingleElement() {
        Config member = new Config() {
            @Override
            public Config filtered(String... paths) {
                return this;
            }

            @Override
            public Optional<String> value(String key) {
                return Optional.of("value");
            }
        };

        Config result = CompositeConfig.create(Collections.singletonList(member));

        assertSame(member, result);
    }

    @Test
    public void filteredFiltersEveryMember() {
        final String[] receivedPaths = new String[1];
        Config member = new Config() {
            @Override
            public Config filtered(String... paths) {
                receivedPaths[0] = Arrays.toString(paths);
                return this;
            }

            @Override
            public Optional<String> value(String key) {
                return Optional.empty();
            }
        };

        CompositeConfig config = new CompositeConfig(member);
        Config filtered = config.filtered("a", "b");

        assertEquals("[a, b]", receivedPaths[0]);
        assertEquals(Optional.empty(), filtered.value("key"));
    }
}
