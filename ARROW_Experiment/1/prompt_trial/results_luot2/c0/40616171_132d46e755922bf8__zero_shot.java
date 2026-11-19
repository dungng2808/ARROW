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
        Schedule firstSchedule = null;
        Schedule secondSchedule = null;
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
                return Optional.of(secondSchedule);
            }

            @Override
            public List<String> examples() {
                return Collections.emptyList();
            }
        };
        Parser third = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.of(firstSchedule);
            }

            @Override
            public List<String> examples() {
                return Collections.emptyList();
            }
        };

        CompositeParser parser = CompositeParser.of(first, second, third);

        assertSame(secondSchedule, parser.parse("schedule").get());
    }

    @Test
    public void parseReturnsEmptyWhenNoDelegateParses() {
        Parser delegate = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.empty();
            }

            @Override
            public List<String> examples() {
                return Collections.emptyList();
            }
        };

        CompositeParser parser = CompositeParser.of(delegate);

        assertFalse(parser.parse("unknown").isPresent());
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

        CompositeParser parser = CompositeParser.of(first, second);

        assertEquals(Arrays.asList("first-a", "first-b", "second"), parser.examples());
    }

    @Test
    public void examplesIsEmptyWhenDelegatesHaveNoExamples() {
        Parser delegate = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.empty();
            }

            @Override
            public List<String> examples() {
                return Collections.emptyList();
            }
        };

        CompositeParser parser = CompositeParser.of(delegate);

        assertTrue(parser.examples().isEmpty());
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
