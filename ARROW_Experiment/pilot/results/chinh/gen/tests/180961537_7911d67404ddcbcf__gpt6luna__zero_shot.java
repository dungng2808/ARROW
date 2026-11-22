package io.pivotal.loansource;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LoansourceApplication_ArrowTest {

  @Test
  public void supplyLoanReturnsSupplierProducingValidLoan() {
    LoansourceApplication application = new LoansourceApplication();
    Supplier<Loan> supplier = application.supplyLoan();

    Loan loan = supplier.get();

    assertNotNull(loan);
    assertNotNull(loan.getUuid());
    assertTrue(loan.getUuid().length() > 0);
    assertNotNull(loan.getName());
    assertTrue(java.util.Arrays.asList(
        "Donald", "Theresa", "Vladimir", "Angela", "Emmanuel", "Shinzō", "Jacinda", "Kim")
        .contains(loan.getName()));
    assertTrue(java.util.Arrays.asList(
        10L, 100L, 1000L, 10000L, 100000L, 1000000L, 10000000L, 100000000L)
        .contains(loan.getAmount()));
  }

  @Test
  public void supplierProducesDifferentUniqueIdentifiers() {
    LoansourceApplication application = new LoansourceApplication();
    Supplier<Loan> supplier = application.supplyLoan();

    Set<String> identifiers = new HashSet<String>();
    identifiers.add(supplier.get().getUuid());
    identifiers.add(supplier.get().getUuid());

    assertEquals(2, identifiers.size());
  }
}
