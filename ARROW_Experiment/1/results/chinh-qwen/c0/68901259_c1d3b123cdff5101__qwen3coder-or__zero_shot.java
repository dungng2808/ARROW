package com.ykushch.prjalgo2.task1;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class WordBreak_ArrowTest {

    @Test
    public void testBreakWordRecursively_NormalCase() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));
        String result = wordBreak.breakWordRecursively("catsanddog", dict);
        assertEquals("cats and dog", result);
    }

    @Test
    public void testBreakWordRecursively_EmptySentence() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));
        String result = wordBreak.breakWordRecursively("", dict);
        assertEquals("", result);
    }

    @Test
    public void testBreakWordRecursively_NullSentence() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));
        String result = wordBreak.breakWordRecursively(null, dict);
        assertEquals("", result);
    }

    @Test
    public void testBreakWordRecursively_NoMatch() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "dog"));
        String result = wordBreak.breakWordRecursively("catsanddog", dict);
        assertEquals("", result);
    }

    @Test
    public void testBreakWord_NormalCase() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));
        String result = wordBreak.breakWord("catsanddog", dict);
        assertEquals("cats and dog", result);
    }

    @Test
    public void testBreakWord_EmptySentence() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));
        String result = wordBreak.breakWord("", dict);
        assertEquals("", result);
    }

    @Test
    public void testBreakWordWithLargeDictionary_NormalCase() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));
        String result = wordBreak.breakWordWithLargeDictionary("catsanddog", dict);
        assertEquals("cats and dog", result);
    }

    @Test
    public void testBreakWordWithLargeDictionary_EmptySentence() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));
        String result = wordBreak.breakWordWithLargeDictionary("", dict);
        assertEquals("", result);
    }

    @Test
    public void testBreakWordWithLargeDictionary_NoMatch() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "dog"));
        String result = wordBreak.breakWordWithLargeDictionary("catsanddog", dict);
        assertEquals("", result);
    }

    @Test
    public void testBreakDownSentence_NormalCase() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));
        List<String> result = wordBreak.breakDownSentence("catsanddog", dict);
        assertEquals(2, result.size());
        assertTrue(result.contains("cats"));
        assertTrue(result.contains("dog"));
    }

    @Test
    public void testBreakDownSentence_EmptySentence() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));
        List<String> result = wordBreak.breakDownSentence("", dict);
        assertEquals(0, result.size());
    }

    @Test
    public void testIsSentenceValid_NullSentence() {
        WordBreak wordBreak = new WordBreak();
        boolean result = wordBreak.isSentenceValid(null);
        assertTrue(result);
    }

    @Test
    public void testIsSentenceValid_EmptyString() {
        WordBreak wordBreak = new WordBreak();
        boolean result = wordBreak.isSentenceValid("");
        assertTrue(result);
    }

    @Test
    public void testIsSentenceValid_NonEmptyString() {
        WordBreak wordBreak = new WordBreak();
        boolean result = wordBreak.isSentenceValid("test");
        assertFalse(result);
    }
}
