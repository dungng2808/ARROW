package org.myLazyClock.calendarApi;

import java.util.Iterator;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class CalendarFactory_ArrowTest {

    @Test
    public void getInstanceReturnsFactory() {
        CalendarFactory factory = CalendarFactory.getInstance();
        assertNotNull(factory);
    }

    @Test
    public void getReturnsStrategyForItsId() {
        CalendarFactory factory = CalendarFactory.getInstance();
        Iterator<CalendarStrategy> strategies = factory.iterator();

        if (strategies.hasNext()) {
            CalendarStrategy strategy = strategies.next();
            assertSame(strategy, factory.get(strategy.getId()));
        } else {
            try {
                factory.get(null);
                fail("Expected lookup of an absent strategy to throw NullPointerException");
            } catch (NullPointerException expected) {
                assertNotNull(expected);
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void getThrowsForUnknownId() {
        CalendarFactory.getInstance().get(null);
    }

    @Test
    public void iteratorContainsStrategiesAccessibleById() {
        CalendarFactory factory = CalendarFactory.getInstance();
        Iterator<CalendarStrategy> strategies = factory.iterator();

        while (strategies.hasNext()) {
            CalendarStrategy strategy = strategies.next();
            assertSame(strategy, factory.get(strategy.getId()));
        }
    }
}
