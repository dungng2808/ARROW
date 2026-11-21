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
    public void testInitiateTransaction_Success() throws Exception {
        // Setup
        TransactionRequest transactionRequest = new TransactionRequest();
        RequestInfo requestInfo = new RequestInfo();
        transactionRequest.setRequestInfo(requestInfo);
        Transaction transaction = new Transaction();
        transaction.setTxnId("TXN123");
        transactionRequest.setTransaction(transaction);

        when(validator.validateCreateTxn(any(TransactionRequest.class))).thenReturn(true);
        when(enrichmentService.enrichCreateTransaction(any(TransactionRequest.class))).thenReturn(null);
        when(gatewayService.initiateTxn(any(Transaction.class))).thenReturn(URI.create("http://gateway.com"));

        // Execute
        Transaction result = transactionService.initiateTransaction(transactionRequest);

        // Verify
        assertNotNull(result);
        assertEquals("TXN123", result.getTxnId());
        assertEquals("http://gateway.com", result.getRedirectUrl());
        verify(producer).push(any(String.class), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer).push(any(String.class), any(TransactionDumpRequest.class));
    }

    @Test
    public void testInitiateTransaction_SkipGateway() throws Exception {
        // Setup
        TransactionRequest transactionRequest = new TransactionRequest();
        RequestInfo requestInfo = new RequestInfo();
        transactionRequest.setRequestInfo(requestInfo);
        Transaction transaction = new Transaction();
        transaction.setTxnId("TXN123");
        transactionRequest.setTransaction(transaction);

        when(validator.validateCreateTxn(any(TransactionRequest.class))).thenReturn(true);
        when(enrichmentService.enrichCreateTransaction(any(TransactionRequest.class))).thenReturn(null);
        when(validator.skipGateway(any(Transaction.class))).thenReturn(true);
        when(collectionService.generateReceipt(any(RequestInfo.class), any(Transaction.class)))
                .thenReturn(Arrays.asList(new Receipt()));

        // Execute
        Transaction result = transactionService.initiateTransaction(transactionRequest);

        // Verify
        assertNotNull(result);
        assertEquals("TXN123", result.getTxnId());
        assertEquals(Transaction.TxnStatusEnum.SUCCESS, result.getTxnStatus());
        verify(collectionService).generateReceipt(any(RequestInfo.class), any(Transaction.class));
        verify(producer).push(any(String.class), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer).push(any(String.class), any(TransactionDumpRequest.class));
    }

    @Test
    public void testGetTransactions_Success() throws Exception {
        // Setup
        TransactionCriteria transactionCriteria = new TransactionCriteria();
        List<Transaction> expectedTransactions = Arrays.asList(new Transaction(), new Transaction());

        when(transactionRepository.fetchTransactions(any(TransactionCriteria.class))).thenReturn(expectedTransactions);

        // Execute
        List<Transaction> result = transactionService.getTransactions(transactionCriteria);

        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedTransactions, result);
    }

    @Test(expected = CustomException.class)
    public void testGetTransactions_DataAccessException() throws Exception {
        // Setup
        TransactionCriteria transactionCriteria = new TransactionCriteria();

        doThrow(new org.springframework.dao.DataAccessException("DB Error") {}).when(transactionRepository)
                .fetchTransactions(any(TransactionCriteria.class));

        // Execute
        transactionService.getTransactions(transactionCriteria);
    }

    @Test
    public void testUpdateTransaction_Success() throws Exception {
        // Setup
        RequestInfo requestInfo = new RequestInfo();
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("txnId", "TXN123");

        Transaction currentTxnStatus = new Transaction();
        currentTxnStatus.setTxnId("TXN123");
        currentTxnStatus.setAmount(100.0);

        Transaction newTxn = new Transaction();
        newTxn.setTxnId("TXN123");
        newTxn.setAmount(100.0);
        newTxn.setTxnStatus(Transaction.TxnStatusEnum.SUCCESS);
        newTxn.setResponseJson("{\"status\":\"SUCCESS\"}");

        when(validator.validateUpdateTxn(any(Map.class))).thenReturn(currentTxnStatus);
        when(validator.skipGateway(any(Transaction.class))).thenReturn(false);
        when(gatewayService.getLiveStatus(any(Transaction.class), any(Map.class))).thenReturn(newTxn);
        when(validator.shouldGenerateReceipt(any(Transaction.class), any(Transaction.class))).thenReturn(true);
        when(enrichmentService.enrichUpdateTransaction(any(TransactionRequest.class), any(Transaction.class)))
                .thenReturn(newTxn);
        when(collectionService.generateReceipt(any(RequestInfo.class), any(Transaction.class)))
                .thenReturn(Arrays.asList(new Receipt()));

        // Execute
        List<Transaction> result = transactionService.updateTransaction(requestInfo, requestParams);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TXN123", result.get(0).getTxnId());
        assertEquals(Transaction.TxnStatusEnum.SUCCESS, result.get(0).getTxnStatus());
        verify(collectionService).generateReceipt(any(RequestInfo.class), any(Transaction.class));
        verify(producer).push(any(String.class), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer).push(any(String.class), any(TransactionDumpRequest.class));
    }

    @Test
    public void testUpdateTransaction_SkipGateway() throws Exception {
        // Setup
        RequestInfo requestInfo = new RequestInfo();
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("txnId", "TXN123");

        Transaction currentTxnStatus = new Transaction();
        currentTxnStatus.setTxnId("TXN123");
        currentTxnStatus.setAmount(100.0);

        when(validator.validateUpdateTxn(any(Map.class))).thenReturn(currentTxnStatus);
        when(validator.skipGateway(any(Transaction.class))).thenReturn(true);

        // Execute
        List<Transaction> result = transactionService.updateTransaction(requestInfo, requestParams);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TXN123", result.get(0).getTxnId());
        verifyZeroInteractions(gatewayService);
        verifyZeroInteractions(collectionService);
        verify(producer).push(any(String.class), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer).push(any(String.class), any(TransactionDumpRequest.class));
    }

    @Test
    public void testUpdateTransaction_NoReceiptGeneration() throws Exception {
        // Setup
        RequestInfo requestInfo = new RequestInfo();
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("txnId", "TXN123");

        Transaction currentTxnStatus = new Transaction();
        currentTxnStatus.setTxnId("TXN123");
        currentTxnStatus.setAmount(100.0);

        Transaction newTxn = new Transaction();
        newTxn.setTxnId("TXN123");
        newTxn.setAmount(100.0);
        newTxn.setTxnStatus(Transaction.TxnStatusEnum.SUCCESS);
        newTxn.setResponseJson("{\"status\":\"SUCCESS\"}");

        when(validator.validateUpdateTxn(any(Map.class))).thenReturn(currentTxnStatus);
        when(validator.skipGateway(any(Transaction.class))).thenReturn(false);
        when(gatewayService.getLiveStatus(any(Transaction.class), any(Map.class))).thenReturn(newTxn);
        when(validator.shouldGenerateReceipt(any(Transaction.class), any(Transaction.class))).thenReturn(false);

        // Execute
        List<Transaction> result = transactionService.updateTransaction(requestInfo, requestParams);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TXN123", result.get(0).getTxnId());
        assertEquals(Transaction.TxnStatusEnum.SUCCESS, result.get(0).getTxnStatus());
        verifyZeroInteractions(collectionService);
        verify(producer).push(any(String.class), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer).push(any(String.class), any(TransactionDumpRequest.class));
    }

    @Test
    public void testGenerateReceipt_Success() throws Exception {
        // Setup
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

        when(collectionService.generateReceipt(any(RequestInfo.class), any(Transaction.class)))
                .thenReturn(Arrays.asList(receipt));

        // Execute
        transactionService.getClass().getDeclaredMethod("generateReceipt", RequestInfo.class, Transaction.class)
                .invoke(transactionService, requestInfo, transaction);

        // Verify
        assertEquals("REC123", transaction.getReceipt());
    }

    @Test
    public void testGenerateReceipt_Exception() throws Exception {
        // Setup
        RequestInfo requestInfo = new RequestInfo();
        Transaction transaction = new Transaction();
        transaction.setTxnId("TXN123");

        when(collectionService.generateReceipt(any(RequestInfo.class), any(Transaction.class)))
                .thenThrow(new CustomException("GEN_REC_FAILED", "Failed to generate receipt"));

        // Execute
        transactionService.getClass().getDeclaredMethod("generateReceipt", RequestInfo.class, Transaction.class)
                .invoke(transactionService, requestInfo, transaction);

        // Verify
        assertEquals("TXN_RECEIPT_GEN_FAILED", transaction.getTxnStatusMsg());
    }
}
