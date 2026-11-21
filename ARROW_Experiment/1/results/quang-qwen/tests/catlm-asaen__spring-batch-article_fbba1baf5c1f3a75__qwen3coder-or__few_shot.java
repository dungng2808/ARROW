package com.toptal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class BirthdayFilterProcessor_ArrowTest {

    @Test
    public void shouldReturnCustomerWhenBirthdayMatchesCurrentMonth() throws Exception {
        // Given
        BirthdayFilterProcessor processor = new BirthdayFilterProcessor();
        
        Calendar currentMonth = new GregorianCalendar();
        int currentMonthValue = currentMonth.get(Calendar.MONTH);
        
        Customer customer = mock(Customer.class);
        Calendar customerBirthday = mock(Calendar.class);
        when(customer.getBirthday()).thenReturn(customerBirthday);
        when(customerBirthday.get(Calendar.MONTH)).thenReturn(currentMonthValue);
        
        // When
        Customer result = processor.process(customer);
        
        // Then
        assertSame(customer, result);
    }

    @Test
    public void shouldReturnNullWhenBirthdayDoesNotMatchCurrentMonth() throws Exception {
        // Given
        BirthdayFilterProcessor processor = new BirthdayFilterProcessor();
        
        Calendar currentMonth = new GregorianCalendar();
        int currentMonthValue = currentMonth.get(Calendar.MONTH);
        
        Customer customer = mock(Customer.class);
        Calendar customerBirthday = mock(Calendar.class);
        when(customer.getBirthday()).thenReturn(customerBirthday);
        when(customerBirthday.get(Calendar.MONTH)).thenReturn((currentMonthValue + 1) % 12);
        
        // When
        Customer result = processor.process(customer);
        
        // Then
        assertNull(result);
    }
}
