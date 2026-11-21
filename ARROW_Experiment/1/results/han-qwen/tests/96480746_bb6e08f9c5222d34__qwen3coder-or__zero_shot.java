package de.jcup.basheditor;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class SimpleWordCodeCompletion_ArrowTest {

    private SimpleWordCodeCompletion completion;

    @Before
    public void before() {
        completion = new SimpleWordCodeCompletion();
    }

    @Test
    public void test_add_null_does_not_crash() {
        completion.add(null);
        // should not crash
    }

    @Test
    public void test_add_word_adds_to_cache() {
        completion.add("test");
        Set<String> result = completion.calculate("test", 0);
        assertTrue(result.contains("test"));
    }

    @Test
    public void test_calculate_empty_source_returns_all_words() {
        completion.add("hello");
        completion.add("world");
        Set<String> result = completion.calculate("", 0);
        assertEquals(2, result.size());
        assertTrue(result.contains("hello"));
        assertTrue(result.contains("world"));
    }

    @Test
    public void test_calculate_offset_zero_returns_all_words() {
        completion.add("foo");
        completion.add("bar");
        Set<String> result = completion.calculate("some text", 0);
        assertEquals(2, result.size());
        assertTrue(result.contains("foo"));
        assertTrue(result.contains("bar"));
    }

    @Test
    public void test_calculate_with_text_before_returns_filtered_words() {
        completion.add("hello");
        completion.add("world");
        completion.add("help");
        Set<String> result = completion.calculate("h", 1);
        assertEquals(2, result.size());
        assertTrue(result.contains("hello"));
        assertTrue(result.contains("help"));
    }

    @Test
    public void test_getTextbefore_null_source_returns_empty_string() {
        String result = completion.getTextbefore(null, 5);
        assertEquals("", result);
    }

    @Test
    public void test_getTextbefore_empty_source_returns_empty_string() {
        String result = completion.getTextbefore("", 5);
        assertEquals("", result);
    }

    @Test
    public void test_getTextbefore_negative_offset_returns_empty_string() {
        String result = completion.getTextbefore("test", -1);
        assertEquals("", result);
    }

    @Test
    public void test_getTextbefore_zero_offset_returns_empty_string() {
        String result = completion.getTextbefore("test", 0);
        assertEquals("", result);
    }

    @Test
    public void test_getTextbefore_offset_bigger_than_source_length_returns_empty_string() {
        String result = completion.getTextbefore("test", 10);
        assertEquals("", result);
    }

    @Test
    public void test_getTextbefore_normal_case_returns_correct_text() {
        String result = completion.getTextbefore("hello world", 6);
        assertEquals("world", result);
    }

    @Test
    public void test_reset_clears_caches() {
        completion.add("test");
        completion.calculate("test", 0);
        completion.reset();
        Set<String> result = completion.calculate("test", 0);
        assertFalse(result.contains("test"));
    }

    @Test
    public void test_filter_with_null_wanted_returns_all_words() {
        Set<String> allWords = new java.util.TreeSet<>();
        allWords.add("hello");
        allWords.add("world");
        Set<String> result = completion.filter(allWords, null);
        assertEquals(2, result.size());
        assertTrue(result.contains("hello"));
        assertTrue(result.contains("world"));
    }

    @Test
    public void test_filter_with_empty_wanted_returns_all_words() {
        Set<String> allWords = new java.util.TreeSet<>();
        allWords.add("hello");
        allWords.add("world");
        Set<String> result = completion.filter(allWords, "");
        assertEquals(2, result.size());
        assertTrue(result.contains("hello"));
        assertTrue(result.contains("world"));
    }

    @Test
    public void test_filter_with_matching_prefix_returns_matching_words() {
        Set<String> allWords = new java.util.TreeSet<>();
        allWords.add("hello");
        allWords.add("world");
        allWords.add("help");
        Set<String> result = completion.filter(allWords, "he");
        assertEquals(2, result.size());
        assertTrue(result.contains("hello"));
        assertTrue(result.contains("help"));
    }

    @Test
    public void test_filter_removes_exact_match() {
        Set<String> allWords = new java.util.TreeSet<>();
        allWords.add("hello");
        allWords.add("world");
        Set<String> result = completion.filter(allWords, "hello");
        assertFalse(result.contains("hello"));
        assertEquals(1, result.size());
        assertTrue(result.contains("world"));
    }

    @Test
    public void test_rebuildCacheIfNecessary_with_empty_cache_builds_from_source_and_additional_words() {
        completion.add("additional");
        Set<String> result = completion.calculate("source with additional", 0);
        assertTrue(result.contains("additional"));
    }
}
