package com.github.kagkarlsson.scheduler.task.schedule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
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
            public java.util.List<String> examples() {
                return Collections.emptyList();
            }
        };
        Parser presentFirst = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.of(first);
            }

            @Override
            public java.util.List<String> examples() {
                return Collections.emptyList();
            }
        };
        Parser presentSecond = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.of(second);
            }

            @Override
            public java.util.List<String> examples() {
                return Collections.emptyList();
            }
        };

        CompositeParser parser = CompositeParser.of(absent, presentFirst, presentSecond);

        assertSame(first, parser.parse("schedule").get());
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

        CompositeParser parser = CompositeParser.of(first, second);

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

        CompositeParser parser = CompositeParser.of(first, second);

        assertEquals(Arrays.asList("first-a", "first-b", "second"), parser.examples());
    }

    @Test
    public void ofAcceptsSingleDelegate() {
        Parser delegate = new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.empty();
            }

            @Override
            public java.util.List<String> examples() {
                return Collections.singletonList("example");
            }
        };

        CompositeParser parser = CompositeParser.of(delegate);

        assertEquals(Collections.singletonList("example"), parser.examples());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ofRejectsNullArray() {
        CompositeParser.of((Parser[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ofRejectsEmptyArray() {
        CompositeParser.of();
    }
}
