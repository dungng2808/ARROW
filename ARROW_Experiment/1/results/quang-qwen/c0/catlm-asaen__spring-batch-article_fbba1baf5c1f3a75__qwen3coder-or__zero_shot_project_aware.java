package com.toptal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.Test;
import org.junit.Before;
import com.toptal.Customer;

public class BirthdayFilterProcessor_ArrowTest {

    private BirthdayFilterProcessor processor;
    
    @Before
    public void setUp() {
        processor = new BirthdayFilterProcessor();
    }
    
    @Test
    public void testProcess_MatchesCurrentMonth_ReturnsCustomer() throws Exception {
        // Setup current month
        Calendar now = new GregorianCalendar();
        int currentMonth = now.get(Calendar.MONTH);
        
        // Create customer with birthday in current month
        Customer customer = new Customer();
        Calendar birthday = new GregorianCalendar();
        birthday.set(Calendar.MONTH, currentMonth);
        customer.setBirthday(birthday);
        
        // Process
        Customer result = processor.process(customer);
        
        // Verify
        assertThat(result).isSameAs(customer);
    }
    
    @Test
    public void testProcess_DoesNotMatchCurrentMonth_ReturnsNull() throws Exception {
        // Setup current month
        Calendar now = new GregorianCalendar();
        int currentMonth = now.get(Calendar.MONTH);
        
        // Create customer with birthday in different month
        Customer customer = new Customer();
        Calendar birthday = new GregorianCalendar();
        birthday.set(Calendar.MONTH, (currentMonth + 1) % 12);
        customer.setBirthday(birthday);
        
        // Process
        Customer result = processor.process(customer);
        
        // Verify
        assertThat(result).isNull();
    }
    
    @Test
    public void testProcess_NullCustomer_ReturnsNull() throws Exception {
        // Process null customer
        Customer result = processor.process(null);
        
        // Verify
        assertThat(result).isNull();
    }
}
