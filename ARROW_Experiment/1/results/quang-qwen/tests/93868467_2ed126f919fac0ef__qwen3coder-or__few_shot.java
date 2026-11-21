package com.redis.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class MythTime_ArrowTest {

    @Test
    public void testGetTime() {
        String time = MythTime.getTime();
        assertNotNull(time);
        assertTrue(time.matches("\\d{2}:\\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    public void testGetDateTime() {
        String dateTime = MythTime.getDateTime();
        assertNotNull(dateTime);
        assertTrue(dateTime.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    public void testGetDate() {
        String date = MythTime.getDate();
        assertNotNull(date);
        assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"));
    }
}
