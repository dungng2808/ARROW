package de.jcup.basheditor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

public class SimpleWordCodeCompletion_ArrowTest {

    @Test
    public void getTextbeforeReturnsNonWhitespaceTextImmediatelyBeforeOffset() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();

        assertEquals("word", completion.getTextbefore("first word rest", 10));
    }

    @Test
    public void getTextbeforeReturnsEmptyForNullOrEmptySource() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();

        assertEquals("", completion.getTextbefore(null, 3));
        assertEquals("", completion.getTextbefore("", 3));
    }

    @Test
    public void getTextbeforeReturnsEmptyForNonPositiveOrTooLargeOffset() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();

        assertEquals("", completion.getTextbefore("word", 0));
        assertEquals("", completion.getTextbefore("word", -1));
        assertEquals("", completion.getTextbefore("word", 5));
    }

    @Test
    public void getTextbeforeReturnsEmptyWhenOffsetFollowsWhitespace() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();

        assertEquals("", completion.getTextbefore("word next", 5));
    }

    @Test
    public void calculateReturnsSortedWordsWhenOffsetIsZero() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        completion.setWordListBuilder(source -> new TreeSet<String>(Arrays.asList("beta", "alpha", "")));

        Set<String> result = completion.calculate("source", 0);

        assertEquals(new TreeSet<String>(Arrays.asList("alpha", "beta")), result);
    }

    @Test
    public void calculateFiltersWordsByPrefixAndThenSubstringCaseInsensitively() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        completion.setWordListBuilder(source ->
                new TreeSet<String>(Arrays.asList("alphabet", "betaalpha", "ALPINE", "gamma")));

        Set<String> result = completion.calculate("alp", 3);

        assertEquals(Arrays.asList("ALPINE", "alphabet", "betaalpha"),
                Arrays.asList(result.toArray(new String[result.size()])));
    }

    @Test
    public void calculateExcludesTheExactWantedWord() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        completion.setWordListBuilder(source ->
                new TreeSet<String>(Arrays.asList("cat", "catalog", "bobcat")));

        Set<String> result = completion.calculate("cat", 3);

        assertEquals(new java.util.LinkedHashSet<String>(Arrays.asList("catalog", "bobcat")), result);
    }

    @Test
    public void calculateWithEmptyWantedTextReturnsAllWords() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        completion.setWordListBuilder(source -> new TreeSet<String>(Arrays.asList("beta", "alpha")));

        Set<String> result = completion.calculate(" ", 1);

        assertEquals(new TreeSet<String>(Arrays.asList("alpha", "beta")), result);
    }

    @Test
    public void addTrimsWordsAndIgnoresNull() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        completion.add(null);
        completion.add("  custom  ");
        completion.setWordListBuilder(source -> Collections.<String>emptySet());

        assertEquals(Collections.singleton("custom"), completion.calculate("source", 0));
    }

    @Test
    public void resetClearsAddedWordsAndReturnsSameInstance() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        completion.add("custom");
        completion.setWordListBuilder(source -> Collections.<String>emptySet());

        completion.calculate("source", 0);
        assertSame(completion, completion.reset());
        assertTrue(completion.calculate("source", 0).isEmpty());
    }

    @Test
    public void getWordListBuilderLazilyCreatesBuilder() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();

        WordListBuilder builder = completion.getWordListBuilder();

        assertNotNull(builder);
        assertSame(builder, completion.getWordListBuilder());
    }

    @Test
    public void calculateReturnsUnmodifiableSetAtZeroOffset() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        completion.setWordListBuilder(source -> new TreeSet<String>(Collections.singleton("word")));

        Set<String> result = completion.calculate("source", 0);

        try {
            result.add("other");
        } catch (UnsupportedOperationException expected) {
            return;
        }
        throw new AssertionError("Expected an unmodifiable set");
    }

    @Test
    public void filterReturnsAllWordsForNullOrEmptyWantedText() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        SortedSet<String> words = new TreeSet<String>(Arrays.asList("alpha", "beta"));

        assertSame(words, completion.filter(words, null));
        assertSame(words, completion.filter(words, ""));
    }
}
