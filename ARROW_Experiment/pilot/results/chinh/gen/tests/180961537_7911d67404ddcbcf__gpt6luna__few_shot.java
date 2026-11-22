package io.pivotal.loansource;

import java.util.function.Supplier;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LoansourceApplication_ArrowTest {

  @Test
  public void supplyLoanReturnsSupplierThatCreatesValidLoan() {
    LoansourceApplication application = new LoansourceApplication();
    Supplier<Loan> supplier = application.supplyLoan();

    assertNotNull(supplier);

    Loan loan = supplier.get();
    assertNotNull(loan);
    assertNotNull(loan.getUuid());
    assertTrue(loan.getUuid().length() > 0);
    assertNotNull(loan.getName());
    assertTrue(loan.getAmount() > 0);
  }
}
