package com.ykushch.prjalgo2.task1;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WordBreak_ArrowTest {

    @Test
    public void breakWordRecursivelyBreaksSentenceIntoWords() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "sand", "dog", "cats", "and"));

        assertEquals("cats and dog", wordBreak.breakWordRecursively("catsanddog", dict));
    }

    @Test
    public void breakWordRecursivelyReturnsEmptyStringForEmptySentence() {
        WordBreak wordBreak = new WordBreak();

        assertEquals("", wordBreak.breakWordRecursively("", Collections.<String>emptySet()));
    }

    @Test
    public void breakWordRecursivelyReturnsEmptyStringWhenSentenceCannotBeBroken() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "dog"));

        assertEquals("", wordBreak.breakWordRecursively("cats", dict));
    }

    @Test
    public void breakWordUsesDynamicProgrammingToBreakSentence() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "sand", "dog", "cats", "and"));

        assertEquals("cats and dog", wordBreak.breakWord("catsanddog", dict));
    }

    @Test
    public void breakWordReturnsEmptyStringForEmptySentence() {
        WordBreak wordBreak = new WordBreak();

        assertEquals("", wordBreak.breakWord("", Collections.<String>emptySet()));
    }

    @Test
    public void breakWordReturnsEmptyStringWhenSentenceCannotBeBroken() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "dog"));

        assertEquals("", wordBreak.breakWord("cats", dict));
    }

    @Test
    public void breakWordWithLargeDictionaryBreaksSentenceIntoWords() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("a", "aa", "aaa", "aaaa", "aaaaa"));

        assertEquals("a a a", wordBreak.breakWordWithLargeDictionary("aaa", dict));
    }

    @Test
    public void breakWordWithLargeDictionaryReturnsEmptyStringForEmptySentence() {
        WordBreak wordBreak = new WordBreak();

        assertEquals("", wordBreak.breakWordWithLargeDictionary("", Collections.<String>emptySet()));
    }

    @Test
    public void breakWordWithLargeDictionaryReturnsEmptyStringWhenSentenceCannotBeBroken() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "dog"));

        assertEquals("", wordBreak.breakWordWithLargeDictionary("cats", dict));
    }
}
