package org.egov.pg.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pg.config.AppProperties;
import org.egov.pg.models.Transaction;
import org.egov.pg.producer.Producer;
import org.egov.pg.repository.TransactionRepository;
import org.egov.pg.validator.TransactionValidator;
import org.egov.pg.web.models.TransactionCriteria;
import org.egov.pg.web.models.TransactionRequest;
import org.junit.Test;
import org.springframework.dao.DataAccessException;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class TransactionService_ArrowTest {

    @Test
    public void getTransactionsReturnsRepositoryResults() {
        final List<Transaction> expected = Collections.emptyList();
        TransactionRepository repository = new TransactionRepository() {
            @Override
            public List<Transaction> fetchTransactions(TransactionCriteria criteria) {
                return expected;
            }
        };

        TransactionService service = new TransactionService(
                null, null, null, repository, null, null, null);

        assertSame(expected, service.getTransactions(new TransactionCriteria()));
    }

    @Test
    public void getTransactionsConvertsDataAccessException() {
        TransactionRepository repository = new TransactionRepository() {
            @Override
            public List<Transaction> fetchTransactions(TransactionCriteria criteria) {
                throw new DataAccessException("failure") {
                };
            }
        };

        TransactionService service = new TransactionService(
                null, null, null, repository, null, null, null);

        try {
            service.getTransactions(new TransactionCriteria());
            fail("Expected CustomException");
        } catch (org.egov.tracer.model.CustomException expected) {
            // Expected conversion of the repository failure.
        }
    }
}
