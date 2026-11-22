package org.myLazyClock.calendarApi;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

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
        }
    }

    @Test(expected = NullPointerException.class)
    public void getThrowsForUnknownId() {
        CalendarFactory.getInstance().get(null);
    }

    @Test
    public void iteratorContainsStrategiesReturnedByGet() {
        CalendarFactory factory = CalendarFactory.getInstance();
        Iterator<CalendarStrategy> strategies = factory.iterator();

        while (strategies.hasNext()) {
            CalendarStrategy strategy = strategies.next();
            assertSame(strategy, factory.get(strategy.getId()));
        }
    }
}
