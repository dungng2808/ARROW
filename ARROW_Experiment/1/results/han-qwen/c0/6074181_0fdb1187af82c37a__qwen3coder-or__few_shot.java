package nl.inl.blacklab.contentstore;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ContentStoreDirZip_ArrowTest {

    @Test
    public void testConstructor() throws IOException {
        Path tempDir = Files.createTempDirectory("contentstore_test");
        File dir = tempDir.toFile();
        try {
            ContentStoreDirZip store = new ContentStoreDirZip(dir, true);
            assertNotNull(store);
        } finally {
            // Clean up
            dir.deleteOnExit();
        }
    }

    @Test
    public void testSetBlockSizeCharacters() throws IOException {
        Path tempDir = Files.createTempDirectory("contentstore_test");
        File dir = tempDir.toFile();
        try {
            ContentStoreDirZip store = new ContentStoreDirZip(dir, true);
            int newSize = 2048;
            store.setBlockSizeCharacters(newSize);
            assertEquals(newSize, store.getBlockSizeCharacters());
        } finally {
            // Clean up
            dir.deleteOnExit();
        }
    }

    @Test
    public void testEncodeAndDecodeBlock() throws IOException {
        Path tempDir = Files.createTempDirectory("contentstore_test");
        File dir = tempDir.toFile();
        try {
            ContentStoreDirZip store = new ContentStoreDirZip(dir, true);
            
            String originalText = "This is a test string for encoding and decoding.";
            byte[] encoded = store.encodeBlock(originalText);
            assertNotNull(encoded);
            assertTrue(encoded.length > 0);
            
            String decoded = store.decodeBlock(encoded, 0, encoded.length);
            assertEquals(originalText, decoded);
        } finally {
            // Clean up
            dir.deleteOnExit();
        }
    }

    @Test
    public void testStoreType() throws IOException {
        Path tempDir = Files.createTempDirectory("contentstore_test");
        File dir = tempDir.toFile();
        try {
            ContentStoreDirZip store = new ContentStoreDirZip(dir, true);
            store.setStoreType();
            // Just verify it doesn't throw an exception
            assertNotNull(store);
        } finally {
            // Clean up
            dir.deleteOnExit();
        }
    }

    @Test
    public void testClose() throws IOException {
        Path tempDir = Files.createTempDirectory("contentstore_test");
        File dir = tempDir.toFile();
        try {
            ContentStoreDirZip store = new ContentStoreDirZip(dir, true);
            store.close();
            // Just verify it doesn't throw an exception
            assertNotNull(store);
        } finally {
            // Clean up
            dir.deleteOnExit();
        }
    }
}
