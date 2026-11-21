package org.egov.pg.service;

import org.junit.Test;

public class TransactionService_ArrowTest {

    @Test(expected = NullPointerException.class)
    public void getTransactionsWithNullCriteriaThrowsNullPointerException() {
        TransactionService service = new TransactionService(null, null, null, null, null, null, null);
        service.getTransactions(null);
    }

    @Test(expected = NullPointerException.class)
    public void initiateTransactionWithoutValidatorThrowsNullPointerException() {
        TransactionService service = new TransactionService(null, null, null, null, null, null, null);
        service.initiateTransaction(null);
    }

    @Test(expected = NullPointerException.class)
    public void updateTransactionWithoutValidatorThrowsNullPointerException() {
        TransactionService service = new TransactionService(null, null, null, null, null, null, null);
        service.updateTransaction(null, null);
    }
}
