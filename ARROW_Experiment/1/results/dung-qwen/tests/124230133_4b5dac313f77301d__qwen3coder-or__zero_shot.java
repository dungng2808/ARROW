package com.github.nbaars.pwnedpasswords4j.client;

import okhttp3.*;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;

public class PwnedPasswordClient_ArrowTest {

    @Test
    public void testFetchHashesAsyncSuccess() throws Exception {
        // Setup mock response
        String mockResponseBody = "123456789ABCDEF:1\n"
                                + "123456789ABCDE0:2\n"
                                + "123456789ABCDE1:3";
        
        ResponseBody responseBody = ResponseBody.create(mockResponseBody, MediaType.get("text/plain"));
        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://example.com/12345").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(responseBody)
                .build();

        // Mock client
        OkHttpClient mockClient = new OkHttpClient() {
            @Override
            public Call newCall(Request request) {
                return new Call() {
                    @Override
                    public Request request() {
                        return request;
                    }

                    @Override
                    public Response execute() throws IOException {
                        return response;
                    }

                    @Override
                    public void enqueue(Callback responseCallback) {
                        responseCallback.onResponse(this, response);
                    }

                    @Override
                    public void cancel() {}

                    @Override
                    public boolean isExecuted() {
                        return false;
                    }

                    @Override
                    public boolean isCanceled() {
                        return false;
                    }

                    @Override
                    public Call clone() {
                        return this;
                    }

                    @Override
                    public Connection connection() {
                        return null;
                    }
                };
            }
        };

        // Create client under test
        PwnedPasswordClient client = new PwnedPasswordClient(mockClient, "https://example.com", "test-agent");
        Hex hashedPassword = Hex.from("123456789ABCDEF123456789ABCDEF");

        // Execute
        CompletableFuture<List<Hex>> future = client.fetchHashesAsync(hashedPassword);
        List<Hex> result = future.get();

        // Verify
        assertEquals(3, result.size());
        assertEquals("12345123456789ABCDEF", result.get(0).value());
        assertEquals("12345123456789ABCDE0", result.get(1).value());
        assertEquals("12345123456789ABCDE1", result.get(2).value());
    }

    @Test
    public void testFetchHashesAsyncFailure() throws Exception {
        // Mock client that fails
        OkHttpClient mockClient = new OkHttpClient() {
            @Override
            public Call newCall(Request request) {
                return new Call() {
                    @Override
                    public Request request() {
                        return request;
                    }

                    @Override
                    public Response execute() throws IOException {
                        throw new IOException("Network error");
                    }

                    @Override
                    public void enqueue(Callback responseCallback) {
                        responseCallback.onFailure(this, new IOException("Network error"));
                    }

                    @Override
                    public void cancel() {}

                    @Override
                    public boolean isExecuted() {
                        return false;
                    }

                    @Override
                    public boolean isCanceled() {
                        return false;
                    }

                    @Override
                    public Call clone() {
                        return this;
                    }

                    @Override
                    public Connection connection() {
                        return null;
                    }
                };
            }
        };

        // Create client under test
        PwnedPasswordClient client = new PwnedPasswordClient(mockClient, "https://example.com", "test-agent");
        Hex hashedPassword = Hex.from("123456789ABCDEF123456789ABCDEF");

        // Execute
        CompletableFuture<List<Hex>> future = client.fetchHashesAsync(hashedPassword);

        // Verify
        try {
            future.get();
            fail("Expected exception was not thrown");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    @Test
    public void testBuildRequest() {
        // Create client under test
        PwnedPasswordClient client = new PwnedPasswordClient(null, "https://example.com", "test-agent");
        Hex hashedPassword = Hex.from("123456789ABCDEF123456789ABCDEF");

        // Build request
        Request request = client.buildRequest(hashedPassword);

        // Verify
        assertEquals("https://example.com/12345", request.url().toString());
        assertEquals("test-agent", request.header("User-Agent"));
    }

    @Test
    public void testParseResponse() throws Exception {
        // Setup mock response body
        String mockResponseBody = "123456789ABCDEF:1\n"
                                + "123456789ABCDE0:2";
        
        ResponseBody responseBody = ResponseBody.create(mockResponseBody, MediaType.get("text/plain"));
        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://example.com/12345").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(responseBody)
                .build();

        // Create client under test
        PwnedPasswordClient client = new PwnedPasswordClient(null, "https://example.com", "test-agent");
        Hex range = Hex.from("12345");

        // Parse response
        List<Hex> result = client.parseResponse(range, response);

        // Verify
        assertEquals(2, result.size());
        assertEquals("12345123456789ABCDEF", result.get(0).value());
        assertEquals("12345123456789ABCDE0", result.get(1).value());
    }
}
