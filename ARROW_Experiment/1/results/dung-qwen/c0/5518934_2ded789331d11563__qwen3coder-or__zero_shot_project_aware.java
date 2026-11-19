package de.kopis.glacier.commands;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;

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

public class CreateVaultCommand_ArrowTest {

    private AmazonGlacier mockClient;
    private AmazonSQS mockSqs;
    private AmazonSNS mockSns;
    private VaultPrinter mockPrinter;
    private OutputStream mockOut;
    private CreateVaultCommand command;
    private ByteArrayOutputStream capturedOutput;

    @Before
    public void setUp() {
        mockClient = createMock(AmazonGlacier.class);
        mockSqs = createMock(AmazonSQS.class);
        mockSns = createMock(AmazonSNS.class);
        mockPrinter = createMock(VaultPrinter.class);
        capturedOutput = new ByteArrayOutputStream();
        mockOut = capturedOutput;
        
        command = new CreateVaultCommand(mockClient, mockSqs, mockSns, mockPrinter, mockOut);
    }

    @Test
    public void testExec_CallsCreateVaultWithCorrectVaultName() throws Exception {
        final String vaultName = "test-vault";
        OptionSet mockOptions = createMock(OptionSet.class);
        GlacierUploaderOptionParser mockParser = createMock(GlacierUploaderOptionParser.class);
        
        expect(mockOptions.valueOf(mockParser.vault)).andReturn(vaultName);
        replay(mockOptions);
        replay(mockParser);
        
        CreateVaultResult mockResult = createMock(CreateVaultResult.class);
        expect(mockClient.createVault(isA(CreateVaultRequest.class))).andAnswer(() -> {
            CreateVaultRequest req = (CreateVaultRequest) getCurrentArguments()[0];
            assertEquals(vaultName, req.getVaultName());
            return mockResult;
        });
        
        DescribeVaultResult mockDescribeResult = createMock(DescribeVaultResult.class);
        expect(mockClient.describeVault(isA(DescribeVaultRequest.class))).andAnswer(() -> {
            DescribeVaultRequest req = (DescribeVaultRequest) getCurrentArguments()[0];
            assertEquals(vaultName, req.getVaultName());
            return mockDescribeResult;
        });
        
        mockPrinter.printVault(mockDescribeResult, mockOut);
        replay(mockClient);
        replay(mockDescribeResult);
        replay(mockPrinter);
        
        command.exec(mockOptions, mockParser);
        
        verify(mockOptions);
        verify(mockParser);
        verify(mockClient);
        verify(mockDescribeResult);
        verify(mockPrinter);
    }

    @Test
    public void testValid_ReturnsTrueWhenCreateVaultOptionIsPresent() {
        OptionSet mockOptions = createMock(OptionSet.class);
        GlacierUploaderOptionParser mockParser = createMock(GlacierUploaderOptionParser.class);
        
        expect(mockOptions.has(mockParser.createVault)).andReturn(true);
        replay(mockOptions);
        replay(mockParser);
        
        boolean result = command.valid(mockOptions, mockParser);
        
        assertTrue(result);
        verify(mockOptions);
        verify(mockParser);
    }

    @Test
    public void testValid_ReturnsFalseWhenCreateVaultOptionIsNotPresent() {
        OptionSet mockOptions = createMock(OptionSet.class);
        GlacierUploaderOptionParser mockParser = createMock(GlacierUploaderOptionParser.class);
        
        expect(mockOptions.has(mockParser.createVault)).andReturn(false);
        replay(mockOptions);
        replay(mockParser);
        
        boolean result = command.valid(mockOptions, mockParser);
        
        assertFalse(result);
        verify(mockOptions);
        verify(mockParser);
    }

    @Test
    public void testCreateVault_ThrowsAmazonClientException() throws Exception {
        final String vaultName = "test-vault";
        
        expect(mockClient.createVault(isA(CreateVaultRequest.class))).andThrow(new AmazonClientException("Test exception"));
        replay(mockClient);
        
        command.createVault(vaultName);
        
        verify(mockClient);
    }

    @Test
    public void testCreateVault_NullVaultName_ThrowsIllegalArgumentException() {
        try {
            command.createVault(null);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("vaultName can not be null", e.getMessage());
        }
    }
}
