package de.jcup.basheditor;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class SimpleWordCodeCompletion_ArrowTest {

	private SimpleWordCodeCompletion completion;

	@Before
	public void setUp() {
		completion = new SimpleWordCodeCompletion();
	}

	@Test
	public void test_add_null_word_does_not_change_cache() {
		int sizeBefore = completion.getWordListBuilder().build("test").size();
		completion.add(null);
		int sizeAfter = completion.getWordListBuilder().build("test").size();
		assertEquals(sizeBefore, sizeAfter);
	}

	@Test
	public void test_add_word_clears_all_words_cache() {
		completion.add("test");
		String source = "test1 test2";
		completion.calculate(source, 5);
		assertTrue(completion.getWordListBuilder().build(source).contains("test1"));
		completion.add("newword");
		assertFalse(completion.getWordListBuilder().build(source).contains("test1"));
	}

	@Test
	public void test_calculate_offset_zero_returns_all_words() {
		completion.add("word1");
		completion.add("word2");
		String source = "some text";
		Set<String> result = completion.calculate(source, 0);
		assertTrue(result.contains("word1"));
		assertTrue(result.contains("word2"));
	}

	@Test
	public void test_calculate_with_text_before_returns_filtered_words() {
		completion.add("word1");
		completion.add("word2");
		completion.add("word3");
		String source = "word1 word2 word3";
		Set<String> result = completion.calculate(source, 5);
		assertTrue(result.contains("word1"));
		assertTrue(result.contains("word2"));
		assertFalse(result.contains("word3"));
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
	public void test_getTextbefore_offset_larger_than_source_length_returns_empty_string() {
		String result = completion.getTextbefore("test", 10);
		assertEquals("", result);
	}

	@Test
	public void test_getTextbefore_normal_case_returns_correct_text() {
		String result = completion.getTextbefore("hello world", 7);
		assertEquals("world", result);
	}

	@Test
	public void test_reset_clears_both_caches() {
		completion.add("test");
		String source = "test1 test2";
		completion.calculate(source, 5);
		assertFalse(completion.getWordListBuilder().build(source).isEmpty());
		completion.reset();
		assertTrue(completion.getWordListBuilder().build(source).isEmpty());
	}

	@Test
	public void test_filter_with_null_wanted_returns_all_words() {
		Set<String> allWords = new java.util.TreeSet<>();
		allWords.add("word1");
		allWords.add("word2");
		Set<String> result = completion.filter(allWords, null);
		assertEquals(allWords, result);
	}

	@Test
	public void test_filter_with_empty_wanted_returns_all_words() {
		Set<String> allWords = new java.util.TreeSet<>();
		allWords.add("word1");
		allWords.add("word2");
		Set<String> result = completion.filter(allWords, "");
		assertEquals(allWords, result);
	}

	@Test
	public void test_filter_with_matching_prefix_returns_filtered_words() {
		Set<String> allWords = new java.util.TreeSet<>();
		allWords.add("word1");
		allWords.add("word2");
		allWords.add("word3");
		Set<String> result = completion.filter(allWords, "wor");
		assertTrue(result.contains("word1"));
		assertTrue(result.contains("word2"));
		assertTrue(result.contains("word3"));
	}

	@Test
	public void test_filter_removes_exact_match() {
		Set<String> allWords = new java.util.TreeSet<>();
		allWords.add("word1");
		allWords.add("word2");
		allWords.add("word");
		Set<String> result = completion.filter(allWords, "word");
		assertFalse(result.contains("word"));
		assertTrue(result.contains("word1"));
		assertTrue(result.contains("word2"));
	}

	@Test
	public void test_rebuildCacheIfNecessary_with_empty_cache_builds_from_additional_words() {
		completion.add("test");
		completion.rebuildCacheIfNecessary("source");
		assertTrue(completion.getWordListBuilder().build("source").contains("test"));
	}

	@Test
	public void test_rebuildCacheIfNecessary_with_non_empty_cache_does_not_rebuild() {
		completion.add("test");
		completion.rebuildCacheIfNecessary("source");
		String source = "source";
		Set<String> words = completion.getWordListBuilder().build(source);
		assertTrue(words.contains("test"));
	}
}
