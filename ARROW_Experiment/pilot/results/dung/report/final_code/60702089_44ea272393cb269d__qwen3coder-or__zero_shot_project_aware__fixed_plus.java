package chapter1.finaltask;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class SubString_ArrowTest {

    @Test
    public void testContains_WhenSubstringExistsAtBeginning() {
        SubString subString = new SubString();
        String source = "hello world";
        String subStr = "hello";
        boolean result = subString.contains(source, subStr);
        assertThat(result, is(true));
    }

    @Test
    public void testContains_WhenSubstringExistsAtEnd() {
        SubString subString = new SubString();
        String source = "hello world";
        String subStr = "world";
        boolean result = subString.contains(source, subStr);
        assertThat(result, is(true));
    }

    @Test
    public void testContains_WhenSubstringExistsInMiddle() {
        SubString subString = new SubString();
        String source = "hello world";
        String subStr = "llo wo";
        boolean result = subString.contains(source, subStr);
        assertThat(result, is(true));
    }

    @Test
    public void testContains_WhenSubstringDoesNotExist() {
        SubString subString = new SubString();
        String source = "hello world";
        String subStr = "xyz";
        boolean result = subString.contains(source, subStr);
        assertThat(result, is(false));
    }

    @Test
    public void testContains_WhenSubstringIsEmpty() {
        SubString subString = new SubString();
        String source = "hello world";
        String subStr = "";
        boolean result = subString.contains(source, subStr);
        assertThat(result, is(true));
    }

    @Test
    public void testContains_WhenSourceStringIsEmpty() {
        SubString subString = new SubString();
        String source = "";
        String subStr = "hello";
        boolean result = subString.contains(source, subStr);
        assertThat(result, is(false));
    }

    @Test
    public void testContains_WhenBothStringsAreEmpty() {
        SubString subString = new SubString();
        String source = "";
        String subStr = "";
        boolean result = subString.contains(source, subStr);
        assertThat(result, is(true));
    }

    @Test
    public void testContains_WhenSubstringIsLongerThanSource() {
        SubString subString = new SubString();
        String source = "hi";
        String subStr = "high";
        boolean result = subString.contains(source, subStr);
        assertThat(result, is(false));
    }

    @Test
    public void testContains_WhenCharactersMatchPartiallyButNotSequentially() {
        SubString subString = new SubString();
        String source = "abcabc";
        String subStr = "bca";
        boolean result = subString.contains(source, subStr);
        assertThat(result, is(true));
    }

    @Test
    public void testContains_WhenSubstringIsIdenticalToSource() {
        SubString subString = new SubString();
        String source = "test";
        String subStr = "test";
        boolean result = subString.contains(source, subStr);
        assertThat(result, is(true));
    }

    @Test
    public void testContains_WhenSourceHasRepeatedCharacters() {
        SubString subString = new SubString();
        String source = "aabbcc";
        String subStr = "abb";
        boolean result = subString.contains(source, subStr);
        assertThat(result, is(false));
    }

    @Test
    public void testContains_WhenSourceAndSubstringHaveSameCharactersDifferentOrder() {
        SubString subString = new SubString();
        String source = "abc";
        String subStr = "bca";
        boolean result = subString.contains(source, subStr);
        assertThat(result, is(false));
    }
}
