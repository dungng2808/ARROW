package nl.inl.blacklab.contentstore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Test;

public class ContentStoreDirZip_ArrowTest {

    @Test
    public void constructsAndClosesStore() throws Exception {
        File dir = Files.createTempDirectory("content-store-zip").toFile();
        ContentStoreDirZip store = new ContentStoreDirZip(dir, true);
        assertNotNull(store);
        store.close();
    }

    @Test
    public void encodesAndDecodesBlock() throws Exception {
        File dir = Files.createTempDirectory("content-store-zip").toFile();
        ContentStoreDirZip store = new ContentStoreDirZip(dir, true);
        try {
            String text = "A short block of text.";
            byte[] encoded = store.encodeBlock(text);
            assertNotNull(encoded);
            assertEquals(text, store.decodeBlock(encoded, 0, encoded.length));
        } finally {
            store.close();
        }
    }

    @Test
    public void encodesAndDecodesEmptyBlock() throws Exception {
        File dir = Files.createTempDirectory("content-store-zip").toFile();
        ContentStoreDirZip store = new ContentStoreDirZip(dir, true);
        try {
            String text = "";
            byte[] encoded = store.encodeBlock(text);
            assertEquals(text, store.decodeBlock(encoded, 0, encoded.length));
        } finally {
            store.close();
        }
    }

    @Test
    public void changesBlockSizeAndStillRoundTrips() throws Exception {
        File dir = Files.createTempDirectory("content-store-zip").toFile();
        ContentStoreDirZip store = new ContentStoreDirZip(dir, true);
        try {
            store.setBlockSizeCharacters(128);
            String text = "Block size changes clear the compression buffer pool.";
            byte[] encoded = store.encodeBlock(text);
            assertEquals(text, store.decodeBlock(encoded, 0, encoded.length));
        } finally {
            store.close();
        }
    }
}
