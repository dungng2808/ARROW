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
    public void testInitiateTransaction_Success() throws Exception {
        // Setup
        TransactionRequest transactionRequest = new TransactionRequest();
        RequestInfo requestInfo = new RequestInfo();
        transactionRequest.setRequestInfo(requestInfo);
        Transaction transaction = new Transaction();
        transaction.setTxnId("TXN123");
        transactionRequest.setTransaction(transaction);

        when(validator.validateCreateTxn(transactionRequest)).thenReturn(true);
        when(enrichmentService.enrichCreateTransaction(transactionRequest)).thenReturn(transaction);
        when(gatewayService.initiateTxn(transaction)).thenReturn(URI.create("http://gateway.com/pay"));
        when(appProperties.getSaveTxnTopic()).thenReturn("save-txn-topic");
        when(appProperties.getSaveTxnDumpTopic()).thenReturn("save-txn-dump-topic");

        // Execute
        Transaction result = transactionService.initiateTransaction(transactionRequest);

        // Verify
        assertEquals("TXN123", result.getTxnId());
        assertNotNull(result.getRedirectUrl());
        verify(producer).push(eq("save-txn-topic"), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer).push(eq("save-txn-dump-topic"), any(TransactionDumpRequest.class));
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

        when(validator.validateCreateTxn(transactionRequest)).thenReturn(true);
        when(enrichmentService.enrichCreateTransaction(transactionRequest)).thenReturn(transaction);
        when(validator.skipGateway(transaction)).thenReturn(true);
        when(appProperties.getSaveTxnTopic()).thenReturn("save-txn-topic");
        when(appProperties.getSaveTxnDumpTopic()).thenReturn("save-txn-dump-topic");

        // Execute
        Transaction result = transactionService.initiateTransaction(transactionRequest);

        // Verify
        assertEquals("TXN123", result.getTxnId());
        assertEquals(Transaction.TxnStatusEnum.SUCCESS, result.getTxnStatus());
        verify(producer).push(eq("save-txn-topic"), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer).push(eq("save-txn-dump-topic"), any(TransactionDumpRequest.class));
    }

    @Test
    public void testGetTransactions_Success() throws Exception {
        // Setup
        TransactionCriteria transactionCriteria = new TransactionCriteria();
        List<Transaction> expectedTransactions = Arrays.asList(new Transaction(), new Transaction());
        when(transactionRepository.fetchTransactions(transactionCriteria)).thenReturn(expectedTransactions);

        // Execute
        List<Transaction> result = transactionService.getTransactions(transactionCriteria);

        // Verify
        assertEquals(expectedTransactions, result);
    }

    @Test(expected = CustomException.class)
    public void testGetTransactions_DataAccessException() throws Exception {
        // Setup
        TransactionCriteria transactionCriteria = new TransactionCriteria();
        when(transactionRepository.fetchTransactions(transactionCriteria)).thenThrow(new org.springframework.dao.DataAccessException("DB Error") {});

        // Execute
        transactionService.getTransactions(transactionCriteria);
    }

    @Test
    public void testUpdateTransaction_Success() throws Exception {
        // Setup
        RequestInfo requestInfo = new RequestInfo();
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("txnId", "TXN123");
        requestParams.put("status", "SUCCESS");

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
        when(enrichmentService.enrichUpdateTransaction(any(TransactionRequest.class), eq(newTxn))).thenReturn(newTxn);
        when(validator.shouldGenerateReceipt(currentTxnStatus, newTxn)).thenReturn(true);
        when(collectionService.generateReceipt(requestInfo, newTxn)).thenReturn(Arrays.asList(new Receipt()));
        when(appProperties.getUpdateTxnTopic()).thenReturn("update-txn-topic");
        when(appProperties.getUpdateTxnDumpTopic()).thenReturn("update-txn-dump-topic");

        // Execute
        List<Transaction> result = transactionService.updateTransaction(requestInfo, requestParams);

        // Verify
        assertEquals(1, result.size());
        assertEquals(Transaction.TxnStatusEnum.SUCCESS, result.get(0).getTxnStatus());
        verify(producer).push(eq("update-txn-topic"), any(org.egov.pg.models.TransactionRequest.class));
        verify(producer).push(eq("update-txn-dump-topic"), any(TransactionDumpRequest.class));
    }

    @Test
    public void testUpdateTransaction_SkipGateway() throws Exception {
        // Setup
        RequestInfo requestInfo = new RequestInfo();
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("txnId", "TXN123");

        Transaction currentTxnStatus = new Transaction();
        currentTxnStatus.setTxnId("TXN123");
        currentTxnStatus.setTxnStatus(Transaction.TxnStatusEnum.PENDING);

        when(validator.validateUpdateTxn(requestParams)).thenReturn(currentTxnStatus);
        when(validator.skipGateway(currentTxnStatus)).thenReturn(true);

        // Execute
        List<Transaction> result = transactionService.updateTransaction(requestInfo, requestParams);

        // Verify
        assertEquals(1, result.size());
        assertEquals(currentTxnStatus, result.get(0));
    }

    @Test
    public void testGenerateReceipt_Success() throws Exception {
        // Setup
        RequestInfo requestInfo = new RequestInfo();
        Transaction transaction = new Transaction();
        transaction.setTxnId("TXN123");

        Receipt receipt = new Receipt();
        receipt.setBill(Collections.singletonList(
                new org.egov.pg.models.Bill()
                        .billDetails(Collections.singletonList(
                                new org.egov.pg.models.BillDetails()
                                        .receiptNumber("REC123")
                        ))
        ));

        when(collectionService.generateReceipt(requestInfo, transaction)).thenReturn(Arrays.asList(receipt));

        // Execute
        transactionService.generateReceipt(requestInfo, transaction);

        // Verify
        assertEquals("REC123", transaction.getReceipt());
    }

    @Test
    public void testGenerateReceipt_Exception() throws Exception {
        // Setup
        RequestInfo requestInfo = new RequestInfo();
        Transaction transaction = new Transaction();
        transaction.setTxnId("TXN123");

        when(collectionService.generateReceipt(requestInfo, transaction))
                .thenThrow(new CustomException("GEN_RECEIPT_FAILED", "Failed to generate receipt"));

        // Execute
        transactionService.generateReceipt(requestInfo, transaction);

        // Verify
        assertEquals("TXN_RECEIPT_GEN_FAILED", transaction.getTxnStatusMsg());
    }
}
