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
            public java.time.Instant getNextExecutionTime(java.time.Instant now) {
                return now;
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
                return Optional.empty();
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
    public void parseReturnsEmptyWhenNoDelegateMatches() {
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
                return Optional.empty();
            }

            @Override
            public java.util.List<String> examples() {
                return Collections.emptyList();
            }
        };

        Optional<Schedule> result = CompositeParser.of(first, second).parse("input");

        assertFalse(result.isPresent());
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
    public void examplesReturnsEmptyForDelegatesWithoutExamples() {
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

        assertEquals(Collections.emptyList(), CompositeParser.of(parser).examples());
    }

    @Test
    public void ofRejectsNullArray() {
        assertThrows(IllegalArgumentException.class, () -> CompositeParser.of((Parser[]) null));
    }

    @Test
    public void ofRejectsEmptyArray() {
        assertThrows(IllegalArgumentException.class, () -> CompositeParser.of());
    }
}
