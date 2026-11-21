package nl.inl.blacklab.contentstore;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ContentStoreDirZip_ArrowTest {

    private File tempDir;
    private ContentStoreDirZip contentStore;

    @Before
    public void setUp() throws IOException {
        tempDir = File.createTempFile("contentstore", "test");
        tempDir.delete();
        tempDir.mkdir();
        contentStore = new ContentStoreDirZip(tempDir, true);
    }

    @After
    public void tearDown() {
        if (contentStore != null) {
            contentStore.close();
        }
        deleteDirectory(tempDir);
    }

    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }

    @Test
    public void testConstructor() {
        assertNotNull(contentStore);
    }

    @Test
    public void testSetBlockSizeCharacters() {
        int originalSize = contentStore.getBlockSizeCharacters();
        contentStore.setBlockSizeCharacters(2000);
        assertEquals(2000, contentStore.getBlockSizeCharacters());
        contentStore.setBlockSizeCharacters(originalSize);
        assertEquals(originalSize, contentStore.getBlockSizeCharacters());
    }

    @Test
    public void testEncodeAndDecodeBlock() {
        String originalText = "This is a test string for encoding and decoding.";
        byte[] encoded = contentStore.encodeBlock(originalText);
        assertNotNull(encoded);
        assertTrue(encoded.length > 0);

        String decoded = contentStore.decodeBlock(encoded, 0, encoded.length);
        assertEquals(originalText, decoded);
    }

    @Test
    public void testEncodeAndDecodeEmptyBlock() {
        String originalText = "";
        byte[] encoded = contentStore.encodeBlock(originalText);
        assertNotNull(encoded);
        assertEquals(0, encoded.length);

        String decoded = contentStore.decodeBlock(encoded, 0, encoded.length);
        assertEquals(originalText, decoded);
    }

    @Test
    public void testEncodeAndDecodeLongBlock() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("This is a long test string to verify compression works correctly. ");
        }
        String originalText = sb.toString();

        byte[] encoded = contentStore.encodeBlock(originalText);
        assertNotNull(encoded);
        assertTrue(encoded.length > 0);
        assertTrue(encoded.length < originalText.getBytes().length);

        String decoded = contentStore.decodeBlock(encoded, 0, encoded.length);
        assertEquals(originalText, decoded);
    }

    @Test
    public void testClose() {
        contentStore.close();
        // Just checking that it doesn't throw an exception
        assertTrue(true);
    }

    @Test
    public void testSetStoreType() {
        // This is tested indirectly through the constructor and super class behavior
        // We just ensure it doesn't throw an exception
        contentStore.setStoreType();
        assertTrue(true);
    }

    @Test
    public void testZipBufferSizeChanged() {
        // This method is called internally when block size changes
        // We test that it doesn't throw an exception
        contentStore.setBlockSizeCharacters(1000);
        assertTrue(true);
    }

    @Test
    public void testEncodeBlockWithSpecialCharacters() {
        String originalText = "Special chars: \n\t\r\"'<>!@#$%^&*()";
        byte[] encoded = contentStore.encodeBlock(originalText);
        assertNotNull(encoded);
        assertTrue(encoded.length > 0);

        String decoded = contentStore.decodeBlock(encoded, 0, encoded.length);
        assertEquals(originalText, decoded);
    }

    @Test
    public void testMultipleEncodesDecodes() {
        String[] testStrings = {
            "First test",
            "Second test with more text",
            "",
            "A",
            "Very long string with many characters that should compress well when repeated multiple times in a large document structure like this one.",
            "Special chars: \n\t\r\"'<>!@#$%^&*()"
        };

        for (String originalText : testStrings) {
            byte[] encoded = contentStore.encodeBlock(originalText);
            assertNotNull(encoded);
            String decoded = contentStore.decodeBlock(encoded, 0, encoded.length);
            assertEquals(originalText, decoded);
        }
    }
}
