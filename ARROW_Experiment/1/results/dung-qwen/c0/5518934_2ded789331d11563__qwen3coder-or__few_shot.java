package de.kopis.glacier.commands;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.glacier.AmazonGlacier;
import com.amazonaws.services.glacier.model.CreateVaultRequest;
import com.amazonaws.services.glacier.model.CreateVaultResult;
import com.amazonaws.services.glacier.model.DescribeVaultRequest;
import com.amazonaws.services.glacier.model.DescribeVaultResult;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sqs.AmazonSQS;
import de.kopis.glacier.parsers.GlacierUploaderOptionParser;
import de.kopis.glacier.printers.VaultPrinter;
import joptsimple.OptionSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class CreateVaultCommand_ArrowTest {

    @Mock
    private AmazonGlacier mockClient;

    @Mock
    private AmazonSQS mockSqs;

    @Mock
    private AmazonSNS mockSns;

    @Mock
    private VaultPrinter mockPrinter;

    @Mock
    private OutputStream mockOut;

    @Mock
    private OptionSet mockOptions;

    @Mock
    private GlacierUploaderOptionParser mockOptionParser;

    private CreateVaultCommand command;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        command = new CreateVaultCommand(mockClient, mockSqs, mockSns, mockPrinter, mockOut);
    }

    @Test
    public void testExecShouldCreateAndDescribeVault() {
        String vaultName = "test-vault";
        when(mockOptions.valueOf(mockOptionParser.vault)).thenReturn(vaultName);
        
        CreateVaultResult createResult = new CreateVaultResult();
        DescribeVaultResult describeResult = new DescribeVaultResult();
        
        when(mockClient.createVault(any(CreateVaultRequest.class))).thenReturn(createResult);
        when(mockClient.describeVault(any(DescribeVaultRequest.class))).thenReturn(describeResult);
        
        command.exec(mockOptions, mockOptionParser);
        
        verify(mockClient).createVault(any(CreateVaultRequest.class));
        verify(mockClient).describeVault(any(DescribeVaultRequest.class));
        verify(mockPrinter).printVault(describeResult, mockOut);
    }

    @Test
    public void testExecShouldHandleAmazonClientException() {
        String vaultName = "test-vault";
        when(mockOptions.valueOf(mockOptionParser.vault)).thenReturn(vaultName);
        
        when(mockClient.createVault(any(CreateVaultRequest.class)))
                .thenThrow(new AmazonClientException("Test exception"));
        
        command.exec(mockOptions, mockOptionParser);
        
        verify(mockClient).createVault(any(CreateVaultRequest.class));
        verify(mockClient, never()).describeVault(any(DescribeVaultRequest.class));
    }

    @Test
    public void testValidShouldReturnTrueWhenCreateVaultOptionIsPresent() {
        when(mockOptions.has(mockOptionParser.createVault)).thenReturn(true);
        
        boolean result = command.valid(mockOptions, mockOptionParser);
        
        assert(result);
    }

    @Test
    public void testValidShouldReturnFalseWhenCreateVaultOptionIsNotPresent() {
        when(mockOptions.has(mockOptionParser.createVault)).thenReturn(false);
        
        boolean result = command.valid(mockOptions, mockOptionParser);
        
        assert(!result);
    }

    @Test
    public void testCreateVaultShouldThrowExceptionWhenVaultNameIsNull() {
        try {
            command.createVault(null);
            assert(false); // Should not reach here
        } catch (IllegalArgumentException e) {
            assert(true); // Expected
        }
    }

    @Test
    public void testCreateVaultShouldCallClientMethods() {
        String vaultName = "test-vault";
        CreateVaultResult createResult = new CreateVaultResult();
        DescribeVaultResult describeResult = new DescribeVaultResult();
        
        when(mockClient.createVault(any(CreateVaultRequest.class))).thenReturn(createResult);
        when(mockClient.describeVault(any(DescribeVaultRequest.class))).thenReturn(describeResult);
        
        command.createVault(vaultName);
        
        verify(mockClient).createVault(any(CreateVaultRequest.class));
        verify(mockClient).describeVault(any(DescribeVaultRequest.class));
        verify(mockPrinter).printVault(describeResult, mockOut);
    }
}
