package com.textocat.textokit.commons.consumer;

import com.textocat.textokit.commons.DocumentMetadata;
import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DefaultSourceURI2OutputFilePathFunction_ArrowTest {

    @Test
    public void applyReturnsNullWhenSourceUriIsNull() {
        DocumentMetadata metadata = new DocumentMetadata();
        DefaultSourceURI2OutputFilePathFunction function =
                new DefaultSourceURI2OutputFilePathFunction();

        assertNull(function.apply(metadata));
    }

    @Test
    public void applyExtractsPathFromSourceUri() {
        DocumentMetadata metadata = new DocumentMetadata();
        metadata.setSourceUri("file:///tmp/document.txt");
        DefaultSourceURI2OutputFilePathFunction function =
                new DefaultSourceURI2OutputFilePathFunction();

        Path result = function.apply(metadata);

        assertEquals(java.nio.file.Paths.get("/tmp/document.txt"), result);
    }
}
