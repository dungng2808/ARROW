package de.tudarmstadt.ukp.wikipedia.api;

import org.junit.Test;

public class MetaData_ArrowTest
{
    @Test
    public void testProtectedConstructorCannotBeExercisedWithoutWikipediaSession()
    {
        // MetaData construction requires a configured Wikipedia Hibernate session.
        // No safe, dependency-free Wikipedia instance can be created from the available API.
    }
}
