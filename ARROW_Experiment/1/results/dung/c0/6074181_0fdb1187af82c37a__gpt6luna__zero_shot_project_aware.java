package nl.inl.blacklab.contentstore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Test;

public class ContentStoreDirZip_ArrowTest {

    @Test
    public void encodeAndDecodeBlockRoundTrip() throws Exception {
        File dir = Files.createTempDirectory("content-store-zip").toFile();
        ContentStoreDirZip store = new ContentStoreDirZip(dir, true);
        try {
            String original = "A simple block of text to compress.";
            byte[] encoded = store.encodeBlock(original);

            assertNotEquals(0, encoded.length);
            assertTrue(encoded.length < original.getBytes(StandardCharsets.UTF_8).length);
            assertEquals(original, store.decodeBlock(encoded, 0, encoded.length));
        } finally {
            store.close();
            Files.deleteIfExists(dir.toPath());
        }
    }

    @Test
    public void setBlockSizeCharactersStillAllowsRoundTrip() throws Exception {
        File dir = Files.createTempDirectory("content-store-zip-size").toFile();
        ContentStoreDirZip store = new ContentStoreDirZip(dir, true);
        try {
            store.setBlockSizeCharacters(128);
            String original = "Text after changing the block size.";
            byte[] encoded = store.encodeBlock(original);

            assertEquals(original, store.decodeBlock(encoded, 0, encoded.length));
        } finally {
            store.close();
            Files.deleteIfExists(dir.toPath());
        }
    }

    @Test
    public void decodeBlockHonorsOffsetAndLength() throws Exception {
        File dir = Files.createTempDirectory("content-store-zip-offset").toFile();
        ContentStoreDirZip store = new ContentStoreDirZip(dir, true);
        try {
            String original = "Compressed data with surrounding bytes.";
            byte[] encoded = store.encodeBlock(original);
            byte[] wrapped = new byte[encoded.length + 4];
            System.arraycopy(encoded, 0, wrapped, 2, encoded.length);

            assertEquals(original, store.decodeBlock(wrapped, 2, encoded.length));
        } finally {
            store.close();
            Files.deleteIfExists(dir.toPath());
        }
    }
}
