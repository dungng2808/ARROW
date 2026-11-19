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
    public void testBreakWordRecursively_NoValidBreak() {
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
    public void testBreakWord_NoValidBreak() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "dog"));
        String result = wordBreak.breakWord("catsanddog", dict);
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
    public void testBreakWordWithLargeDictionary_NoValidBreak() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "dog"));
        String result = wordBreak.breakWordWithLargeDictionary("catsanddog", dict);
        assertEquals("", result);
    }

    @Test
    public void testBreakWordWithLargeDictionary_SingleCharacter() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("a"));
        String result = wordBreak.breakWordWithLargeDictionary("a", dict);
        assertEquals("a", result);
    }
}
