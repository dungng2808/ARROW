package com.toptal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class BirthdayFilterProcessor_ArrowTest {

    private BirthdayFilterProcessor processor;
    private Customer customer;

    @Before
    public void setUp() {
        processor = new BirthdayFilterProcessor();
        customer = mock(Customer.class);
    }

    @Test
    public void shouldReturnCustomerWhenBirthdayMonthMatchesCurrentMonth() throws Exception {
        // Given
        Calendar now = new GregorianCalendar();
        Calendar customerBirthday = mock(Calendar.class);
        when(customer.getBirthday()).thenReturn(customerBirthday);
        when(customerBirthday.get(Calendar.MONTH)).thenReturn(now.get(Calendar.MONTH));

        // When
        Customer result = processor.process(customer);

        // Then
        assertSame(customer, result);
    }

    @Test
    public void shouldReturnNullWhenBirthdayMonthDoesNotMatchCurrentMonth() throws Exception {
        // Given
        Calendar now = new GregorianCalendar();
        Calendar customerBirthday = mock(Calendar.class);
        when(customer.getBirthday()).thenReturn(customerBirthday);
        when(customerBirthday.get(Calendar.MONTH)).thenReturn(now.get(Calendar.MONTH) + 1);

        // When
        Customer result = processor.process(customer);

        // Then
        assertNull(result);
    }

    @Test
    public void shouldReturnNullWhenBirthdayIsNull() throws Exception {
        // Given
        when(customer.getBirthday()).thenReturn(null);

        // When
        Customer result = processor.process(customer);

        // Then
        assertNull(result);
    }
}
