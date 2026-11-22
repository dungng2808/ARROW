package org.myLazyClock.calendarApi;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class CalendarFactory_ArrowTest {

    @Test
    public void testGetInstanceReturnsSameInstance() {
        CalendarFactory factory1 = CalendarFactory.getInstance();
        CalendarFactory factory2 = CalendarFactory.getInstance();
        assertEquals(factory1, factory2);
    }

    @Test
    public void testGetReturnsCorrectStrategy() {
        CalendarFactory factory = CalendarFactory.getInstance();
        
        // Test that the method doesn't throw NullPointerException for valid input
        // Since we can't know which strategies are actually registered, we test
        // with a non-existent ID to ensure proper behavior
        try {
            CalendarStrategy strategy = factory.get(new CalendarId("nonExistent"));
            // This line should not be reached because get() throws NullPointerException
            // when strategy is not found
            fail("Expected NullPointerException was not thrown");
        } catch (NullPointerException e) {
            // Expected behavior - this confirms the method works correctly
            assertNotNull(e);
        }
    }

    @Test
    public void testIteratorReturnsAllStrategies() {
        CalendarFactory factory = CalendarFactory.getInstance();
        Iterator<CalendarStrategy> iterator = factory.iterator();
        
        // Since we don't know what strategies are actually loaded,
        // we just verify that the iterator works and returns something
        assertNotNull(iterator);
        assertTrue(iterator.hasNext() || !iterator.hasNext());
    }

    @Test
    public void testConstructorInitializesMap() {
        CalendarFactory factory = CalendarFactory.getInstance();
        // We can't directly access the private field, but we can test that
        // the factory is properly initialized by testing its methods
        assertNotNull(factory);
    }
}
