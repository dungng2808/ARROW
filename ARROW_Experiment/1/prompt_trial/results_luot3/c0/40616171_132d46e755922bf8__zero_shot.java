package com.github.kagkarlsson.scheduler.task.schedule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class CompositeParser_ArrowTest {

    @Test
    public void parseReturnsFirstMatchingSchedule() {
        final Schedule first = new Schedule() {};
        final Schedule second = new Schedule() {};
        Parser parser = CompositeParser.of(
                new Parser() {
                    @Override
                    public Optional<Schedule> parse(String scheduleString) {
                        return Optional.empty();
                    }

                    @Override
                    public List<String> examples() {
                        return Collections.emptyList();
                    }
                },
                new Parser() {
                    @Override
                    public Optional<Schedule> parse(String scheduleString) {
                        return Optional.of(first);
                    }

                    @Override
                    public List<String> examples() {
                        return Collections.emptyList();
                    }
                },
                new Parser() {
                    @Override
                    public Optional<Schedule> parse(String scheduleString) {
                        return Optional.of(second);
                    }

                    @Override
                    public List<String> examples() {
                        return Collections.emptyList();
                    }
                });

        assertEquals(Optional.of(first), parser.parse("input"));
    }

    @Test
    public void parseReturnsEmptyWhenNoDelegateMatches() {
        Parser parser = CompositeParser.of(new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.empty();
            }

            @Override
            public List<String> examples() {
                return Collections.emptyList();
            }
        });

        assertFalse(parser.parse("input").isPresent());
    }

    @Test
    public void examplesConcatenatesDelegateExamplesInOrder() {
        Parser parser = CompositeParser.of(
                new Parser() {
                    @Override
                    public Optional<Schedule> parse(String scheduleString) {
                        return Optional.empty();
                    }

                    @Override
                    public List<String> examples() {
                        return Arrays.asList("first", "second");
                    }
                },
                new Parser() {
                    @Override
                    public Optional<Schedule> parse(String scheduleString) {
                        return Optional.empty();
                    }

                    @Override
                    public List<String> examples() {
                        return Collections.singletonList("third");
                    }
                });

        assertEquals(Arrays.asList("first", "second", "third"), parser.examples());
    }

    @Test
    public void examplesIsEmptyWhenDelegatesHaveNoExamples() {
        Parser parser = CompositeParser.of(new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.empty();
            }

            @Override
            public List<String> examples() {
                return Collections.emptyList();
            }
        });

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
