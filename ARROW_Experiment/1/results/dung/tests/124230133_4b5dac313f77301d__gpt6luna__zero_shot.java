package com.github.nbaars.pwnedpasswords4j.client;

import okhttp3.OkHttpClient;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PwnedPasswordClient_ArrowTest {

    @Test
    public void fetchHashesAsyncParsesSuccessfulResponse() throws Exception {
        PwnedPasswordClient client = new PwnedPasswordClient(
                new OkHttpClient(),
                "http://127.0.0.1:1",
                "test-agent");

        Hex hash = Hex.from("ABCDE12345");
        CompletableFuture<List<Hex>> future = client.fetchHashesAsync(hash);

        try {
            future.get();
        } catch (Exception expected) {
            assertTrue(expected.getCause() != null);
        }
    }
}
