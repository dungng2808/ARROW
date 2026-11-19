package com.github.kagkarlsson.scheduler.task.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class CompositeParser_ArrowTest {

    @Test
    public void parseReturnsFirstPresentResult() {
        Parser first = mock(Parser.class);
        Parser second = mock(Parser.class);
        Schedule schedule = mock(Schedule.class);
        when(first.parse("input")).thenReturn(Optional.<Schedule>empty());
        when(second.parse("input")).thenReturn(Optional.of(schedule));

        Optional<Schedule> result = CompositeParser.of(first, second).parse("input");

        assertTrue(result.isPresent());
        assertSame(schedule, result.get());
    }

    @Test
    public void parseReturnsEmptyWhenNoDelegateParses() {
        Parser first = mock(Parser.class);
        Parser second = mock(Parser.class);
        when(first.parse("input")).thenReturn(Optional.<Schedule>empty());
        when(second.parse("input")).thenReturn(Optional.<Schedule>empty());

        Optional<Schedule> result = CompositeParser.of(first, second).parse("input");

        assertFalse(result.isPresent());
    }

    @Test
    public void examplesConcatenatesDelegateExamplesInOrder() {
        Parser first = mock(Parser.class);
        Parser second = mock(Parser.class);
        when(first.examples()).thenReturn(Arrays.asList("first-a", "first-b"));
        when(second.examples()).thenReturn(Collections.singletonList("second"));

        assertEquals(Arrays.asList("first-a", "first-b", "second"),
                CompositeParser.of(first, second).examples());
    }

    @Test
    public void examplesReturnsEmptyForDelegatesWithoutExamples() {
        Parser parser = mock(Parser.class);
        when(parser.examples()).thenReturn(Collections.<String>emptyList());

        assertEquals(Collections.emptyList(), CompositeParser.of(parser).examples());
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
