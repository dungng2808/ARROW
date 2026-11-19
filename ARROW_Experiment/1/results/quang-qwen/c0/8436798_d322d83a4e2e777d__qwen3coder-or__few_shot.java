package com.github.hburgmeier.jerseyoauth2.protocol.impl.extractor;

import com.github.hburgmeier.jerseyoauth2.api.protocol.IHttpRequest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HeaderExtractor_ArrowTest {

    @Test
    public void testExtractValueWithNullHeader() {
        IHttpRequest request = mock(IHttpRequest.class);
        when(request.getHeaderField("Authorization")).thenReturn(null);
        
        HeaderExtractor extractor = new HeaderExtractor("Authorization");
        String result = extractor.extractValue(request);
        
        assertNull(result);
    }

    @Test
    public void testExtractValueWithEmptyHeader() {
        IHttpRequest request = mock(IHttpRequest.class);
        when(request.getHeaderField("Authorization")).thenReturn("");
        
        HeaderExtractor extractor = new HeaderExtractor("Authorization");
        String result = extractor.extractValue(request);
        
        assertNull(result);
    }

    @Test
    public void testExtractValueWithValidHeader() {
        IHttpRequest request = mock(IHttpRequest.class);
        when(request.getHeaderField("Authorization")).thenReturn("Bearer ***");
        
        HeaderExtractor extractor = new HeaderExtractor("Authorization");
        String result = extractor.extractValue(request);
        
        assertEquals("Bearer ***", result);
    }

    @Test
    public void testExtractValueWithWhitespaceHeader() {
        IHttpRequest request = mock(IHttpRequest.class);
        when(request.getHeaderField("Authorization")).thenReturn("   ");
        
        HeaderExtractor extractor = new HeaderExtractor("Authorization");
        String result = extractor.extractValue(request);
        
        assertNull(result);
    }
}
