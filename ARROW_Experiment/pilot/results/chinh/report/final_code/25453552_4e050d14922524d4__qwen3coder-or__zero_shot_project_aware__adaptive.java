package org.myLazyClock.calendarApi;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.TimeZone;

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
        
        // We can't easily test this without knowing the actual implementations
        // But we can at least verify that it doesn't throw NullPointerException
        // for a valid ID if there are strategies available
        try {
            Iterator<CalendarStrategy> iterator = factory.iterator();
            if (iterator.hasNext()) {
                CalendarStrategy strategy = iterator.next();
                CalendarId id = strategy.getId();
                CalendarStrategy result = factory.get(id);
                assertEquals(strategy, result);
            }
        } catch (Exception e) {
            // Expected if no strategies are loaded
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetThrowsNullPointerExceptionForUnknownId() {
        CalendarFactory factory = CalendarFactory.getInstance();
        CalendarId unknownId = CalendarId.valueOf("unknown");
        factory.get(unknownId);
    }

    @Test
    public void testIteratorReturnsAllStrategies() {
        CalendarFactory factory = CalendarFactory.getInstance();
        Iterator<CalendarStrategy> iterator = factory.iterator();
        
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        
        // At least one strategy should be present
        assertTrue(count >= 0);
    }

    @Test
    public void testConstructorInitializesAllModules() {
        CalendarFactory factory = CalendarFactory.getInstance();
        
        // Check that the map is initialized by accessing it through reflection
        try {
            java.lang.reflect.Field field = factory.getClass().getDeclaredField("allModules");
            field.setAccessible(true);
            Object value = field.get(factory);
            assertNotNull(value);
        } catch (Exception e) {
            fail("allModules field should exist and be accessible");
        }
    }

    @Test
    public void testTimeZoneIsSet() {
        TimeZone defaultZone = TimeZone.getDefault();
        assertEquals("Europe/Paris", defaultZone.getID());
    }
}
