package com.github.nbaars.pwnedpasswords4j.client;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PwnedPasswordClient_ArrowTest {

    @Test
    public void fetchHashesAsyncParsesSuccessfulResponse() throws Exception {
        final Request[] capturedRequest = new Request[1];
        OkHttpClient client = new OkHttpClient() {
            @Override
            public Call newCall(Request request) {
                capturedRequest[0] = request;
                return new Call() {
                    @Override
                    public Request request() {
                        return request;
                    }

                    @Override
                    public void enqueue(Callback callback) {
                        try {
                            Response response = new Response.Builder()
                                    .request(request)
                                    .protocol(Protocol.HTTP_1_1)
                                    .code(200)
                                    .message("OK")
                                    .body(ResponseBody.create(
                                            MediaType.parse("text/plain"),
                                            "0000000000000000000000000000000000000001:2\n0000000000000000000000000000000000000002:5"))
                                    .build();
                            callback.onResponse(this, response);
                        } catch (IOException e) {
                            callback.onFailure(this, e);
                        }
                    }

                    @Override
                    public void cancel() {
                    }

                    @Override
                    public boolean isExecuted() {
                        return true;
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
                    public okhttp3.Timeout timeout() {
                        return new okhttp3.Timeout();
                    }
                };
            }
        };

        PwnedPasswordClient clientUnderTest =
                new PwnedPasswordClient(client, "https://example.test/range", "test-agent");
        Hex hash = Hex.from("ABCDE12345FFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");

        CompletableFuture<List<Hex>> future = clientUnderTest.fetchHashesAsync(hash);
        List<Hex> result = future.get();

        assertEquals(2, result.size());
        assertEquals(Hex.from("ABCDE0000000000000000000000000000000000001"), result.get(0));
        assertEquals(Hex.from("ABCDE0000000000000000000000000000000000002"), result.get(1));
        assertEquals("https://example.test/range/ABCDE", capturedRequest[0].url().toString());
        assertEquals("test-agent", capturedRequest[0].header("User-Agent"));
    }

    @Test
    public void fetchHashesAsyncCompletesExceptionallyOnUnsuccessfulResponse() throws Exception {
        OkHttpClient client = new OkHttpClient() {
            @Override
            public Call newCall(final Request request) {
                return new Call() {
                    @Override
                    public Request request() {
                        return request;
                    }

                    @Override
                    public void enqueue(Callback callback) {
                        Response response = new Response.Builder()
                                .request(request)
                                .protocol(Protocol.HTTP_1_1)
                                .code(404)
                                .message("Not Found")
                                .body(ResponseBody.create(MediaType.parse("text/plain"), ""))
                                .build();
                        callback.onResponse(this, response);
                    }

                    @Override
                    public void cancel() {
                    }

                    @Override
                    public boolean isExecuted() {
                        return true;
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
                    public okhttp3.Timeout timeout() {
                        return new okhttp3.Timeout();
                    }
                };
            }
        };

        PwnedPasswordClient clientUnderTest =
                new PwnedPasswordClient(client, "https://example.test/range", "test-agent");

        try {
            clientUnderTest.fetchHashesAsync(Hex.from("ABCDE12345FFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")).get();
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof java.net.ConnectException);
            return;
        }
        throw new AssertionError("Expected the future to complete exceptionally");
    }

    @Test
    public void fetchHashesAsyncCompletesExceptionallyOnCallFailure() throws Exception {
        final IOException failure = new IOException("offline");
        OkHttpClient client = new OkHttpClient() {
            @Override
            public Call newCall(final Request request) {
                return new Call() {
                    @Override
                    public Request request() {
                        return request;
                    }

                    @Override
                    public void enqueue(Callback callback) {
                        callback.onFailure(this, failure);
                    }

                    @Override
                    public void cancel() {
                    }

                    @Override
                    public boolean isExecuted() {
                        return true;
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
                    public okhttp3.Timeout timeout() {
                        return new okhttp3.Timeout();
                    }
                };
            }
        };

        PwnedPasswordClient clientUnderTest =
                new PwnedPasswordClient(client, "https://example.test/range", "test-agent");

        try {
            clientUnderTest.fetchHashesAsync(Hex.from("ABCDE12345FFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")).get();
        } catch (ExecutionException e) {
            assertEquals(failure, e.getCause());
            return;
        }
        throw new AssertionError("Expected the future to complete exceptionally");
    }
}
