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
		IHttpRequest request = mock(IHttpRequest.class);
		when(request.getHeaderField("Authorization")).thenReturn("Bearer ***");
		
		HeaderExtractor extractor = new HeaderExtractor("Authorization");
		String result = extractor.extractValue(request);
		
		assertEquals("Bearer ***", result);
	}
	
	@Test
	public void testExtractValueWithNullHeader() {
		IHttpRequest request = mock(IHttpRequest.class);
		when(request.getHeaderField("Authorization")).thenReturn(null);
		
		HeaderExtractor extractor = new HeaderExtractor("Authorization");
		String result = extractor.extractValue(request);
		
		assertEquals(null, result);
	}
	
	@Test
	public void testExtractValueWithEmptyHeader() {
		IHttpRequest request = mock(IHttpRequest.class);
		when(request.getHeaderField("Authorization")).thenReturn("");
		
		HeaderExtractor extractor = new HeaderExtractor("Authorization");
		String result = extractor.extractValue(request);
		
		assertEquals(null, result);
	}
	
	@Test
	public void testExtractValueWithDefaultIfEmpty() {
		IHttpRequest request = mock(IHttpRequest.class);
		when(request.getHeaderField("Authorization")).thenReturn(StringUtils.EMPTY);
		
		HeaderExtractor extractor = new HeaderExtractor("Authorization");
		String result = extractor.extractValue(request);
		
		assertEquals(null, result);
	}
}
