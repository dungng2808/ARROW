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
import org.mockito.*;

import java.net.URI;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TransactionService_ArrowTest {

    @InjectMocks
    private TransactionService transactionService;

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

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInitiateTransaction_WithSkipGateway() {
        TransactionRequest transactionRequest = new TransactionRequest();
        RequestInfo requestInfo = new RequestInfo();
        transactionRequest.setRequestInfo(requestInfo);
        Transaction transaction = new Transaction();
        transaction.setTxnId("TXN123");
        transactionRequest.setTransaction(transaction);

        when(validator.validateCreateTxn(transactionRequest)).thenReturn(true);
        when(validator.skipGateway(transaction)).thenReturn(true);
        doNothing().when(enrichmentService).enrichCreateTransaction(transactionRequest);
        doNothing().when(producer).push(anyString(), any(org.egov.pg.models.TransactionRequest.class));
        doNothing().when(producer).push(anyString(), any(TransactionDumpRequest.class));

        Transaction result = transactionService.initiateTransaction(transactionRequest);

        assertNotNull(result);
        assertEquals(Transaction.TxnStatusEnum.SUCCESS, result.getTxnStatus());
        verify(validator).validateCreateTxn(transactionRequest);
        verify(enrichmentService).enrichCreateTransaction(transactionRequest);
        verify(producer).push(anyString(), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer).push(anyString(), any(TransactionDumpRequest.class));
    }

    @Test
    public void testInitiateTransaction_WithoutSkipGateway() {
        TransactionRequest transactionRequest = new TransactionRequest();
        RequestInfo requestInfo = new RequestInfo();
        transactionRequest.setRequestInfo(requestInfo);
        Transaction transaction = new Transaction();
        transaction.setTxnId("TXN123");
        transactionRequest.setTransaction(transaction);

        when(validator.validateCreateTxn(transactionRequest)).thenReturn(true);
        when(validator.skipGateway(transaction)).thenReturn(false);
        when(gatewayService.initiateTxn(transaction)).thenReturn(URI.create("http://example.com"));
        doNothing().when(enrichmentService).enrichCreateTransaction(transactionRequest);
        doNothing().when(producer).push(anyString(), any(org.egov.pg.models.TransactionRequest.class));
        doNothing().when(producer).push(anyString(), any(TransactionDumpRequest.class));

        Transaction result = transactionService.initiateTransaction(transactionRequest);

        assertNotNull(result);
        assertEquals("http://example.com", result.getRedirectUrl());
        verify(validator).validateCreateTxn(transactionRequest);
        verify(enrichmentService).enrichCreateTransaction(transactionRequest);
        verify(gatewayService).initiateTxn(transaction);
        verify(producer).push(anyString(), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer).push(anyString(), any(TransactionDumpRequest.class));
    }

    @Test
    public void testGetTransactions_Success() {
        TransactionCriteria transactionCriteria = new TransactionCriteria();
        List<Transaction> expectedTransactions = Arrays.asList(new Transaction(), new Transaction());

        when(transactionRepository.fetchTransactions(transactionCriteria)).thenReturn(expectedTransactions);

        List<Transaction> result = transactionService.getTransactions(transactionCriteria);

        assertEquals(expectedTransactions, result);
        verify(transactionRepository).fetchTransactions(transactionCriteria);
    }

    @Test(expected = CustomException.class)
    public void testGetTransactions_DataAccessException() {
        TransactionCriteria transactionCriteria = new TransactionCriteria();

        when(transactionRepository.fetchTransactions(transactionCriteria)).thenThrow(new org.springframework.dao.DataAccessException("DB Error") {});

        transactionService.getTransactions(transactionCriteria);
    }

    @Test
    public void testUpdateTransaction_WithSkipGateway() {
        RequestInfo requestInfo = new RequestInfo();
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("txnId", "TXN123");

        Transaction currentTxnStatus = new Transaction();
        currentTxnStatus.setTxnId("TXN123");
        currentTxnStatus.setTxnStatus(Transaction.TxnStatusEnum.PENDING);

        when(validator.validateUpdateTxn(requestParams)).thenReturn(currentTxnStatus);
        when(validator.skipGateway(currentTxnStatus)).thenReturn(true);
        doNothing().when(producer).push(anyString(), any(org.egov.pg.models.TransactionRequest.class));
        doNothing().when(producer).push(anyString(), any(TransactionDumpRequest.class));

        List<Transaction> result = transactionService.updateTransaction(requestInfo, requestParams);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(currentTxnStatus, result.get(0));
        verify(validator).validateUpdateTxn(requestParams);
        verify(validator).skipGateway(currentTxnStatus);
        verify(producer).push(anyString(), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer).push(anyString(), any(TransactionDumpRequest.class));
    }

    @Test
    public void testUpdateTransaction_WithoutSkipGateway_Success() {
        RequestInfo requestInfo = new RequestInfo();
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("txnId", "TXN123");

        Transaction currentTxnStatus = new Transaction();
        currentTxnStatus.setTxnId("TXN123");
        currentTxnStatus.setTxnStatus(Transaction.TxnStatusEnum.PENDING);

        Transaction newTxn = new Transaction();
        newTxn.setTxnId("TXN123");
        newTxn.setTxnStatus(Transaction.TxnStatusEnum.SUCCESS);
        newTxn.setResponseJson("{\"status\":\"SUCCESS\"}");

        when(validator.validateUpdateTxn(requestParams)).thenReturn(currentTxnStatus);
        when(validator.skipGateway(currentTxnStatus)).thenReturn(false);
        when(gatewayService.getLiveStatus(currentTxnStatus, requestParams)).thenReturn(newTxn);
        doNothing().when(enrichmentService).enrichUpdateTransaction(any(TransactionRequest.class), any(Transaction.class));
        when(validator.shouldGenerateReceipt(currentTxnStatus, newTxn)).thenReturn(true);
        when(collectionService.generateReceipt(requestInfo, newTxn)).thenReturn(Arrays.asList(new Receipt()));
        doNothing().when(producer).push(anyString(), any(org.egov.pg.models.TransactionRequest.class));
        doNothing().when(producer).push(anyString(), any(TransactionDumpRequest.class));

        List<Transaction> result = transactionService.updateTransaction(requestInfo, requestParams);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(newTxn, result.get(0));
        verify(validator).validateUpdateTxn(requestParams);
        verify(validator).skipGateway(currentTxnStatus);
        verify(gatewayService).getLiveStatus(currentTxnStatus, requestParams);
        verify(enrichmentService).enrichUpdateTransaction(any(TransactionRequest.class), any(Transaction.class));
        verify(validator).shouldGenerateReceipt(currentTxnStatus, newTxn);
        verify(collectionService).generateReceipt(requestInfo, newTxn);
        verify(producer).push(anyString(), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer).push(anyString(), any(TransactionDumpRequest.class));
    }

    @Test
    public void testUpdateTransaction_WithoutSkipGateway_Failure() {
        RequestInfo requestInfo = new RequestInfo();
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("txnId", "TXN123");

        Transaction currentTxnStatus = new Transaction();
        currentTxnStatus.setTxnId("TXN123");
        currentTxnStatus.setTxnStatus(Transaction.TxnStatusEnum.PENDING);

        Transaction newTxn = new Transaction();
        newTxn.setTxnId("TXN123");
        newTxn.setTxnStatus(Transaction.TxnStatusEnum.FAILED);
        newTxn.setResponseJson("{\"status\":\"FAILED\"}");

        when(validator.validateUpdateTxn(requestParams)).thenReturn(currentTxnStatus);
        when(validator.skipGateway(currentTxnStatus)).thenReturn(false);
        when(gatewayService.getLiveStatus(currentTxnStatus, requestParams)).thenReturn(newTxn);
        doNothing().when(enrichmentService).enrichUpdateTransaction(any(TransactionRequest.class), any(Transaction.class));
        when(validator.shouldGenerateReceipt(currentTxnStatus, newTxn)).thenReturn(false);
        doNothing().when(producer).push(anyString(), any(org.egov.pg.models.TransactionRequest.class));
        doNothing().when(producer).push(anyString(), any(TransactionDumpRequest.class));

        List<Transaction> result = transactionService.updateTransaction(requestInfo, requestParams);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(newTxn, result.get(0));
        verify(validator).validateUpdateTxn(requestParams);
        verify(validator).skipGateway(currentTxnStatus);
        verify(gatewayService).getLiveStatus(currentTxnStatus, requestParams);
        verify(enrichmentService).enrichUpdateTransaction(any(TransactionRequest.class), any(Transaction.class));
        verify(validator).shouldGenerateReceipt(currentTxnStatus, newTxn);
        verify(producer).push(anyString(), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer).push(anyString(), any(TransactionDumpRequest.class));
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
        verify(collectionService).generateReceipt(requestInfo, transaction);
    }

    @Test
    public void testGenerateReceipt_CustomException() {
        RequestInfo requestInfo = new RequestInfo();
        Transaction transaction = new Transaction();
        transaction.setTxnId("TXN123");

        when(collectionService.generateReceipt(requestInfo, transaction)).thenThrow(new CustomException("ERROR", "Error"));

        transactionService.generateReceipt(requestInfo, transaction);

        assertEquals(PgConstants.TXN_RECEIPT_GEN_FAILED, transaction.getTxnStatusMsg());
        verify(collectionService).generateReceipt(requestInfo, transaction);
    }

    @Test
    public void testGenerateReceipt_ServiceCallException() {
        RequestInfo requestInfo = new RequestInfo();
        Transaction transaction = new Transaction();
        transaction.setTxnId("TXN123");

        when(collectionService.generateReceipt(requestInfo, transaction)).thenThrow(new ServiceCallException("ERROR"));

        transactionService.generateReceipt(requestInfo, transaction);

        assertEquals(PgConstants.TXN_RECEIPT_GEN_FAILED, transaction.getTxnStatusMsg());
        verify(collectionService).generateReceipt(requestInfo, transaction);
    }
}
