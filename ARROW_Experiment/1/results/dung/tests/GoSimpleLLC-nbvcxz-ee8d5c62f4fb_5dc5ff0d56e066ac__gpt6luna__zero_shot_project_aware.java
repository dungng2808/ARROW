package me.gosimple.nbvcxz.matching;

import me.gosimple.nbvcxz.matching.match.Match;
import me.gosimple.nbvcxz.resources.Configuration;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DictionaryMatcher_ArrowTest
{
    @Test
    public void matchReturnsEmptyListForEmptyPassword()
    {
        DictionaryMatcher matcher = new DictionaryMatcher();
        Configuration configuration = new Configuration();

        List<Match> matches = matcher.match(configuration, "");

        assertNotNull(matches);
        assertTrue(matches.isEmpty());
    }
}
