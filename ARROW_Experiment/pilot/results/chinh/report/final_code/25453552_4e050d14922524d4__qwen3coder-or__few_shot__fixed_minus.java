package org.myLazyClock.calendarApi;

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;

public class CalendarFactory_ArrowTest {

    private CalendarFactory calendarFactory;

    @Before
    public void setUp() throws Exception {
        // Use the singleton instance
        calendarFactory = CalendarFactory.getInstance();
    }

    @Test
    public void testGetInstanceReturnsSameInstance() {
        CalendarFactory factory1 = CalendarFactory.getInstance();
        CalendarFactory factory2 = CalendarFactory.getInstance();
        assertSame(factory1, factory2);
    }

    @Test
    public void testGetReturnsStrategyWhenIdExists() {
        // Since we can't know what strategies are actually loaded,
        // we'll test with a known ID that should exist based on the implementation
        // We'll use the iterator to check what's available
        Iterator<CalendarStrategy> iterator = calendarFactory.iterator();
        if (iterator.hasNext()) {
            CalendarStrategy strategy = iterator.next();
            CalendarId id = strategy.getId();
            
            // This should not throw NullPointerException
            CalendarStrategy result = calendarFactory.get(id);
            assertNotNull(result);
            assertEquals(strategy, result);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testGetThrowsNullPointerExceptionWhenIdDoesNotExist() {
        // Use a null value instead of unknown enum constant
        CalendarId nonExistentId = null;
        calendarFactory.get(nonExistentId);
    }

    @Test
    public void testIteratorReturnsAllStrategies() {
        Iterator<CalendarStrategy> iterator = calendarFactory.iterator();
        assertNotNull(iterator);
        
        int count = 0;
        while (iterator.hasNext()) {
            CalendarStrategy strategy = iterator.next();
            assertNotNull(strategy);
            assertNotNull(strategy.getId());
            assertNotNull(strategy.getName());
            count++;
        }
        
        // At least one strategy should be present
        assertTrue("Expected at least one strategy", count >= 1);
    }

    @Test
    public void testIteratorHasNextAndNext() {
        Iterator<CalendarStrategy> iterator = calendarFactory.iterator();
        assertTrue("Iterator should have next element", iterator.hasNext());
        
        CalendarStrategy strategy = iterator.next();
        assertNotNull("Strategy should not be null", strategy);
        assertNotNull("Strategy ID should not be null", strategy.getId());
        assertNotNull("Strategy name should not be null", strategy.getName());
    }
}
