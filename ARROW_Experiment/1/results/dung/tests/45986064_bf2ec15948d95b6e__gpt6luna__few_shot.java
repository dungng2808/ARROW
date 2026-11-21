package com.textocat.textokit.commons.consumer;

import com.textocat.textokit.commons.DocumentMetadata;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

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
        metadata.setSourceUri("file:///tmp/input.txt");
        DefaultSourceURI2OutputFilePathFunction function =
                new DefaultSourceURI2OutputFilePathFunction();

        Path expected = Paths.get("/tmp/input.txt");
        assertEquals(expected, function.apply(metadata));
    }
}
