package org.egov.pg.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pg.config.AppProperties;
import org.egov.pg.models.Receipt;
import org.egov.pg.models.Transaction;
import org.egov.pg.models.TransactionDump;
import org.egov.pg.models.TransactionDumpRequest;
import org.egov.pg.producer.Producer;
import org.egov.pg.repository.TransactionRepository;
import org.egov.pg.validator.TransactionValidator;
import org.egov.pg.web.models.TransactionCriteria;
import org.egov.pg.web.models.TransactionRequest;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TransactionService_ArrowTest {

    @Mock
    private TransactionValidator validator;

    @Mock
    private GatewayService gatewayService;

    @Mock
    private Producer producer;

    @Mock
    private EnrichmentService enrichmentService;

    @Mock
    private AppProperties appProperties;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CollectionService collectionService;

    private TransactionService transactionService;

    @Before
    public void setUp() {
        transactionService = new TransactionService(validator, gatewayService, producer, transactionRepository,
                collectionService, enrichmentService, appProperties);
    }

    @Test
    public void testInitiateTransaction_WithSkipGateway() {
        TransactionRequest transactionRequest = new TransactionRequest();
        RequestInfo requestInfo = new RequestInfo();
        transactionRequest.setRequestInfo(requestInfo);
        Transaction transaction = new Transaction();
        transaction.setTxnId("TXN123");
        transactionRequest.setTransaction(transaction);

        when(validator.skipGateway(any(Transaction.class))).thenReturn(true);
        when(validator.validateCreateTxn(any(TransactionRequest.class))).thenReturn(true);
        when(enrichmentService.enrichCreateTransaction(any(TransactionRequest.class))).thenReturn(null);

        Transaction result = transactionService.initiateTransaction(transactionRequest);

        assertEquals("TXN123", result.getTxnId());
        assertEquals(Transaction.TxnStatusEnum.SUCCESS, result.getTxnStatus());
        verify(producer, times(1)).push(any(String.class), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer, times(1)).push(any(String.class), any(TransactionDumpRequest.class));
    }

    @Test
    public void testInitiateTransaction_WithoutSkipGateway() throws Exception {
        TransactionRequest transactionRequest = new TransactionRequest();
        RequestInfo requestInfo = new RequestInfo();
        transactionRequest.setRequestInfo(requestInfo);
        Transaction transaction = new Transaction();
        transaction.setTxnId("TXN123");
        transactionRequest.setTransaction(transaction);

        when(validator.skipGateway(any(Transaction.class))).thenReturn(false);
        when(validator.validateCreateTxn(any(TransactionRequest.class))).thenReturn(true);
        when(enrichmentService.enrichCreateTransaction(any(TransactionRequest.class))).thenReturn(null);
        when(gatewayService.initiateTxn(any(Transaction.class))).thenReturn(URI.create("http://example.com"));

        Transaction result = transactionService.initiateTransaction(transactionRequest);

        assertEquals("TXN123", result.getTxnId());
        assertNotNull(result.getRedirectUrl());
        verify(producer, times(1)).push(any(String.class), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer, times(1)).push(any(String.class), any(TransactionDumpRequest.class));
    }

    @Test
    public void testGetTransactions_Success() {
        TransactionCriteria criteria = new TransactionCriteria();
        List<Transaction> expectedTransactions = Arrays.asList(new Transaction(), new Transaction());

        when(transactionRepository.fetchTransactions(criteria)).thenReturn(expectedTransactions);

        List<Transaction> result = transactionService.getTransactions(criteria);

        assertEquals(expectedTransactions, result);
    }

    @Test(expected = CustomException.class)
    public void testGetTransactions_DataAccessException() {
        TransactionCriteria criteria = new TransactionCriteria();

        when(transactionRepository.fetchTransactions(criteria)).thenThrow(new RuntimeException());

        transactionService.getTransactions(criteria);
    }

    @Test
    public void testUpdateTransaction_SkipGateway() {
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("txnId", "TXN123");

        Transaction currentTxnStatus = new Transaction();
        currentTxnStatus.setTxnId("TXN123");
        currentTxnStatus.setTxnStatus(Transaction.TxnStatusEnum.PENDING);

        when(validator.validateUpdateTxn(requestParams)).thenReturn(currentTxnStatus);
        when(validator.skipGateway(any(Transaction.class))).thenReturn(true);

        List<Transaction> result = transactionService.updateTransaction(new RequestInfo(), requestParams);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(currentTxnStatus, result.get(0));
    }

    @Test
    public void testUpdateTransaction_WithGateway() {
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("txnId", "TXN123");

        Transaction currentTxnStatus = new Transaction();
        currentTxnStatus.setTxnId("TXN123");
        currentTxnStatus.setTxnStatus(Transaction.TxnStatusEnum.PENDING);

        Transaction newTxn = new Transaction();
        newTxn.setTxnId("TXN123");
        newTxn.setTxnStatus(Transaction.TxnStatusEnum.SUCCESS);

        when(validator.validateUpdateTxn(requestParams)).thenReturn(currentTxnStatus);
        when(validator.skipGateway(any(Transaction.class))).thenReturn(false);
        when(gatewayService.getLiveStatus(any(Transaction.class), any(Map.class))).thenReturn(newTxn);
        when(validator.shouldGenerateReceipt(any(Transaction.class), any(Transaction.class))).thenReturn(true);
        when(enrichmentService.enrichUpdateTransaction(any(TransactionRequest.class), any(Transaction.class))).thenReturn(null);

        List<Transaction> result = transactionService.updateTransaction(new RequestInfo(), requestParams);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(newTxn, result.get(0));
        verify(producer, times(1)).push(any(String.class), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer, times(1)).push(any(String.class), any(TransactionDumpRequest.class));
    }

    @Test
    public void testGenerateReceipt_Success() {
        RequestInfo requestInfo = new RequestInfo();
        Transaction transaction = new Transaction();
        transaction.setTxnId("TXN123");

        Receipt receipt = new Receipt();
        receipt.setBill(Collections.singletonList(
                new org.egov.pg.models.Bill(
                        Collections.singletonList(
                                new org.egov.pg.models.BillDetails("REC123")
                        )
                )
        ));

        when(collectionService.generateReceipt(requestInfo, transaction)).thenReturn(Arrays.asList(receipt));

        transactionService.generateReceipt(requestInfo, transaction);

        assertEquals("REC123", transaction.getReceipt());
    }

    @Test
    public void testGenerateReceipt_Exception() {
        RequestInfo requestInfo = new RequestInfo();
        Transaction transaction = new Transaction();
        transaction.setTxnId("TXN123");

        when(collectionService.generateReceipt(requestInfo, transaction))
                .thenThrow(new CustomException("ERROR", "Error occurred"));

        transactionService.generateReceipt(requestInfo, transaction);

        assertEquals("TXN_RECEIPT_GEN_FAILED", transaction.getTxnStatusMsg());
    }
}
