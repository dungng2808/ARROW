package com.toptal;

import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class BirthdayFilterProcessor_ArrowTest {

    @Test
    public void processReturnsCustomerBornInCurrentMonth() throws Exception {
        Customer customer = new Customer();
        customer.setBirthday(new GregorianCalendar(
                new GregorianCalendar().get(Calendar.YEAR), new GregorianCalendar().get(Calendar.MONTH), 1));

        BirthdayFilterProcessor processor = new BirthdayFilterProcessor();

        assertSame(customer, processor.process(customer));
    }

    @Test
    public void processReturnsNullForCustomerBornInDifferentMonth() throws Exception {
        Calendar current = new GregorianCalendar();
        int differentMonth = (current.get(Calendar.MONTH) + 1) % 12;

        Customer customer = new Customer();
        customer.setBirthday(new GregorianCalendar(current.get(Calendar.YEAR), differentMonth, 1));

        BirthdayFilterProcessor processor = new BirthdayFilterProcessor();

        assertNull(processor.process(customer));
    }
}
