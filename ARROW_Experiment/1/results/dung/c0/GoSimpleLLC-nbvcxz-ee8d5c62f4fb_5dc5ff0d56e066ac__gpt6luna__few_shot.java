package me.gosimple.nbvcxz.matching;

import me.gosimple.nbvcxz.matching.match.Match;
import me.gosimple.nbvcxz.resources.Configuration;
import me.gosimple.nbvcxz.resources.Dictionary;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DictionaryMatcher_ArrowTest
{
    @Test
    public void returnsNoMatchesForEmptyPassword()
    {
        Configuration configuration = new Configuration();
        DictionaryMatcher matcher = new DictionaryMatcher();

        List<Match> matches = matcher.match(configuration, "");

        assertTrue(matches.isEmpty());
    }

    @Test
    public void returnsNoMatchesWhenDictionaryDoesNotContainPassword()
    {
        Configuration configuration = new Configuration();
        DictionaryMatcher matcher = new DictionaryMatcher();

        List<Match> matches = matcher.match(configuration, "not-in-dictionary");

        assertTrue(matches.isEmpty());
    }

    @Test
    public void matchesDictionaryEntry()
    {
        Configuration configuration = new Configuration();
        DictionaryMatcher matcher = new DictionaryMatcher();
        Dictionary dictionary = new Dictionary();

        List<Match> matches = matcher.match(configuration, "password");

        assertEquals(0, matches.size());
    }
}
