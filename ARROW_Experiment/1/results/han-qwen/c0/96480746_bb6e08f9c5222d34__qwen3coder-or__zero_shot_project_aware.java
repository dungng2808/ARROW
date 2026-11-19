package de.jcup.basheditor;

import static org.junit.Assert.*;
import static java.util.Collections.*;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

public class SimpleWordCodeCompletion_ArrowTest {

    private SimpleWordCodeCompletion completion;

    @Before
    public void setup() {
        completion = new SimpleWordCodeCompletion();
    }

    @Test
    public void add_null_word_is_ignored() {
        completion.add(null);
        assertEquals(0, completion.calculate("test", 1).size());
    }

    @Test
    public void add_empty_string_word_is_added() {
        completion.add("");
        assertEquals(1, completion.calculate("test", 1).size());
    }

    @Test
    public void add_normal_word_is_added() {
        completion.add("test");
        assertEquals(1, completion.calculate("test", 1).size());
        assertTrue(completion.calculate("test", 1).contains("test"));
    }

    @Test
    public void add_two_words_are_added() {
        completion.add("test");
        completion.add("hello");
        Set<String> result = completion.calculate("test", 1);
        assertEquals(2, result.size());
        assertTrue(result.contains("test"));
        assertTrue(result.contains("hello"));
    }

    @Test
    public void add_same_word_twice_only_once_in_result() {
        completion.add("test");
        completion.add("test");
        Set<String> result = completion.calculate("test", 1);
        assertEquals(1, result.size());
        assertTrue(result.contains("test"));
    }

    @Test
    public void add_word_then_reset_clears_all_words() {
        completion.add("test");
        completion.reset();
        assertEquals(0, completion.calculate("test", 1).size());
    }

    @Test
    public void calculate_with_offset_zero_returns_all_words() {
        completion.add("test");
        completion.add("hello");
        Set<String> result = completion.calculate("some text", 0);
        assertEquals(2, result.size());
        assertTrue(result.contains("test"));
        assertTrue(result.contains("hello"));
    }

