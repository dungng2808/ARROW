package com.ykushch.prjalgo2.task1;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class WordBreak_ArrowTest {

    @Test
    public void breakWordRecursivelyBreaksSentenceIntoWords() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dictionary = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));

        assertEquals("cats and dog", wordBreak.breakWordRecursively("catsanddog", dictionary));
    }

    @Test
    public void breakWordRecursivelyReturnsEmptyStringForEmptySentence() {
        WordBreak wordBreak = new WordBreak();

        assertEquals("", wordBreak.breakWordRecursively("", Collections.<String>emptySet()));
    }

    @Test
    public void breakWordRecursivelyReturnsEmptyStringWhenSentenceCannotBeBroken() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dictionary = new HashSet<>(Collections.singletonList("cat"));

        assertEquals("", wordBreak.breakWordRecursively("dog", dictionary));
    }

    @Test
    public void breakWordUsesDynamicProgrammingToBreakSentence() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dictionary = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));

        assertEquals("cats and dog", wordBreak.breakWord("catsanddog", dictionary));
    }

    @Test
    public void breakWordReturnsEmptyStringForEmptySentence() {
        WordBreak wordBreak = new WordBreak();

        assertEquals("", wordBreak.breakWord("", Collections.<String>emptySet()));
    }

    @Test
    public void breakWordReturnsWordsReachableFromSentenceStart() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dictionary = new HashSet<>(Arrays.asList("cat", "cats", "sand"));

        assertEquals("cat cats sand", wordBreak.breakWord("catsand", dictionary));
    }

    @Test
    public void breakWordWithLargeDictionaryBreaksSentenceIntoWords() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dictionary = new HashSet<>(Arrays.asList("a", "aa", "aaa", "aaaa", "aaaaa"));

        assertEquals("a a a", wordBreak.breakWordWithLargeDictionary("aaa", dictionary));
    }

    @Test
    public void breakWordWithLargeDictionaryReturnsEmptyStringForEmptySentence() {
        WordBreak wordBreak = new WordBreak();

        assertEquals("", wordBreak.breakWordWithLargeDictionary("", Collections.<String>emptySet()));
    }

    @Test
    public void breakWordWithLargeDictionaryReturnsEmptyStringWhenSentenceCannotBeBroken() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dictionary = new HashSet<>(Collections.singletonList("cat"));

        assertEquals("", wordBreak.breakWordWithLargeDictionary("dog", dictionary));
    }
}
