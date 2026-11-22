package io.pivotal.loansource;

import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class LoansourceApplication_ArrowTest {

    @Test
    public void testMainMethod() {
        // Testing that main method runs without exception
        // This test verifies the application can start
        try {
            LoansourceApplication.main(new String[]{});
        } catch (Exception e) {
            fail("Main method should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testSupplyLoanReturnsNonNull() {
        LoansourceApplication application = new LoansourceApplication();
        Supplier<Loan> loanSupplier = application.supplyLoan();
        
        assertNotNull("Loan supplier should not be null", loanSupplier);
        
        Loan loan = loanSupplier.get();
        assertNotNull("Generated loan should not be null", loan);
        assertNotNull("Loan UUID should not be null", loan.getUuid());
        assertNotNull("Loan name should not be null", loan.getName());
        assertNotNull("Loan amount should not be null", loan.getAmount());
        assertNotNull("Loan status should not be null", loan.getStatus());
    }

    @Test
    public void testSupplyLoanGeneratesValidLoan() {
        LoansourceApplication application = new LoansourceApplication();
        Supplier<Loan> loanSupplier = application.supplyLoan();
        
        Loan loan = loanSupplier.get();
        
        // Verify UUID format
        assertNotNull(loan.getUuid());
        try {
            UUID.fromString(loan.getUuid());
        } catch (IllegalArgumentException e) {
            fail("UUID should be valid format");
        }
        
        // Verify name is from the predefined list
        assertTrue("Name should be from the predefined list", 
                  application.names.contains(loan.getName()));
        
        // Verify amount is from the predefined list
        assertTrue("Amount should be from the predefined list", 
                  application.amounts.contains(loan.getAmount()));
        
        // Verify status is set (should be null since it's not explicitly set in the constructor)
        assertNotNull(loan.getStatus());
    }

    @Test
    public void testSupplyLoanMultipleCalls() {
        LoansourceApplication application = new LoansourceApplication();
        Supplier<Loan> loanSupplier = application.supplyLoan();
        
        // Generate multiple loans
        for (int i = 0; i < 10; i++) {
            Loan loan = loanSupplier.get();
            assertNotNull("Loan " + i + " should not be null", loan);
            assertNotNull("Loan UUID " + i + " should not be null", loan.getUuid());
            assertTrue("Loan name " + i + " should be valid", 
                      application.names.contains(loan.getName()));
            assertTrue("Loan amount " + i + " should be valid", 
                      application.amounts.contains(loan.getAmount()));
        }
    }
}
