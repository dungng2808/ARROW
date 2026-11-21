package nl.inl.blacklab.contentstore;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;

public class ContentStoreDirZip_ArrowTest {

    @Test
    public void testConstructor() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "test_contentstore");
        tempDir.mkdirs();
        try {
            ContentStoreDirZip store = new ContentStoreDirZip(tempDir, true);
            assertNotNull(store);
        } finally {
            // Clean up
            tempDir.deleteOnExit();
        }
    }

    @Test
    public void testSetBlockSizeCharacters() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "test_contentstore2");
        tempDir.mkdirs();
        try {
            ContentStoreDirZip store = new ContentStoreDirZip(tempDir, true);
            store.setBlockSizeCharacters(1000);
            assertEquals(1000, store.getBlockSizeCharacters());
        } finally {
            // Clean up
            tempDir.deleteOnExit();
        }
    }

    @Test
    public void testEncodeDecodeBlock() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "test_contentstore3");
        tempDir.mkdirs();
        try {
            ContentStoreDirZip store = new ContentStoreDirZip(tempDir, true);
            String original = "This is a test string for encoding and decoding.";
            
            byte[] encoded = store.encodeBlock(original);
            String decoded = store.decodeBlock(encoded, 0, encoded.length);
            
            assertEquals(original, decoded);
        } finally {
            // Clean up
            tempDir.deleteOnExit();
        }
    }

    @Test
    public void testSetStoreType() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "test_contentstore4");
        tempDir.mkdirs();
        try {
            ContentStoreDirZip store = new ContentStoreDirZip(tempDir, true);
            store.setStoreType(); // This should be called internally
            
            // We can't directly test the internal state, but we can verify
            // that the constructor and basic operations work correctly
            assertNotNull(store);
        } finally {
            // Clean up
            tempDir.deleteOnExit();
        }
    }

    @Test
    public void testClose() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "test_contentstore5");
        tempDir.mkdirs();
        try {
            ContentStoreDirZip store = new ContentStoreDirZip(tempDir, true);
            store.close();
            // Just verify that no exception is thrown
            assertTrue(true);
        } finally {
            // Clean up
            tempDir.deleteOnExit();
        }
    }

    @Test
    public void testCompressDecompressWithSpecialCharacters() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "test_contentstore6");
        tempDir.mkdirs();
        try {
            ContentStoreDirZip store = new ContentStoreDirZip(tempDir, true);
            String original = "<xml><tag>Special chars: áéíóú ñü</tag></xml>";
            
            byte[] encoded = store.encodeBlock(original);
            String decoded = store.decodeBlock(encoded, 0, encoded.length);
            
            assertEquals(original, decoded);
        } finally {
            // Clean up
            tempDir.deleteOnExit();
        }
    }
}
