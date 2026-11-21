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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PwnedPasswordClient_ArrowTest {

    @Test
    public void fetchHashesAsyncParsesSuccessfulResponse() throws Exception {
        OkHttpClient httpClient = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        when(httpClient.newCall(any(Request.class))).thenReturn(call);

        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://example.com/ABCDE").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(MediaType.parse("text/plain"), "1234567890:4\nFEDCBA9876:2"))
                .build();

        when(call.enqueue(any(Callback.class))).thenAnswer(invocation -> {
            Callback callback = (Callback) invocation.getArguments()[0];
            callback.onResponse(call, response);
            return null;
        });

        PwnedPasswordClient client = new PwnedPasswordClient(httpClient, "https://example.com", "test-agent");
        Hex range = Hex.from("ABCDE");
        CompletableFuture<List<Hex>> future = client.fetchHashesAsync(Hex.from("ABCDE1234567890"));

        assertEquals(Arrays.asList(Hex.from("ABCDE1234567890"), Hex.from("ABCDE FEDCBA9876".replace(" ", ""))), future.get());
        verify(httpClient).newCall(any(Request.class));
    }

    @Test
    public void fetchHashesAsyncCompletesExceptionallyOnFailure() throws Exception {
        OkHttpClient httpClient = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        when(httpClient.newCall(any(Request.class))).thenReturn(call);
        IOException failure = new IOException("connection failed");

        when(call.enqueue(any(Callback.class))).thenAnswer(invocation -> {
            Callback callback = (Callback) invocation.getArguments()[0];
            callback.onFailure(call, failure);
            return null;
        });

        PwnedPasswordClient client = new PwnedPasswordClient(httpClient, "https://example.com", "test-agent");
        CompletableFuture<List<Hex>> future = client.fetchHashesAsync(Hex.from("ABCDE1234567890"));

        try {
            future.get();
        } catch (ExecutionException e) {
            assertEquals(failure, e.getCause());
            return;
        }
        throw new AssertionError("Expected the future to fail");
    }

    @Test
    public void fetchHashesAsyncCompletesExceptionallyForUnsuccessfulResponse() throws Exception {
        OkHttpClient httpClient = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        when(httpClient.newCall(any(Request.class))).thenReturn(call);

        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://example.com/ABCDE").build())
                .protocol(Protocol.HTTP_1_1)
                .code(500)
                .message("Server Error")
                .body(ResponseBody.create(MediaType.parse("text/plain"), ""))
                .build();

        when(call.enqueue(any(Callback.class))).thenAnswer(invocation -> {
            Callback callback = (Callback) invocation.getArguments()[0];
            callback.onResponse(call, response);
            return null;
        });

        PwnedPasswordClient client = new PwnedPasswordClient(httpClient, "https://example.com", "test-agent");
        CompletableFuture<List<Hex>> future = client.fetchHashesAsync(Hex.from("ABCDE1234567890"));

        try {
            future.get();
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof java.net.ConnectException);
            return;
        }
        throw new AssertionError("Expected the future to fail");
    }
}
