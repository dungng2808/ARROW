package de.jcup.basheditor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

public class SimpleWordCodeCompletion_ArrowTest {

    @Test
    public void calculateReturnsWordsFromBuilderAtOffsetZero() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        completion.setWordListBuilder(source -> new LinkedHashSet<>(Arrays.asList("alpha", "beta", "")));

        Set<String> result = completion.calculate("", 0);

        assertEquals(new TreeSet<>(Arrays.asList("alpha", "beta")), result);
    }

    @Test
    public void calculateFiltersWordsByPrefixAndThenSubstringCaseInsensitively() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        completion.setWordListBuilder(source ->
                new LinkedHashSet<>(Arrays.asList("Alpha", "alphabet", "xAlpha", "beta")));

        Set<String> result = completion.calculate("al", 2);

        assertEquals(new LinkedHashSet<>(Arrays.asList("Alpha", "alphabet", "xAlpha")), result);
    }

    @Test
    public void calculateExcludesTheExactWantedWord() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        completion.setWordListBuilder(source ->
                new LinkedHashSet<>(Arrays.asList("cat", "catalog", "bobcat")));

        Set<String> result = completion.calculate("cat", 3);

        assertEquals(new LinkedHashSet<>(Arrays.asList("catalog", "bobcat")), result);
    }

    @Test
    public void addTrimsWordsAndIgnoresNull() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        completion.add("  custom  ");
        completion.add(null);
        completion.setWordListBuilder(source -> Collections.<String>emptySet());

        assertEquals(Collections.singleton("custom"), completion.calculate("", 0));
    }

    @Test
    public void resetClearsAdditionalWords() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        completion.add("custom");
        completion.setWordListBuilder(source -> Collections.<String>emptySet());
        completion.reset();

        assertTrue(completion.calculate("", 0).isEmpty());
    }

    @Test
    public void getTextbeforeReturnsNonWhitespaceTextImmediatelyBeforeOffset() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();

        assertEquals("word", completion.getTextbefore("first word rest", 10));
    }

    @Test
    public void getTextbeforeReturnsEmptyForNullEmptyAndInvalidOffsets() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();

        assertEquals("", completion.getTextbefore(null, 1));
        assertEquals("", completion.getTextbefore("", 1));
        assertEquals("", completion.getTextbefore("word", 0));
        assertEquals("", completion.getTextbefore("word", 5));
    }

    @Test
    public void getWordListBuilderCreatesAndReusesDefaultBuilder() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();

        WordListBuilder first = completion.getWordListBuilder();
        WordListBuilder second = completion.getWordListBuilder();

        assertTrue(first instanceof SimpleWordListBuilder);
        assertTrue(first == second);
    }

    @Test
    public void calculateReturnsAnUnmodifiableSetAtOffsetZero() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        completion.setWordListBuilder(source -> Collections.singleton("alpha"));
        Set<String> result = completion.calculate("", 0);

        try {
            result.add("beta");
            assertFalse(true);
        } catch (UnsupportedOperationException expected) {
            assertEquals(Collections.singleton("alpha"), result);
        }
    }
}
