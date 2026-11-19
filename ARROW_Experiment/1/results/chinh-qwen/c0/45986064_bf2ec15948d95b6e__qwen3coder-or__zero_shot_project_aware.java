package com.textocat.textokit.commons.consumer;

import com.google.common.base.Function;
import com.textocat.textokit.commons.DocumentMetadata;
import com.textocat.textokit.commons.io.IoUtils;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DefaultSourceURI2OutputFilePathFunction_ArrowTest {

    @Test
    public void testApplyWithNullSourceUri() {
        // Given
        DocumentMetadata metaAnno = new DocumentMetadata() {
            @Override
            public String getSourceUri() {
                return null;
            }

            @Override
            public String getDocumentId() {
                return null;
            }
        };

        Function<DocumentMetadata, Path> function = new DefaultSourceURI2OutputFilePathFunction();

        // When
        Path result = function.apply(metaAnno);

        // Then
        assertNull(result);
    }

    @Test
    public void testApplyWithValidSourceUri() {
        // Given
        String uriString = "file:///path/to/document.txt";
        DocumentMetadata metaAnno = new DocumentMetadata() {
            @Override
            public String getSourceUri() {
                return uriString;
            }

            @Override
            public String getDocumentId() {
                return null;
            }
        };

        Function<DocumentMetadata, Path> function = new DefaultSourceURI2OutputFilePathFunction();

        // When
        Path result = function.apply(metaAnno);

        // Then
        assertEquals(URI.create(uriString).getPath(), result.toString());
    }

    @Test
    public void testApplyWithFileUri() {
        // Given
        String uriString = "file:/home/user/document.xml";
        DocumentMetadata metaAnno = new DocumentMetadata() {
            @Override
            public String getSourceUri() {
                return uriString;
            }

            @Override
            public String getDocumentId() {
                return null;
            }
        };

        Function<DocumentMetadata, Path> function = new DefaultSourceURI2OutputFilePathFunction();

        // When
        Path result = function.apply(metaAnno);

        // Then
        assertEquals(URI.create(uriString).getPath(), result.toString());
    }

    @Test
    public void testApplyWithComplexUri() {
        // Given
        String uriString = "file:///var/lib/documents/subdir/file-with-dashes_123.txt";
        DocumentMetadata metaAnno = new DocumentMetadata() {
            @Override
            public String getSourceUri() {
                return uriString;
            }

            @Override
            public String getDocumentId() {
                return null;
            }
        };

        Function<DocumentMetadata, Path> function = new DefaultSourceURI2OutputFilePathFunction();

        // When
        Path result = function.apply(metaAnno);

        // Then
        assertEquals(URI.create(uriString).getPath(), result.toString());
    }
}