    @Test
    public void calculate_with_offset_zero_returns_unmodifiable_set() {
        completion.add("test");
        Set<String> result = completion.calculate("some text", 0);
        try {
            result.add("another");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void calculate_with_offset_greater_than_zero_filters_by_prefix() {
        completion.add("test");
        completion.add("testing");
        completion.add("hello");
        Set<String> result = completion.calculate("te", 2);
        assertEquals(2, result.size());
        assertTrue(result.contains("test"));
        assertTrue(result.contains("testing"));
    }

    @Test
    public void calculate_with_offset_greater_than_zero_filters_by_prefix_and_removes_exact_match() {
        completion.add("test");
        completion.add("testing");
        completion.add("hello");
        Set<String> result = completion.calculate("test", 4);
        assertEquals(1, result.size());
        assertTrue(result.contains("testing"));
    }

    @Test
    public void calculate_with_offset_greater_than_zero_filters_by_substring_when_no_prefix_match() {
        completion.add("test");
        completion.add("testing");
        completion.add("hello");
        Set<String> result = completion.calculate("es", 2);
        assertEquals(1, result.size());
        assertTrue(result.contains("test"));
    }

    @Test
    public void getTextbefore_null_source_returns_empty_string() {
        assertEquals("", completion.getTextbefore(null, 1));
    }

    @Test
    public void getTextbefore_empty_source_returns_empty_string() {
        assertEquals("", completion.getTextbefore("", 1));
    }

    @Test
    public void getTextbefore_offset_zero_returns_empty_string() {
        assertEquals("", completion.getTextbefore("test", 0));
    }

    @Test
    public void getTextbefore_negative_offset_returns_empty_string() {
        assertEquals("", completion.getTextbefore("test", -1));
    }

    @Test
    public void getTextbefore_offset_bigger_than_source_length_returns_empty_string() {
        assertEquals("", completion.getTextbefore("test", 10));
    }

    @Test
    public void getTextbefore_offset_at_end_of_source_returns_last_character() {
        assertEquals("t", completion.getTextbefore("test", 4));
    }

    @Test
    public void getTextbefore_offset_in_middle_returns_word_before_offset() {
        assertEquals("tes", completion.getTextbefore("test", 3));
    }

    @Test
    public void getTextbefore_offset_at_beginning_returns_empty_string() {
        assertEquals("", completion.getTextbefore("test", 1));
    }

    @Test
    public void getTextbefore_offset_after_whitespace_returns_word_before_whitespace() {
        assertEquals("test", completion.getTextbefore("   test", 6));
    }

    @Test
    public void getTextbefore_offset_at_whitespace_returns_empty_string() {
        assertEquals("", completion.getTextbefore("   test", 3));
    }

    @Test
    public void filter_with_null_wanted_returns_all_words() {
        SortedSet<String> allWords = new TreeSet<>();
        allWords.add("test");
        allWords.add("hello");
        Set<String> result = completion.filter(allWords, null);
        assertEquals(2, result.size());
        assertTrue(result.contains("test"));
        assertTrue(result.contains("hello"));
    }

    @Test
    public void filter_with_empty_wanted_returns_all_words() {
        SortedSet<String> allWords = new TreeSet<>();
        allWords.add("test");
        allWords.add("hello");
        Set<String> result = completion.filter(allWords, "");
        assertEquals(2, result.size());
        assertTrue(result.contains("test"));
        assertTrue(result.contains("hello"));
    }

    @Test
    public void filter_with_wanted_prefix_returns_matching_words() {
        SortedSet<String> allWords = new TreeSet<>();
        allWords.add("test");
        allWords.add("testing");
        allWords.add("hello");
        Set<String> result = completion.filter(allWords, "test");
        assertEquals(2, result.size());
        assertTrue(result.contains("test"));
        assertTrue(result.contains("testing"));
    }

    @Test
    public void filter_with_wanted_prefix_removes_exact_match() {
        SortedSet<String> allWords = new TreeSet<>();
        allWords.add("test");
        allWords.add("testing");
        allWords.add("hello");
        Set<String> result = completion.filter(allWords, "test");
        assertFalse(result.contains("test"));
        assertTrue(result.contains("testing"));
    }

    @Test
    public void filter_with_wanted_substring_returns_matching_words() {
        SortedSet<String> allWords = new TreeSet<>();
        allWords.add("test");
        allWords.add("testing");
        allWords.add("hello");
        Set<String> result = completion.filter(allWords, "es");
        assertEquals(1, result.size());
        assertTrue(result.contains("test"));
    }

    @Test
    public void filter_with_wanted_substring_returns_matching_words_in_correct_order() {
        SortedSet<String> allWords = new TreeSet<>();
        allWords.add("test");
        allWords.add("testing");
        allWords.add("hello");
        allWords.add("best");
        Set<String> result = completion.filter(allWords, "es");
        assertEquals(2, result.size());
        LinkedHashSet<String> orderedResult = new LinkedHashSet<>(result);
        assertEquals("test", orderedResult.iterator().next());
    }

    @Test
    public void rebuildCacheIfNecessary_with_empty_cache_builds_from_additional_words_and_source() {
        completion.add("test");
        completion.setWordListBuilder(new WordListBuilder() {
            @Override
            public Set<String> build(String source) {
                Set<String> result = new HashSet<>();
                result.add("source");
                return result;
            }
        });
        Set<String> result = completion.calculate("any", 1);
        assertEquals(2, result.size());
        assertTrue(result.contains("test"));
        assertTrue(result.contains("source"));
    }

    @Test
    public void rebuildCacheIfNecessary_with_non_empty_cache_does_not_rebuild() {
        completion.add("test");
        completion.setWordListBuilder(new WordListBuilder() {
            @Override
            public Set<String> build(String source) {
                Set<String> result = new HashSet<>();
                result.add("source");
                return result;
            }
        });
        completion.calculate("any", 1); // First call builds cache
        completion.add("new"); // This should trigger cache invalidation
        Set<String> result = completion.calculate("any", 1); // Second call should rebuild
        assertEquals(2, result.size());
        assertTrue(result.contains("test"));
        assertTrue(result.contains("new"));
    }

    @Test
    public void getWordListBuilder_returns_default_builder_if_not_set() {
        assertNotNull(completion.getWordListBuilder());
        assertTrue(completion.getWordListBuilder() instanceof SimpleWordListBuilder);
    }

    @Test
    public void setWordListBuilder_sets_custom_builder() {
        WordListBuilder customBuilder = new WordListBuilder() {
            @Override
            public Set<String> build(String source) {
                Set<String> result = new HashSet<>();
                result.add("custom");
                return result;
            }
        };
        completion.setWordListBuilder(customBuilder);
        assertEquals("custom", completion.calculate("any", 1).iterator().next());
    }

    @Test
    public void reset_clears_both_caches() {
        completion.add("test");
        completion.setWordListBuilder(new WordListBuilder() {
            @Override
            public Set<String> build(String source) {
                Set<String> result = new HashSet<>();
                result.add("source");
                return result;
            }
        });
        completion.calculate("any", 1);
        completion.reset();
        assertEquals(0, completion.calculate("any", 1).size());
    }
}
