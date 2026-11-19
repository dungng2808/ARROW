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
        String responseBody = "hash1:1\nhash2:2";
        Response response = new Response.Builder()
                .request(new Request.Builder().url("http://example.com/12345").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(responseBody, MediaType.get("text/plain")))
                .build();

        // Mock client that returns our response
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
                    public void cancel() {
                    }

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
        PwnedPasswordClient client = new PwnedPasswordClient(mockClient, "http://example.com", "test-agent");

        // Execute
        CompletableFuture<List<Hex>> future = client.fetchHashesAsync(Hex.from("1234567890abcdef"));

        // Verify
        List<Hex> result = future.get();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("12345hash1", result.get(0).toString());
        assertEquals("12345hash2", result.get(1).toString());
    }

    @Test
    public void testFetchHashesAsyncFailure() throws Exception {
        // Mock client that throws exception
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
                    public void cancel() {
                    }

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
        PwnedPasswordClient client = new PwnedPasswordClient(mockClient, "http://example.com", "test-agent");

        // Execute
        CompletableFuture<List<Hex>> future = client.fetchHashesAsync(Hex.from("1234567890abcdef"));

        // Verify
        try {
            future.get();
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    @Test
    public void testFetchHashesAsyncUnsuccessfulResponse() throws Exception {
        // Setup unsuccessful response
        Response response = new Response.Builder()
                .request(new Request.Builder().url("http://example.com/12345").build())
                .protocol(Protocol.HTTP_1_1)
                .code(500)
                .message("Internal Server Error")
                .build();

        // Mock client that returns our response
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
                    public void cancel() {
                    }

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
        PwnedPasswordClient client = new PwnedPasswordClient(mockClient, "http://example.com", "test-agent");

        // Execute
        CompletableFuture<List<Hex>> future = client.fetchHashesAsync(Hex.from("1234567890abcdef"));

        // Verify
        try {
            future.get();
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ConnectException);
        }
    }

    @Test
    public void testBuildRequest() {
        // Create client under test
        PwnedPasswordClient client = new PwnedPasswordClient(null, "http://example.com", "test-agent");

        // Build request
        Request request = client.buildRequest(Hex.from("1234567890abcdef"));

        // Verify
        assertEquals("http://example.com/12345", request.url().toString());
        assertEquals("test-agent", request.header("User-Agent"));
    }
}
