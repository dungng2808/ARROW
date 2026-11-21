package com.ykushch.prjalgo2.task1;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class WordBreak_ArrowTest {

    @Test
    public void testBreakWordRecursively_NormalCase() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));
        String result = wordBreak.breakWordRecursively("catsanddog", dict);
        assertThat(result).isEqualTo("cats and dog");
    }

    @Test
    public void testBreakWordRecursively_EmptySentence() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));
        String result = wordBreak.breakWordRecursively("", dict);
        assertThat(result).isEqualTo("");
    }

    @Test
    public void testBreakWordRecursively_NullSentence() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));
        String result = wordBreak.breakWordRecursively(null, dict);
        assertThat(result).isEqualTo("");
    }

    @Test
    public void testBreakWordRecursively_NoMatch() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "dog"));
        String result = wordBreak.breakWordRecursively("catsanddog", dict);
        assertThat(result).isEqualTo("");
    }

    @Test
    public void testBreakWord_NormalCase() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));
        String result = wordBreak.breakWord("catsanddog", dict);
        assertThat(result).isEqualTo("cats and dog");
    }

    @Test
    public void testBreakWord_EmptySentence() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));
        String result = wordBreak.breakWord("", dict);
        assertThat(result).isEqualTo("");
    }

    @Test
    public void testBreakWordWithLargeDictionary_NormalCase() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));
        String result = wordBreak.breakWordWithLargeDictionary("catsanddog", dict);
        assertThat(result).isEqualTo("cats and dog");
    }

    @Test
    public void testBreakWordWithLargeDictionary_EmptySentence() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));
        String result = wordBreak.breakWordWithLargeDictionary("", dict);
        assertThat(result).isEqualTo("");
    }

    @Test
    public void testBreakWordWithLargeDictionary_NoMatch() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "dog"));
        String result = wordBreak.breakWordWithLargeDictionary("catsanddog", dict);
        assertThat(result).isEqualTo("");
    }

    @Test
    public void testBreakWordWithLargeDictionary_NullSentence() {
        WordBreak wordBreak = new WordBreak();
        Set<String> dict = new HashSet<>(Arrays.asList("cat", "cats", "and", "sand", "dog"));
        String result = wordBreak.breakWordWithLargeDictionary(null, dict);
        assertThat(result).isEqualTo("");
    }
}
