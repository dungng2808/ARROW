package org.egov.pg.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pg.config.AppProperties;
import org.egov.pg.models.Transaction;
import org.egov.pg.producer.Producer;
import org.egov.pg.repository.TransactionRepository;
import org.egov.pg.validator.TransactionValidator;
import org.egov.pg.web.models.TransactionCriteria;
import org.egov.tracer.model.CustomException;
import org.junit.Test;
import org.springframework.dao.DataAccessException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionService_ArrowTest {

    @Test
    public void getTransactionsReturnsRepositoryResults() {
        TransactionValidator validator = mock(TransactionValidator.class);
        GatewayService gatewayService = mock(GatewayService.class);
        Producer producer = mock(Producer.class);
        TransactionRepository repository = mock(TransactionRepository.class);
        CollectionService collectionService = mock(CollectionService.class);
        EnrichmentService enrichmentService = mock(EnrichmentService.class);
        AppProperties appProperties = mock(AppProperties.class);
        TransactionService service = new TransactionService(validator, gatewayService, producer,
                repository, collectionService, enrichmentService, appProperties);

        TransactionCriteria criteria = mock(TransactionCriteria.class);
        List<Transaction> expected = new ArrayList<Transaction>();
        expected.add(new Transaction());
        when(repository.fetchTransactions(criteria)).thenReturn(expected);

        assertSame(expected, service.getTransactions(criteria));
    }

    @Test
    public void getTransactionsReturnsEmptyRepositoryResults() {
        TransactionValidator validator = mock(TransactionValidator.class);
        GatewayService gatewayService = mock(GatewayService.class);
        Producer producer = mock(Producer.class);
        TransactionRepository repository = mock(TransactionRepository.class);
        CollectionService collectionService = mock(CollectionService.class);
        EnrichmentService enrichmentService = mock(EnrichmentService.class);
        AppProperties appProperties = mock(AppProperties.class);
        TransactionService service = new TransactionService(validator, gatewayService, producer,
                repository, collectionService, enrichmentService, appProperties);

        TransactionCriteria criteria = mock(TransactionCriteria.class);
        when(repository.fetchTransactions(criteria)).thenReturn(Collections.<Transaction>emptyList());

        assertEquals(Collections.emptyList(), service.getTransactions(criteria));
    }

    @Test
    public void getTransactionsConvertsDataAccessExceptionToCustomException() {
        TransactionValidator validator = mock(TransactionValidator.class);
        GatewayService gatewayService = mock(GatewayService.class);
        Producer producer = mock(Producer.class);
        TransactionRepository repository = mock(TransactionRepository.class);
        CollectionService collectionService = mock(CollectionService.class);
        EnrichmentService enrichmentService = mock(EnrichmentService.class);
        AppProperties appProperties = mock(AppProperties.class);
        TransactionService service = new TransactionService(validator, gatewayService, producer,
                repository, collectionService, enrichmentService, appProperties);

        TransactionCriteria criteria = mock(TransactionCriteria.class);
        when(repository.fetchTransactions(criteria)).thenThrow(new DataAccessException("failure") { });

        try {
            service.getTransactions(criteria);
            fail("Expected CustomException");
        } catch (CustomException exception) {
            assertEquals("FETCH_TXNS_FAILED", exception.getCode());
        }
    }
}
