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
    public void testInitiateTransaction_Success() throws Exception {
        // Setup
        RequestInfo requestInfo = new RequestInfo();
        Transaction transaction = new Transaction();
        transaction.setTxnId("TXN123");
        transaction.setTxnStatus(Transaction.TxnStatusEnum.PENDING);
        TransactionRequest transactionRequest = new TransactionRequest(requestInfo, transaction);

        TransactionDump dump = TransactionDump.builder()
                .txnId("TXN123")
                .auditDetails(transaction.getAuditDetails())
                .build();

        URI uri = URI.create("http://example.com/gateway");

        // Mock behavior
        when(validator.validateCreateTxn(transactionRequest)).thenReturn(true);
        when(validator.skipGateway(transaction)).thenReturn(false);
        when(gatewayService.initiateTxn(transaction)).thenReturn(uri);
        when(appProperties.getSaveTxnTopic()).thenReturn("save-txn-topic");
        when(appProperties.getSaveTxnDumpTopic()).thenReturn("save-txn-dump-topic");

        // Execute
        Transaction result = transactionService.initiateTransaction(transactionRequest);

        // Verify
        assertEquals("TXN123", result.getTxnId());
        assertEquals("http://example.com/gateway", result.getRedirectUrl());
        verify(validator).validateCreateTxn(transactionRequest);
        verify(enrichmentService).enrichCreateTransaction(transactionRequest);
        verify(gatewayService).initiateTxn(transaction);
        verify(producer).push("save-txn-topic", transactionRequest);
        verify(producer).push("save-txn-dump-topic", new TransactionDumpRequest(requestInfo, dump));
    }

    @Test
    public void testInitiateTransaction_SkipGateway() throws Exception {
        // Setup
        RequestInfo requestInfo = new RequestInfo();
        Transaction transaction = new Transaction();
        transaction.setTxnId("TXN123");
        transaction.setTxnStatus(Transaction.TxnStatusEnum.PENDING);
        TransactionRequest transactionRequest = new TransactionRequest(requestInfo, transaction);

        // Mock behavior
        when(validator.validateCreateTxn(transactionRequest)).thenReturn(true);
        when(validator.skipGateway(transaction)).thenReturn(true);
        when(appProperties.getSaveTxnTopic()).thenReturn("save-txn-topic");
        when(appProperties.getSaveTxnDumpTopic()).thenReturn("save-txn-dump-topic");

        // Execute
        Transaction result = transactionService.initiateTransaction(transactionRequest);

        // Verify
        assertEquals("TXN123", result.getTxnId());
        assertEquals(Transaction.TxnStatusEnum.SUCCESS, result.getTxnStatus());
        verify(validator).validateCreateTxn(transactionRequest);
        verify(enrichmentService).enrichCreateTransaction(transactionRequest);
        verify(validator).skipGateway(transaction);
        verify(producer).push("save-txn-topic", transactionRequest);
        verify(producer).push("save-txn-dump-topic", any(TransactionDumpRequest.class));
    }

    @Test
    public void testGetTransactions_Success() throws Exception {
        // Setup
        TransactionCriteria transactionCriteria = new TransactionCriteria();
        List<Transaction> expectedTransactions = Arrays.asList(new Transaction(), new Transaction());

        // Mock behavior
        when(transactionRepository.fetchTransactions(transactionCriteria)).thenReturn(expectedTransactions);

        // Execute
        List<Transaction> result = transactionService.getTransactions(transactionCriteria);

        // Verify
        assertEquals(expectedTransactions, result);
        verify(transactionRepository).fetchTransactions(transactionCriteria);
    }

    @Test(expected = CustomException.class)
    public void testGetTransactions_DataAccessException() throws Exception {
        // Setup
        TransactionCriteria transactionCriteria = new TransactionCriteria();

        // Mock behavior
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

        Transaction currentTxnStatus = new Transaction();
        currentTxnStatus.setTxnId("TXN123");
        currentTxnStatus.setTxnStatus(Transaction.TxnStatusEnum.PENDING);

        Transaction newTxn = new Transaction();
        newTxn.setTxnId("TXN123");
        newTxn.setTxnStatus(Transaction.TxnStatusEnum.SUCCESS);
        newTxn.setResponseJson("{\"status\": \"success\"}");

        TransactionDump dump = TransactionDump.builder()
                .txnId("TXN123")
                .txnResponse("{\"status\": \"success\"}")
                .auditDetails(newTxn.getAuditDetails())
                .build();

        // Mock behavior
        when(validator.validateUpdateTxn(requestParams)).thenReturn(currentTxnStatus);
        when(validator.skipGateway(currentTxnStatus)).thenReturn(false);
        when(gatewayService.getLiveStatus(currentTxnStatus, requestParams)).thenReturn(newTxn);
        when(validator.shouldGenerateReceipt(currentTxnStatus, newTxn)).thenReturn(true);
        when(collectionService.generateReceipt(requestInfo, newTxn)).thenReturn(Arrays.asList(new Receipt()));
        when(appProperties.getUpdateTxnTopic()).thenReturn("update-txn-topic");
        when(appProperties.getUpdateTxnDumpTopic()).thenReturn("update-txn-dump-topic");

        // Execute
        List<Transaction> result = transactionService.updateTransaction(requestInfo, requestParams);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TXN123", result.get(0).getTxnId());
        assertEquals(Transaction.TxnStatusEnum.SUCCESS, result.get(0).getTxnStatus());
        verify(validator).validateUpdateTxn(requestParams);
        verify(validator).skipGateway(currentTxnStatus);
        verify(gatewayService).getLiveStatus(currentTxnStatus, requestParams);
        verify(enrichmentService).enrichUpdateTransaction(any(TransactionRequest.class), eq(newTxn));
        verify(validator).shouldGenerateReceipt(currentTxnStatus, newTxn);
        verify(collectionService).generateReceipt(requestInfo, newTxn);
        verify(producer).push("update-txn-topic", new TransactionRequest(requestInfo, newTxn));
        verify(producer).push("update-txn-dump-topic", new TransactionDumpRequest(requestInfo, dump));
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

        // Mock behavior
        when(validator.validateUpdateTxn(requestParams)).thenReturn(currentTxnStatus);
        when(validator.skipGateway(currentTxnStatus)).thenReturn(true);

        // Execute
        List<Transaction> result = transactionService.updateTransaction(requestInfo, requestParams);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TXN123", result.get(0).getTxnId());
        assertEquals(Transaction.TxnStatusEnum.PENDING, result.get(0).getTxnStatus());
        verify(validator).validateUpdateTxn(requestParams);
        verify(validator).skipGateway(currentTxnStatus);
        verify(gatewayService, never()).getLiveStatus(any(Transaction.class), any(Map.class));
    }

    @Test
    public void testUpdateTransaction_NoReceiptGeneration() throws Exception {
        // Setup
        RequestInfo requestInfo = new RequestInfo();
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("txnId", "TXN123");

        Transaction currentTxnStatus = new Transaction();
        currentTxnStatus.setTxnId("TXN123");
        currentTxnStatus.setTxnStatus(Transaction.TxnStatusEnum.PENDING);

        Transaction newTxn = new Transaction();
        newTxn.setTxnId("TXN123");
        newTxn.setTxnStatus(Transaction.TxnStatusEnum.SUCCESS);
        newTxn.setResponseJson("{\"status\": \"success\"}");

        TransactionDump dump = TransactionDump.builder()
                .txnId("TXN123")
                .txnResponse("{\"status\": \"success\"}")
                .auditDetails(newTxn.getAuditDetails())
                .build();

        // Mock behavior
        when(validator.validateUpdateTxn(requestParams)).thenReturn(currentTxnStatus);
        when(validator.skipGateway(currentTxnStatus)).thenReturn(false);
        when(gatewayService.getLiveStatus(currentTxnStatus, requestParams)).thenReturn(newTxn);
        when(validator.shouldGenerateReceipt(currentTxnStatus, newTxn)).thenReturn(false);
        when(appProperties.getUpdateTxnTopic()).thenReturn("update-txn-topic");
        when(appProperties.getUpdateTxnDumpTopic()).thenReturn("update-txn-dump-topic");

        // Execute
        List<Transaction> result = transactionService.updateTransaction(requestInfo, requestParams);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TXN123", result.get(0).getTxnId());
        assertEquals(Transaction.TxnStatusEnum.SUCCESS, result.get(0).getTxnStatus());
        verify(validator).validateUpdateTxn(requestParams);
        verify(validator).skipGateway(currentTxnStatus);
        verify(gatewayService).getLiveStatus(currentTxnStatus, requestParams);
        verify(enrichmentService).enrichUpdateTransaction(any(TransactionRequest.class), eq(newTxn));
        verify(validator).shouldGenerateReceipt(currentTxnStatus, newTxn);
        verify(collectionService, never()).generateReceipt(any(RequestInfo.class), any(Transaction.class));
        verify(producer).push("update-txn-topic", new TransactionRequest(requestInfo, newTxn));
        verify(producer).push("update-txn-dump-topic", new TransactionDumpRequest(requestInfo, dump));
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

        // Mock behavior
        when(collectionService.generateReceipt(requestInfo, transaction)).thenReturn(Arrays.asList(receipt));

        // Execute
        transactionService.getClass().getDeclaredMethod("generateReceipt", RequestInfo.class, Transaction.class)
                .invoke(transactionService, requestInfo, transaction);

        // Verify
        assertEquals("REC123", transaction.getReceipt());
    }

    @Test
    public void testGenerateReceipt_CustomException() throws Exception {
        // Setup
        RequestInfo requestInfo = new RequestInfo();
        Transaction transaction = new Transaction();
        transaction.setTxnId("TXN123");

        // Mock behavior
        when(collectionService.generateReceipt(requestInfo, transaction))
                .thenThrow(new CustomException("GEN_REC_FAILED", "Failed to generate receipt"));

        // Execute
        transactionService.getClass().getDeclaredMethod("generateReceipt", RequestInfo.class, Transaction.class)
                .invoke(transactionService, requestInfo, transaction);

        // Verify
        assertEquals(PgConstants.TXN_RECEIPT_GEN_FAILED, transaction.getTxnStatusMsg());
    }

    @Test
    public void testGenerateReceipt_ServiceCallException() throws Exception {
        // Setup
        RequestInfo requestInfo = new RequestInfo();
        Transaction transaction = new Transaction();
        transaction.setTxnId("TXN123");

        // Mock behavior
        when(collectionService.generateReceipt(requestInfo, transaction))
                .thenThrow(new ServiceCallException("SERVICE_CALL_ERROR"));

        // Execute
        transactionService.getClass().getDeclaredMethod("generateReceipt", RequestInfo.class, Transaction.class)
                .invoke(transactionService, requestInfo, transaction);

        // Verify
        assertEquals(PgConstants.TXN_RECEIPT_GEN_FAILED, transaction.getTxnStatusMsg());
    }
}
