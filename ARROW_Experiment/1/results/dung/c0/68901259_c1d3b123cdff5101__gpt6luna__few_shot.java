package com.ykushch.prjalgo2.task1;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WordBreak_ArrowTest {

    @Test
    public void breakWordRecursivelyBreaksSentenceIntoDictionaryWords() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));

        assertEquals("cats and dog", wordBreak.breakWordRecursively("catsanddog", dict));
    }

    @Test
    public void breakWordRecursivelyReturnsEmptyStringWhenSentenceCannotBeBroken() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "dog"));

        assertEquals("", wordBreak.breakWordRecursively("cats", dict));
    }

    @Test
    public void breakWordRecursivelyReturnsEmptyStringForEmptySentence() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "dog"));

        assertEquals("", wordBreak.breakWordRecursively("", dict));
    }

    @Test
    public void breakWordReturnsWordsFoundByDynamicProgramming() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));

        assertEquals("cats sand dog", wordBreak.breakWord("catsanddog", dict));
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
    public void breakWordWithLargeDictionaryReturnsEmptyStringWhenSentenceCannotBeBroken() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "dog"));

        assertEquals("", wordBreak.breakWordWithLargeDictionary("cats", dict));
    }

    @Test
    public void breakWordWithLargeDictionaryReturnsEmptyStringForEmptySentence() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "dog"));

        assertEquals("", wordBreak.breakWordWithLargeDictionary("", dict));
    }
}
