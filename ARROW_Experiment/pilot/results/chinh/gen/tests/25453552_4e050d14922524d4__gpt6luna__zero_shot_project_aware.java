package org.myLazyClock.calendarApi;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CalendarFactory_ArrowTest {

    @Test
    public void getInstanceReturnsSingleton() {
        CalendarFactory first = CalendarFactory.getInstance();
        CalendarFactory second = CalendarFactory.getInstance();

        assertSame(first, second);
    }

    @Test
    public void iteratorReturnsAnIterator() {
        CalendarFactory factory = CalendarFactory.getInstance();

        Iterator<CalendarStrategy> iterator = factory.iterator();

        assertNotNull(iterator);
    }

    @Test
    public void iteratorContainsOnlyStrategiesWithIdsThatCanBeRetrieved() {
        CalendarFactory factory = CalendarFactory.getInstance();
        Iterator<CalendarStrategy> iterator = factory.iterator();

        while (iterator.hasNext()) {
            CalendarStrategy strategy = iterator.next();
            assertNotNull(strategy);
            assertSame(strategy, factory.get(strategy.getId()));
        }
    }

    @Test(expected = NullPointerException.class)
    public void getThrowsForUnknownId() {
        CalendarFactory.getInstance().get(null);
    }
}
