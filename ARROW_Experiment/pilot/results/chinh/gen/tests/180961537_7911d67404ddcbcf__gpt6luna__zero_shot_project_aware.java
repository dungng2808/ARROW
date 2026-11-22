package io.pivotal.loansource;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LoansourceApplication_ArrowTest {

  @Test
  public void supplyLoanReturnsLoanWithExpectedProperties() {
    LoansourceApplication application = new LoansourceApplication();
    Supplier<Loan> supplier = application.supplyLoan();

    Loan loan = supplier.get();

    assertNotNull(loan);
    assertNotNull(loan.getUuid());
    assertTrue(loan.getUuid().length() > 0);
    assertTrue(Arrays.asList("Donald", "Theresa", "Vladimir", "Angela", "Emmanuel",
        "Shinzō", "Jacinda", "Kim").contains(loan.getName()));
    List<Long> amounts = Arrays.asList(10L, 100L, 1000L, 10000L, 100000L, 1000000L,
        10000000L, 100000000L);
    assertTrue(amounts.contains(loan.getAmount()));
  }
}
