package com.github.hburgmeier.jerseyoauth2.protocol.impl.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.github.hburgmeier.jerseyoauth2.api.protocol.IHttpRequest;

public class HeaderExtractor_ArrowTest {

    @Test
    public void extractValueReturnsHeaderValue() {
        IHttpRequest request = new IHttpRequest() {
            @Override
            public String getHeaderField(String key) {
                return "Bearer token";
            }
        };

        HeaderExtractor extractor = new HeaderExtractor("Authorization");

        assertEquals("Bearer token", extractor.extractValue(request));
    }

    @Test
    public void extractValueReturnsNullForEmptyHeader() {
        IHttpRequest request = new IHttpRequest() {
            @Override
            public String getHeaderField(String key) {
                return "";
            }
        };

        HeaderExtractor extractor = new HeaderExtractor("Authorization");

        assertNull(extractor.extractValue(request));
    }
}
