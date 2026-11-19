package org.egov.pg.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pg.config.AppProperties;
import org.egov.pg.constants.PgConstants;
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
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
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
    public void testInitiateTransaction_NormalFlow() {
        // Given
        TransactionRequest transactionRequest = mock(TransactionRequest.class);
        Transaction transaction = mock(Transaction.class);
        RequestInfo requestInfo = mock(RequestInfo.class);
        URI uri = URI.create("http://example.com");

        when(transactionRequest.getTransaction()).thenReturn(transaction);
        when(transactionRequest.getRequestInfo()).thenReturn(requestInfo);
        when(validator.skipGateway(any(Transaction.class))).thenReturn(false);
        when(gatewayService.initiateTxn(any(Transaction.class))).thenReturn(uri);
        when(appProperties.getSaveTxnTopic()).thenReturn("save-txn-topic");
        when(appProperties.getSaveTxnDumpTopic()).thenReturn("save-txn-dump-topic");

        // When
        Transaction result = transactionService.initiateTransaction(transactionRequest);

        // Then
        verify(validator).validateCreateTxn(transactionRequest);
        verify(enrichmentService).enrichCreateTransaction(transactionRequest);
        verify(gatewayService).initiateTxn(transaction);
        verify(transaction).setRedirectUrl(uri.toString());
        verify(producer).push(eq("save-txn-topic"), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer).push(eq("save-txn-dump-topic"), any(TransactionDumpRequest.class));
        assertEquals(transaction, result);
    }

    @Test
    public void testInitiateTransaction_SkipGateway() {
        // Given
        TransactionRequest transactionRequest = mock(TransactionRequest.class);
        Transaction transaction = mock(Transaction.class);
        RequestInfo requestInfo = mock(RequestInfo.class);

        when(transactionRequest.getTransaction()).thenReturn(transaction);
        when(transactionRequest.getRequestInfo()).thenReturn(requestInfo);
        when(validator.skipGateway(any(Transaction.class))).thenReturn(true);
        when(appProperties.getSaveTxnTopic()).thenReturn("save-txn-topic");
        when(appProperties.getSaveTxnDumpTopic()).thenReturn("save-txn-dump-topic");

        // When
        Transaction result = transactionService.initiateTransaction(transactionRequest);

        // Then
        verify(validator).validateCreateTxn(transactionRequest);
        verify(enrichmentService).enrichCreateTransaction(transactionRequest);
        verify(validator).skipGateway(transaction);
        verify(transaction).setTxnStatus(Transaction.TxnStatusEnum.SUCCESS);
        verify(producer).push(eq("save-txn-topic"), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer).push(eq("save-txn-dump-topic"), any(TransactionDumpRequest.class));
        assertEquals(transaction, result);
    }

    @Test
    public void testGetTransactions_Success() {
        // Given
        TransactionCriteria transactionCriteria = mock(TransactionCriteria.class);
        List<Transaction> expectedTransactions = Arrays.asList(mock(Transaction.class), mock(Transaction.class));

        when(transactionRepository.fetchTransactions(transactionCriteria)).thenReturn(expectedTransactions);

        // When
        List<Transaction> result = transactionService.getTransactions(transactionCriteria);

        // Then
        assertEquals(expectedTransactions, result);
    }

    @Test(expected = CustomException.class)
    public void testGetTransactions_DataAccessException() {
        // Given
        TransactionCriteria transactionCriteria = mock(TransactionCriteria.class);

        when(transactionRepository.fetchTransactions(transactionCriteria)).thenThrow(new org.springframework.dao.DataAccessException("DB Error") {});

        // When & Then
        transactionService.getTransactions(transactionCriteria);
    }

    @Test
    public void testUpdateTransaction_NormalFlow() {
        // Given
        RequestInfo requestInfo = mock(RequestInfo.class);
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("txnId", "TXN123");
        Transaction currentTxnStatus = mock(Transaction.class);
        Transaction newTxn = mock(Transaction.class);
        TransactionDump dump = mock(TransactionDump.class);

        when(validator.validateUpdateTxn(requestParams)).thenReturn(currentTxnStatus);
        when(validator.skipGateway(currentTxnStatus)).thenReturn(false);
        when(gatewayService.getLiveStatus(currentTxnStatus, requestParams)).thenReturn(newTxn);
        when(newTxn.getResponseJson()).thenReturn("{\"status\":\"SUCCESS\"}");
        when(appProperties.getUpdateTxnTopic()).thenReturn("update-txn-topic");
        when(appProperties.getUpdateTxnDumpTopic()).thenReturn("update-txn-dump-topic");

        // When
        List<Transaction> result = transactionService.updateTransaction(requestInfo, requestParams);

        // Then
        verify(validator).validateUpdateTxn(requestParams);
        verify(validator).skipGateway(currentTxnStatus);
        verify(gatewayService).getLiveStatus(currentTxnStatus, requestParams);
        verify(enrichmentService).enrichUpdateTransaction(any(TransactionRequest.class), eq(newTxn));
        verify(validator).shouldGenerateReceipt(currentTxnStatus, newTxn);
        verify(producer).push(eq("update-txn-topic"), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer).push(eq("update-txn-dump-topic"), any(TransactionDumpRequest.class));
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(newTxn, result.get(0));
    }

    @Test
    public void testUpdateTransaction_SkipGateway() {
        // Given
        RequestInfo requestInfo = mock(RequestInfo.class);
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("txnId", "TXN123");
        Transaction currentTxnStatus = mock(Transaction.class);

        when(validator.validateUpdateTxn(requestParams)).thenReturn(currentTxnStatus);
        when(validator.skipGateway(currentTxnStatus)).thenReturn(true);

        // When
        List<Transaction> result = transactionService.updateTransaction(requestInfo, requestParams);

        // Then
        verify(validator).validateUpdateTxn(requestParams);
        verify(validator).skipGateway(currentTxnStatus);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(currentTxnStatus, result.get(0));
    }

    @Test
    public void testUpdateTransaction_GenerateReceipt_Success() {
        // Given
        RequestInfo requestInfo = mock(RequestInfo.class);
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("txnId", "TXN123");
        Transaction currentTxnStatus = mock(Transaction.class);
        Transaction newTxn = mock(Transaction.class);
        Receipt receipt = mock(Receipt.class);
        List<Receipt> receipts = Arrays.asList(receipt);

        when(validator.validateUpdateTxn(requestParams)).thenReturn(currentTxnStatus);
        when(validator.skipGateway(currentTxnStatus)).thenReturn(false);
        when(gatewayService.getLiveStatus(currentTxnStatus, requestParams)).thenReturn(newTxn);
        when(newTxn.getResponseJson()).thenReturn("{\"status\":\"SUCCESS\"}");
        when(validator.shouldGenerateReceipt(currentTxnStatus, newTxn)).thenReturn(true);
        when(collectionService.generateReceipt(requestInfo, newTxn)).thenReturn(receipts);
        when(receipt.getBill()).thenReturn(Collections.emptyList());
        when(appProperties.getUpdateTxnTopic()).thenReturn("update-txn-topic");
        when(appProperties.getUpdateTxnDumpTopic()).thenReturn("update-txn-dump-topic");

        // When
        List<Transaction> result = transactionService.updateTransaction(requestInfo, requestParams);

        // Then
        verify(validator).validateUpdateTxn(requestParams);
        verify(validator).shouldGenerateReceipt(currentTxnStatus, newTxn);
        verify(collectionService).generateReceipt(requestInfo, newTxn);
        verify(producer).push(eq("update-txn-topic"), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer).push(eq("update-txn-dump-topic"), any(TransactionDumpRequest.class));
    }

    @Test
    public void testUpdateTransaction_GenerateReceipt_Exception() {
        // Given
        RequestInfo requestInfo = mock(RequestInfo.class);
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("txnId", "TXN123");
        Transaction currentTxnStatus = mock(Transaction.class);
        Transaction newTxn = mock(Transaction.class);

        when(validator.validateUpdateTxn(requestParams)).thenReturn(currentTxnStatus);
        when(validator.skipGateway(currentTxnStatus)).thenReturn(false);
        when(gatewayService.getLiveStatus(currentTxnStatus, requestParams)).thenReturn(newTxn);
        when(newTxn.getResponseJson()).thenReturn("{\"status\":\"SUCCESS\"}");
        when(validator.shouldGenerateReceipt(currentTxnStatus, newTxn)).thenReturn(true);
        when(collectionService.generateReceipt(requestInfo, newTxn))
                .thenThrow(new CustomException("RECEIPT_GENERATION_FAILED", "Failed to generate receipt"));

        // When
        List<Transaction> result = transactionService.updateTransaction(requestInfo, requestParams);

        // Then
        verify(validator).validateUpdateTxn(requestParams);
        verify(validator).shouldGenerateReceipt(currentTxnStatus, newTxn);
        verify(collectionService).generateReceipt(requestInfo, newTxn);
        verify(newTxn).setTxnStatusMsg(PgConstants.TXN_RECEIPT_GEN_FAILED);
    }
}
