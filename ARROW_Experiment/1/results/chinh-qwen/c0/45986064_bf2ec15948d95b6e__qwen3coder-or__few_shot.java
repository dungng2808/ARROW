package com.textocat.textokit.commons.consumer;

import com.google.common.base.Function;
import com.textocat.textokit.commons.DocumentMetadata;
import com.textocat.textokit.commons.io.IoUtils;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Path;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultSourceURI2OutputFilePathFunction_ArrowTest {

    @Test
    public void testApplyWithNullSourceUri() {
        DocumentMetadata metaAnno = mock(DocumentMetadata.class);
        when(metaAnno.getSourceUri()).thenReturn(null);
        
        Function<DocumentMetadata, Path> function = new DefaultSourceURI2OutputFilePathFunction();
        Path result = function.apply(metaAnno);
        
        assertNull(result);
    }

    @Test
    public void testApplyWithValidSourceUri() {
        DocumentMetadata metaAnno = mock(DocumentMetadata.class);
        when(metaAnno.getSourceUri()).thenReturn("file:///path/to/file.txt");
        
        Function<DocumentMetadata, Path> function = new DefaultSourceURI2OutputFilePathFunction();
        Path result = function.apply(metaAnno);
        
        // We can't directly verify the path content since IoUtils.extractPathFromURI is a static method
        // but we can verify that it doesn't return null
        assertNotNull(result);
    }

    @Test
    public void testApplyWithOpaqueUri() {
        DocumentMetadata metaAnno = mock(DocumentMetadata.class);
        when(metaAnno.getSourceUri()).thenReturn("mailto:user@example.com");
        
        Function<DocumentMetadata, Path> function = new DefaultSourceURI2OutputFilePathFunction();
        Path result = function.apply(metaAnno);
        
        // For opaque URIs, IoUtils.extractPathFromURI should handle them appropriately
        // We just ensure it doesn't throw and returns a non-null result
        assertNotNull(result);
    }

    // Helper method to avoid compilation issues with JUnit 4
    private void assertNotNull(Object obj) {
        if (obj == null) {
            throw new AssertionError("Expected non-null value");
        }
    }
}
