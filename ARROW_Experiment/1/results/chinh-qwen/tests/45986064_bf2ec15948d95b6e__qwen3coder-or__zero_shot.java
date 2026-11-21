package com.textocat.textokit.commons.consumer;

import com.google.common.base.Function;
import com.textocat.textokit.commons.DocumentMetadata;
import com.textocat.textokit.commons.io.IoUtils;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class DefaultSourceURI2OutputFilePathFunction_ArrowTest {

    @Test
    public void testApplyWithNullSourceUri() {
        DocumentMetadata metaAnno = new DocumentMetadata() {
            @Override
            public String getSourceUri() {
                return null;
            }

            @Override
            public String getId() {
                return null;
            }
        };

        DefaultSourceURI2OutputFilePathFunction function = new DefaultSourceURI2OutputFilePathFunction();
        Path result = function.apply(metaAnno);
        assertNull(result);
    }

    @Test
    public void testApplyWithValidSourceUri() {
        final String uriStr = "file:///path/to/file.txt";
        DocumentMetadata metaAnno = new DocumentMetadata() {
            @Override
            public String getSourceUri() {
                return uriStr;
            }

            @Override
            public String getId() {
                return null;
            }
        };

        DefaultSourceURI2OutputFilePathFunction function = new DefaultSourceURI2OutputFilePathFunction();
        Path result = function.apply(metaAnno);
        
        // We expect the path part of the URI to be returned
        Path expectedPath = IoUtils.extractPathFromURI(uriStr);
        assertEquals(expectedPath, result);
    }

    @Test
    public void testImplementsFunctionInterface() {
        DefaultSourceURI2OutputFilePathFunction function = new DefaultSourceURI2OutputFilePathFunction();
        assertTrue(Function.class.isAssignableFrom(DefaultSourceURI2OutputFilePathFunction.class));
    }
}
