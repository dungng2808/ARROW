package com.github.hburgmeier.jerseyoauth2.protocol.impl.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.github.hburgmeier.jerseyoauth2.api.protocol.IHttpRequest;

public class HeaderExtractor_ArrowTest {

    @Test
    public void extractsHeaderValue() {
        IHttpRequest request = mock(IHttpRequest.class);
        when(request.getHeaderField("Authorization")).thenReturn("Bearer token");

        HeaderExtractor extractor = new HeaderExtractor("Authorization");

        assertEquals("Bearer token", extractor.extractValue(request));
    }

    @Test
    public void returnsNullForEmptyHeaderValue() {
        IHttpRequest request = mock(IHttpRequest.class);
        when(request.getHeaderField("Authorization")).thenReturn("");

        HeaderExtractor extractor = new HeaderExtractor("Authorization");

        assertNull(extractor.extractValue(request));
    }

    @Test
    public void returnsNullForNullHeaderValue() {
        IHttpRequest request = mock(IHttpRequest.class);
        when(request.getHeaderField("Authorization")).thenReturn(null);

        HeaderExtractor extractor = new HeaderExtractor("Authorization");

        assertNull(extractor.extractValue(request));
    }
}
