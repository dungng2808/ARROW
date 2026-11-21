package com.github.hburgmeier.jerseyoauth2.protocol.impl.extractor;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.Before;
import com.github.hburgmeier.jerseyoauth2.api.protocol.IHttpRequest;

public class HeaderExtractor_ArrowTest {

    private HeaderExtractor extractor;
    private MockHttpRequest mockRequest;

    @Before
    public void setUp() {
        mockRequest = new MockHttpRequest();
    }

    @Test
    public void testExtractValue_NullHeaderField() {
        extractor = new HeaderExtractor("Authorization");
        mockRequest.setHeaderField("Authorization", null);
        String result = extractor.extractValue(mockRequest);
        assertNull(result);
    }

    @Test
    public void testExtractValue_EmptyHeaderField() {
        extractor = new HeaderExtractor("Authorization");
        mockRequest.setHeaderField("Authorization", "");
        String result = extractor.extractValue(mockRequest);
        assertNull(result);
    }

    @Test
    public void testExtractValue_ValidHeaderValue() {
        extractor = new HeaderExtractor("Authorization");
        mockRequest.setHeaderField("Authorization", "Bearer ***");
        String result = extractor.extractValue(mockRequest);
        assertEquals("Bearer ***", result);
    }

    @Test
    public void testExtractValue_NonExistentHeader() {
        extractor = new HeaderExtractor("NonExistent");
        String result = extractor.extractValue(mockRequest);
        assertNull(result);
    }

    private class MockHttpRequest implements IHttpRequest {
        private java.util.Map<String, String> headers = new java.util.HashMap<>();

        @Override
        public String getHeaderField(String name) {
            return headers.get(name);
        }

        public void setHeaderField(String name, String value) {
            headers.put(name, value);
        }
    }
}
