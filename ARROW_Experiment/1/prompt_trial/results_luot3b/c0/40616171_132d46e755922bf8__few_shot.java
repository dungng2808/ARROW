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
        Parser parser = CompositeParser.of(
                new StubParser(Optional.<Schedule>empty(), Collections.<String>emptyList()),
                new StubParser(Optional.of(first), Collections.<String>emptyList()),
                new StubParser(Optional.of(second), Collections.<String>emptyList()));

        Optional<Schedule> result = parser.parse("input");

        assertTrue(result.isPresent());
        assertSame(first, result.get());
    }

    @Test
    public void parseReturnsEmptyWhenNoDelegateMatches() {
        Parser parser = CompositeParser.of(
                new StubParser(Optional.<Schedule>empty(), Collections.<String>emptyList()),
                new StubParser(Optional.<Schedule>empty(), Collections.<String>emptyList()));

        assertFalse(parser.parse("input").isPresent());
    }

    @Test
    public void examplesConcatenatesDelegateExamplesInOrder() {
        Parser parser = CompositeParser.of(
                new StubParser(Optional.<Schedule>empty(), Arrays.asList("one", "two")),
                new StubParser(Optional.<Schedule>empty(), Collections.singletonList("three")));

        List<String> result = parser.examples();

        assertEquals(Arrays.asList("one", "two", "three"), result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ofRejectsNullArray() {
        CompositeParser.of((Parser[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ofRejectsEmptyArray() {
        CompositeParser.of();
    }

    private static class StubParser implements Parser {
        private final Optional<Schedule> result;
        private final List<String> examples;

        private StubParser(Optional<Schedule> result, List<String> examples) {
            this.result = result;
            this.examples = examples;
        }

        @Override
        public Optional<Schedule> parse(String scheduleString) {
            return result;
        }

        @Override
        public List<String> examples() {
            return examples;
        }
    }
}
