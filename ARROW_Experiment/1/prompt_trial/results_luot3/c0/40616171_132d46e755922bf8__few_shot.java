package com.github.kagkarlsson.scheduler.task.schedule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class CompositeParser_ArrowTest {

    @Test
    public void parseReturnsFirstPresentSchedule() {
        Schedule first = new Schedule() {};
        Schedule second = new Schedule() {};
        Parser absent = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.empty();
            }

            @Override
            public List<String> examples() {
                return Collections.emptyList();
            }
        };
        Parser presentFirst = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.of(first);
            }

            @Override
            public List<String> examples() {
                return Collections.emptyList();
            }
        };
        Parser presentSecond = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.of(second);
            }

            @Override
            public List<String> examples() {
                return Collections.emptyList();
            }
        };

        Optional<Schedule> result =
                CompositeParser.of(absent, presentFirst, presentSecond).parse("schedule");

        assertTrue(result.isPresent());
        assertSame(first, result.get());
    }

    @Test
    public void parseReturnsEmptyWhenNoDelegateParses() {
        Parser absent = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.empty();
            }

            @Override
            public List<String> examples() {
                return Collections.emptyList();
            }
        };

        Optional<Schedule> result = CompositeParser.of(absent).parse("schedule");

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

        assertEquals(
                Arrays.asList("first-a", "first-b", "second"),
                CompositeParser.of(first, second).examples());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ofRejectsNullParsersArray() {
        CompositeParser.of((Parser[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ofRejectsEmptyParsersArray() {
        CompositeParser.of();
    }
}
