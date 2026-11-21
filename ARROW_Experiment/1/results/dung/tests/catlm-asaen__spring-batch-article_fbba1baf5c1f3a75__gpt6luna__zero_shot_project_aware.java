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
        Calendar birthday = new GregorianCalendar();
        customer.setBirthday(birthday);

        BirthdayFilterProcessor processor = new BirthdayFilterProcessor();

        assertSame(customer, processor.process(customer));
    }

    @Test
    public void processReturnsNullForCustomerBornInDifferentMonth() throws Exception {
        Customer customer = new Customer();
        Calendar birthday = new GregorianCalendar();
        birthday.set(Calendar.MONTH, (birthday.get(Calendar.MONTH) + 1) % 12);
        customer.setBirthday(birthday);

        BirthdayFilterProcessor processor = new BirthdayFilterProcessor();

        assertNull(processor.process(customer));
    }
}
