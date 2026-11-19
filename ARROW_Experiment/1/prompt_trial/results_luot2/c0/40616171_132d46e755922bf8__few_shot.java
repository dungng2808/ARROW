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
        Parser composite = CompositeParser.of(
                scheduleString -> Optional.empty(),
                scheduleString -> Optional.of(first),
                scheduleString -> Optional.of(second));

        Optional<Schedule> result = composite.parse("input");

        assertTrue(result.isPresent());
        assertSame(first, result.get());
    }

    @Test
    public void parseReturnsEmptyWhenNoDelegateMatches() {
        Parser composite = CompositeParser.of(
                scheduleString -> Optional.empty(),
                scheduleString -> Optional.empty());

        assertFalse(composite.parse("input").isPresent());
    }

    @Test
    public void examplesConcatenatesDelegateExamplesInOrder() {
        Parser composite = CompositeParser.of(
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

        assertEquals(Arrays.asList("first", "second", "third"), composite.examples());
    }

    @Test
    public void examplesReturnsEmptyListWhenDelegatesHaveNoExamples() {
        Parser composite = CompositeParser.of(new Parser() {
            @Override
            public Optional<Schedule> parse(String scheduleString) {
                return Optional.empty();
            }

            @Override
            public List<String> examples() {
                return Collections.emptyList();
            }
        });

        assertEquals(Collections.emptyList(), composite.examples());
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
