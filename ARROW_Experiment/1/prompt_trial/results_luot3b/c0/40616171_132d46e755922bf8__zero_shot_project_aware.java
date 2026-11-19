package com.github.kagkarlsson.scheduler.task.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class CompositeParser_ArrowTest {

    @Test
    public void parseReturnsFirstPresentResult() {
        Schedule schedule = new Schedule() {};
        Parser first = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.empty();
            }

            @Override
            public List<String> examples() {
                return Collections.emptyList();
            }
        };
        Parser second = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.of(schedule);
            }

            @Override
            public List<String> examples() {
                return Collections.emptyList();
            }
        };
        Parser third = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                throw new AssertionError("Later parsers should not be called");
            }

            @Override
            public List<String> examples() {
                return Collections.emptyList();
            }
        };

        Optional<Schedule> result = CompositeParser.of(first, second, third).parse("input");

        assertSame(schedule, result.get());
    }

    @Test
    public void parseReturnsEmptyWhenNoDelegateMatches() {
        Parser parser = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.empty();
            }

            @Override
            public List<String> examples() {
                return Collections.emptyList();
            }
        };

        Optional<Schedule> result = CompositeParser.of(parser).parse("input");

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
            public List<String> examples() {
                return Arrays.asList("first-a", "first-b");
            }
        };
        Parser second = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.empty();
            }

            @Override
            public List<String> examples() {
                return Collections.singletonList("second");
            }
        };

        List<String> result = CompositeParser.of(first, second).examples();

        assertEquals(Arrays.asList("first-a", "first-b", "second"), result);
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
