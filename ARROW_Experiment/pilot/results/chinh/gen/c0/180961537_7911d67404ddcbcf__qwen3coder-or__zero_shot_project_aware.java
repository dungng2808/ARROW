package io.pivotal.loansource;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class LoansourceApplication_ArrowTest {

    @Test
    public void testMain() {
        // This test verifies that the main method runs without throwing an exception
        // and logs the startup message. We cannot easily test the logging without
        // more complex setup, so we just verify it doesn't crash.
        LoansourceApplication.main(new String[]{});
    }

    @Test
    public void testSupplyLoanReturnsNonNullLoan() {
        LoansourceApplication application = new LoansourceApplication();
        Supplier<Loan> loanSupplier = application.supplyLoan();
        
        Loan loan = loanSupplier.get();
        
        assertThat(loan).isNotNull();
        assertThat(loan.getUuid()).isNotNull();
        assertThat(loan.getName()).isNotNull();
        assertThat(loan.getAmount()).isNotNull();
        assertThat(loan.getStatus()).isEqualTo("APPROVED");
    }

    @Test
    public void testSupplyLoanReturnsLoanWithValidUuid() {
        LoansourceApplication application = new LoansourceApplication();
        Supplier<Loan> loanSupplier = application.supplyLoan();
        
        Loan loan = loanSupplier.get();
        
        UUID uuid = UUID.fromString(loan.getUuid());
        assertThat(uuid).isNotNull();
    }

    @Test
    public void testSupplyLoanReturnsLoanWithNameFromList() {
        LoansourceApplication application = new LoansourceApplication();
        Supplier<Loan> loanSupplier = application.supplyLoan();
        
        Loan loan = loanSupplier.get();
        
        List<String> names = Arrays.asList("Donald", "Theresa", "Vladimir", "Angela", "Emmanuel", "Shinzō", "Jacinda", "Kim");
        assertThat(names).contains(loan.getName());
    }

    @Test
    public void testSupplyLoanReturnsLoanWithAmountFromList() {
        LoansourceApplication application = new LoansourceApplication();
        Supplier<Loan> loanSupplier = application.supplyLoan();
        
        Loan loan = loanSupplier.get();
        
        List<Long> amounts = Arrays.asList(10L, 100L, 1000L, 10000L, 100000L, 1000000L, 10000000L, 100000000L, 100000000L);
        assertThat(amounts).contains(loan.getAmount());
    }

    @Test
    public void testSupplyLoanReturnsLoanWithApprovedStatus() {
        LoansourceApplication application = new LoansourceApplication();
        Supplier<Loan> loanSupplier = application.supplyLoan();
        
        Loan loan = loanSupplier.get();
        
        assertThat(loan.getStatus()).isEqualTo("APPROVED");
    }
}
