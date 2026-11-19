package de.kopis.glacier.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

import com.amazonaws.services.glacier.AmazonGlacier;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sqs.AmazonSQS;

import de.kopis.glacier.parsers.GlacierUploaderOptionParser;
import de.kopis.glacier.printers.VaultPrinter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class CreateVaultCommand_ArrowTest {

    @Test
    public void validReturnsFalseWhenCreateVaultOptionIsAbsent() {
        GlacierUploaderOptionParser optionParser = new GlacierUploaderOptionParser();
        OptionSet options = new OptionParser().parse();

        CreateVaultCommand command = new CreateVaultCommand(
                (AmazonGlacier) null,
                (AmazonSQS) null,
                (AmazonSNS) null,
                new VaultPrinter(),
                new ByteArrayOutputStream());

        assertFalse(command.valid(options, optionParser));
    }

    @Test
    public void validReturnsTrueWhenCreateVaultOptionIsPresent() {
        GlacierUploaderOptionParser optionParser = new GlacierUploaderOptionParser();
        OptionSet options = new OptionParser().parse(optionParser.createVault);

        CreateVaultCommand command = new CreateVaultCommand(
                (AmazonGlacier) null,
                (AmazonSQS) null,
                (AmazonSNS) null,
                new VaultPrinter(),
                new ByteArrayOutputStream());

        assertTrue(command.valid(options, optionParser));
    }
}
