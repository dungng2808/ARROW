package de.jcup.basheditor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

public class SimpleWordCodeCompletion_ArrowTest {

    @Test
    public void getTextBeforeReturnsNonWhitespaceSuffix() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();

        assertEquals("world", completion.getTextbefore("hello world", 11));
    }

    @Test
    public void getTextBeforeReturnsEmptyForNullOrEmptySource() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();

        assertEquals("", completion.getTextbefore(null, 3));
        assertEquals("", completion.getTextbefore("", 0));
    }

    @Test
    public void getTextBeforeReturnsEmptyForInvalidOffsets() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();

        assertEquals("", completion.getTextbefore("word", 0));
        assertEquals("", completion.getTextbefore("word", -1));
        assertEquals("", completion.getTextbefore("word", 5));
    }

    @Test
    public void addAndCalculateIncludesAdditionalWordsAndFiltersByPrefix() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        completion.add(" apple ");
        completion.add("apricot");
        completion.add(null);

        Set<String> result = completion.calculate("unused", 2);

        assertEquals(new HashSet<String>(Arrays.asList("apple", "apricot")), result);
    }

    @Test
    public void calculateReturnsAllWordsWhenOffsetIsZero() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        completion.add("banana");
        completion.add("apple");

        Set<String> result = completion.calculate("", 0);

        assertEquals(new TreeSet<String>(Arrays.asList("apple", "banana")), result);
    }

    @Test
    public void calculateFiltersCaseInsensitivelyAndPlacesSubstringMatchesAfterPrefixes() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        completion.add("alphabet");
        completion.add("ALPINE");
        completion.add("palp");

        Set<String> result = completion.calculate("alp", 3);

        assertEquals(Arrays.asList("ALPINE", "alphabet", "palp"),
                new java.util.ArrayList<String>(result));
    }

    @Test
    public void calculateRemovesExactWantedWord() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        completion.add("cat");
        completion.add("catalog");

        assertEquals(new HashSet<String>(Arrays.asList("catalog")),
                completion.calculate("cat", 3));
    }

    @Test
    public void resetClearsAdditionalWordsAndReturnsSameInstance() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        completion.add("temporary");
        completion.calculate("", 0);

        assertSame(completion, completion.reset());
        assertEquals(new HashSet<String>(), completion.calculate("", 0));
    }

    @Test
    public void getWordListBuilderLazilyCreatesBuilder() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();

        assertNotNull(completion.getWordListBuilder());
        assertSame(completion.getWordListBuilder(), completion.getWordListBuilder());
    }

    @Test
    public void setWordListBuilderUsesProvidedBuilder() {
        SimpleWordCodeCompletion completion = new SimpleWordCodeCompletion();
        WordListBuilder builder = completion.getWordListBuilder();

        completion.setWordListBuilder(builder);

        assertSame(builder, completion.getWordListBuilder());
    }
}
