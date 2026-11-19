package com.github.hburgmeier.jerseyoauth2.protocol.impl.extractor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.github.hburgmeier.jerseyoauth2.api.protocol.IHttpRequest;

public class HeaderExtractor_ArrowTest {

	@Test
	public void testExtractValueWithValidHeader() {
		String headerKey = "Authorization";
		String headerValue = "Bearer ***";
		
		IHttpRequest request = mock(IHttpRequest.class);
		when(request.getHeaderField(headerKey)).thenReturn(headerValue);
		
		HeaderExtractor extractor = new HeaderExtractor(headerKey);
		String result = extractor.extractValue(request);
		
		assertEquals(headerValue, result);
	}
	
	@Test
	public void testExtractValueWithNullHeader() {
		String headerKey = "Authorization";
		
		IHttpRequest request = mock(IHttpRequest.class);
		when(request.getHeaderField(headerKey)).thenReturn(null);
		
		HeaderExtractor extractor = new HeaderExtractor(headerKey);
		String result = extractor.extractValue(request);
		
		assertEquals(null, result);
	}
	
	@Test
	public void testExtractValueWithEmptyHeader() {
		String headerKey = "Authorization";
		
		IHttpRequest request = mock(IHttpRequest.class);
		when(request.getHeaderField(headerKey)).thenReturn("");
		
		HeaderExtractor extractor = new HeaderExtractor(headerKey);
		String result = extractor.extractValue(request);
		
		assertEquals(null, result);
	}
	
	@Test
	public void testExtractValueWithWhitespaceHeader() {
		String headerKey = "Authorization";
		
		IHttpRequest request = mock(IHttpRequest.class);
		when(request.getHeaderField(headerKey)).thenReturn("   ");
		
		HeaderExtractor extractor = new HeaderExtractor(headerKey);
		String result = extractor.extractValue(request);
		
		assertEquals(null, result);
	}
}
