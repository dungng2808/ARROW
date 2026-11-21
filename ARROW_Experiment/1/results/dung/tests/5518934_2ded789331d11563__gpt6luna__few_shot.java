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
import joptsimple.OptionSet;

public class CreateVaultCommand_ArrowTest {

    @Test
    public void validReturnsFalseWhenCreateVaultOptionIsAbsent() {
        AmazonGlacier glacier = null;
        AmazonSQS sqs = null;
        AmazonSNS sns = null;
        CreateVaultCommand command = new CreateVaultCommand(
                glacier, sqs, sns, new VaultPrinter(), new ByteArrayOutputStream());
        GlacierUploaderOptionParser parser = new GlacierUploaderOptionParser();
        OptionSet options = parser.parser.parse(new String[0]);

        assertFalse(command.valid(options, parser));
    }

    @Test
    public void validReturnsTrueWhenCreateVaultOptionIsPresent() {
        AmazonGlacier glacier = null;
        AmazonSQS sqs = null;
        AmazonSNS sns = null;
        CreateVaultCommand command = new CreateVaultCommand(
                glacier, sqs, sns, new VaultPrinter(), new ByteArrayOutputStream());
        GlacierUploaderOptionParser parser = new GlacierUploaderOptionParser();
        OptionSet options = parser.parser.parse(new String[] {"--create-vault"});

        assertTrue(command.valid(options, parser));
    }
}
