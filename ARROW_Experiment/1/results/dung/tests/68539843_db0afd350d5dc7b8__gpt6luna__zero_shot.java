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
                return Optional.of("first");
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

        CompositeConfig config = new CompositeConfig(first, second);

        assertEquals(Optional.of("first"), config.value("key"));
    }

    @Test
    public void valueSkipsAbsentValues() {
        Config absent = new Config() {
            @Override
            public Config filtered(String... paths) {
                return this;
            }

            @Override
            public Optional<String> value(String key) {
                return Optional.empty();
            }
        };
        Config present = new Config() {
            @Override
            public Config filtered(String... paths) {
                return this;
            }

            @Override
            public Optional<String> value(String key) {
                return Optional.of("found");
            }
        };

        CompositeConfig config = new CompositeConfig(absent, present);

        assertEquals(Optional.of("found"), config.value("key"));
    }

    @Test
    public void valueReturnsEmptyWhenNoMemberHasValue() {
        Config absent = new Config() {
            @Override
            public Config filtered(String... paths) {
                return this;
            }

            @Override
            public Optional<String> value(String key) {
                return Optional.empty();
            }
        };

        CompositeConfig config = new CompositeConfig(Collections.singletonList(absent));

        assertEquals(Optional.empty(), config.value("key"));
    }

    @Test
    public void filteredFiltersEachMember() {
        Config member = new Config() {
            @Override
            public Config filtered(String... paths) {
                return new Config() {
                    @Override
                    public Config filtered(String... nestedPaths) {
                        return this;
                    }

                    @Override
                    public Optional<String> value(String key) {
                        return Optional.of(paths[0] + ":" + key);
                    }
                };
            }

            @Override
            public Optional<String> value(String key) {
                return Optional.empty();
            }
        };

        Config filtered = new CompositeConfig(member).filtered("section");

        assertEquals(Optional.of("section:key"), filtered.value("key"));
    }

    @Test
    public void createWithNoMembersReturnsEmptyConfig() {
        Config result = CompositeConfig.create(Collections.<Config>emptyList());

        assertEquals(Optional.empty(), result.value("key"));
    }

    @Test
    public void createWithOneMemberReturnsThatMember() {
        Config member = new Config() {
            @Override
            public Config filtered(String... paths) {
                return this;
            }

            @Override
            public Optional<String> value(String key) {
                return Optional.of("single");
            }
        };

        Config result = CompositeConfig.create(Collections.singletonList(member));

        assertSame(member, result);
    }

    @Test
    public void createWithMultipleMembersCombinesThem() {
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

        Config result = CompositeConfig.create(Arrays.asList(first, second));

        assertEquals(Optional.of("second"), result.value("key"));
    }
}
