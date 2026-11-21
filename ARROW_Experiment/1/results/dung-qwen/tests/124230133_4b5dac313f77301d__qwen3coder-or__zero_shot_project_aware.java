package com.github.nbaars.pwnedpasswords4j.client;

import okhttp3.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class PwnedPasswordClient_ArrowTest {

    @Mock
    private OkHttpClient mockHttpClient;

    @Mock
    private Call mockCall;

    @Mock
    private Response mockResponse;

    @Mock
    private ResponseBody mockResponseBody;

    private PwnedPasswordClient client;
    private static final String TEST_URL = "https://api.pwnedpasswords.com";
    private static final String TEST_USER_AGENT = "test-user-agent";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        client = new PwnedPasswordClient(mockHttpClient, TEST_URL, TEST_USER_AGENT);
    }

    @Test
    public void testFetchHashesAsync_SuccessfulResponse() throws IOException {
        // Given
        Hex testHash = new Hex("1234567890ABCDEF1234567890ABCDEF12345678");
        String responseBodyContent = "1234567890ABCDEF1234567890ABCDEF12345678:1\n" +
                "1234567890ABCDEF1234567890ABCDEF12345679:2\n" +
                "1234567890ABCDEF1234567890ABCDEF1234567A:3";
        
        when(mockHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponseBody.string()).thenReturn(responseBodyContent);
        doNothing().when(mockResponse).close();

        // When
        CompletableFuture<List<Hex>> future = client.fetchHashesAsync(testHash);

        // Then
        try {
            List<Hex> result = future.get();
            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals("1234567890ABCDEF1234567890ABCDEF12345678", result.get(0).value());
            assertEquals("1234567890ABCDEF1234567890ABCDEF12345679", result.get(1).value());
            assertEquals("1234567890ABCDEF1234567890ABCDEF1234567A", result.get(2).value());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        // Verify
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();
        assertEquals(TEST_URL + "/12345", capturedRequest.url().toString());
        assertEquals(TEST_USER_AGENT, capturedRequest.header("User-Agent"));
    }

    @Test
    public void testFetchHashesAsync_UnsuccessfulResponse() throws IOException {
        // Given
        Hex testHash = new Hex("1234567890ABCDEF1234567890ABCDEF12345678");
        when(mockHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockResponse.isSuccessful()).thenReturn(false);
        doNothing().when(mockResponse).close();

        // When
        CompletableFuture<List<Hex>> future = client.fetchHashesAsync(testHash);

        // Then
        try {
            future.get();
            fail("Expected exception was not thrown");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ConnectException);
            assertTrue(e.getCause().getMessage().contains("Call failed to " + TEST_URL + " got "));
        }

        // Verify
        verify(mockHttpClient).newCall(any(Request.class));
    }

    @Test
    public void testFetchHashesAsync_IOFailure() throws IOException {
        // Given
        Hex testHash = new Hex("1234567890ABCDEF1234567890ABCDEF12345678");
        when(mockHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        doThrow(new IOException("Network error")).when(mockCall).enqueue(any(Callback.class));

        // When
        CompletableFuture<List<Hex>> future = client.fetchHashesAsync(testHash);

        // Then
        try {
            future.get();
            fail("Expected exception was not thrown");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IOException);
            assertEquals("Network error", e.getCause().getMessage());
        }
    }

    @Test
    public void testBuildRequest() {
        // Given
        Hex testHash = new Hex("1234567890ABCDEF1234567890ABCDEF12345678");

        // When
        Request request = client.buildRequest(testHash);

        // Then
        assertEquals(TEST_URL + "/12345", request.url().toString());
        assertEquals(TEST_USER_AGENT, request.header("User-Agent"));
    }

    @Test
    public void testParseResponse() throws IOException {
        // Given
        Hex range = new Hex("12345");
        String responseBodyContent = "1234567890ABCDEF1234567890ABCDEF12345678:1\n" +
                "1234567890ABCDEF1234567890ABCDEF12345679:2";
        when(mockResponseBody.string()).thenReturn(responseBodyContent);

        // When
        List<Hex> result = client.parseResponse(range, mockResponse);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("1234567890ABCDEF1234567890ABCDEF12345678", result.get(0).value());
        assertEquals("1234567890ABCDEF1234567890ABCDEF12345679", result.get(1).value());
    }
}
