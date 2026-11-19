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
    public void testExec_CallsCreateVaultWithCorrectVaultName() throws Exception {
        String vaultName = "test-vault";
        when(mockOptions.valueOf(mockOptionParser.vault)).thenReturn(vaultName);

        command.exec(mockOptions, mockOptionParser);

        verify(mockClient).createVault(any(CreateVaultRequest.class));
    }

    @Test
    public void testExec_CallsDescribeVaultAfterCreate() throws Exception {
        String vaultName = "test-vault";
        when(mockOptions.valueOf(mockOptionParser.vault)).thenReturn(vaultName);

        command.exec(mockOptions, mockOptionParser);

        verify(mockClient).describeVault(any(DescribeVaultRequest.class));
    }

    @Test
    public void testExec_CallsPrinterWithDescribeVaultResult() throws Exception {
        String vaultName = "test-vault";
        when(mockOptions.valueOf(mockOptionParser.vault)).thenReturn(vaultName);
        DescribeVaultResult mockResult = mock(DescribeVaultResult.class);
        when(mockClient.describeVault(any(DescribeVaultRequest.class))).thenReturn(mockResult);

        command.exec(mockOptions, mockOptionParser);

        verify(mockPrinter).printVault(mockResult, mockOut);
    }

    @Test
    public void testValid_ReturnsTrueWhenCreateVaultOptionIsPresent() {
        when(mockOptions.has(mockOptionParser.createVault)).thenReturn(true);

        boolean result = command.valid(mockOptions, mockOptionParser);

        assert result;
    }

    @Test
    public void testValid_ReturnsFalseWhenCreateVaultOptionIsNotPresent() {
        when(mockOptions.has(mockOptionParser.createVault)).thenReturn(false);

        boolean result = command.valid(mockOptions, mockOptionParser);

        assert !result;
    }

    @Test
    public void testCreateVault_ThrowsAmazonClientException() throws Exception {
        String vaultName = "test-vault";
        doThrow(new AmazonClientException("Test exception")).when(mockClient).createVault(any(CreateVaultRequest.class));

        command.exec(mockOptions, mockOptionParser);
    }
}
