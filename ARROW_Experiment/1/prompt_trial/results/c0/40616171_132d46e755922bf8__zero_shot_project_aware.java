package com.github.kagkarlsson.scheduler.task.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class CompositeParser_ArrowTest {

    @Test
    public void parseReturnsFirstPresentResult() {
        Schedule schedule = new Schedule() {
            @Override
            public java.time.Duration getNextExecutionTime(java.time.Instant now) {
                return java.time.Duration.ZERO;
            }
        };
        Parser first = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.empty();
            }

            @Override
            public java.util.List<String> examples() {
                return Collections.emptyList();
            }
        };
        Parser second = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.of(schedule);
            }

            @Override
            public java.util.List<String> examples() {
                return Collections.emptyList();
            }
        };
        Parser third = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                throw new AssertionError("Later parsers should not be called");
            }

            @Override
            public java.util.List<String> examples() {
                return Collections.emptyList();
            }
        };

        Optional<Schedule> result = CompositeParser.of(first, second, third).parse("input");

        assertTrue(result.isPresent());
        assertSame(schedule, result.get());
    }

    @Test
    public void parseReturnsEmptyWhenNoDelegateParses() {
        Parser parser = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.empty();
            }

            @Override
            public java.util.List<String> examples() {
                return Collections.emptyList();
            }
        };

        assertEquals(Optional.empty(), CompositeParser.of(parser).parse("input"));
    }

    @Test
    public void examplesConcatenatesDelegateExamplesInOrder() {
        Parser first = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.empty();
            }

            @Override
            public java.util.List<String> examples() {
                return Arrays.asList("first-a", "first-b");
            }
        };
        Parser second = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.empty();
            }

            @Override
            public java.util.List<String> examples() {
                return Collections.singletonList("second");
            }
        };

        assertEquals(Arrays.asList("first-a", "first-b", "second"),
                CompositeParser.of(first, second).examples());
    }

    @Test
    public void examplesIsEmptyWhenDelegateExamplesAreEmpty() {
        Parser parser = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.empty();
            }

            @Override
            public java.util.List<String> examples() {
                return Collections.emptyList();
            }
        };

        assertTrue(CompositeParser.of(parser).examples().isEmpty());
    }

    @Test
    public void ofRejectsNullParserArray() {
        assertThrows(IllegalArgumentException.class, () -> CompositeParser.of((Parser[]) null));
    }

    @Test
    public void ofRejectsEmptyParserArray() {
        assertThrows(IllegalArgumentException.class, () -> CompositeParser.of());
    }
}
