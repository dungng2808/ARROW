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
        Schedule firstSchedule = new Schedule() {
            @Override
            public Optional<java.time.Instant> nextExecutionTime(java.time.Instant lastExecutionTime, java.time.Instant now) {
                return Optional.empty();
            }
        };
        Schedule secondSchedule = new Schedule() {
            @Override
            public Optional<java.time.Instant> nextExecutionTime(java.time.Instant lastExecutionTime, java.time.Instant now) {
                return Optional.empty();
            }
        };

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
                return Optional.of(firstSchedule);
            }

            @Override
            public List<String> examples() {
                return Collections.emptyList();
            }
        };
        Parser third = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.of(secondSchedule);
            }

            @Override
            public List<String> examples() {
                return Collections.emptyList();
            }
        };

        Optional<Schedule> result = CompositeParser.of(first, second, third).parse("input");

        assertTrue(result.isPresent());
        assertSame(firstSchedule, result.get());
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

    @Test(expected = IllegalArgumentException.class)
    public void ofRejectsNullParserArray() {
        CompositeParser.of((Parser[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ofRejectsEmptyParserArray() {
        CompositeParser.of();
    }
}
