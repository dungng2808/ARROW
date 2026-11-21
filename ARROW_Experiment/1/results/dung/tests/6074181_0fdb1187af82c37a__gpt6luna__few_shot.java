package nl.inl.blacklab.contentstore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Test;

public class ContentStoreDirZip_ArrowTest {

    @Test
    public void roundTripsContentThroughStore() throws Exception {
        File dir = Files.createTempDirectory("content-store-zip").toFile();
        ContentStoreDirZip store = new ContentStoreDirZip(dir, true);
        try {
            String content = "A simple piece of content.";
            int id = store.store(content);
            assertEquals(content, store.retrieve(id));
        } finally {
            store.close();
            deleteRecursively(dir);
        }
    }

    @Test
    public void supportsEmptyContent() throws Exception {
        File dir = Files.createTempDirectory("content-store-zip-empty").toFile();
        ContentStoreDirZip store = new ContentStoreDirZip(dir, true);
        try {
            int id = store.store("");
            assertEquals("", store.retrieve(id));
        } finally {
            store.close();
            deleteRecursively(dir);
        }
    }

    @Test
    public void canSetBlockSizeAndStoreContent() throws Exception {
        File dir = Files.createTempDirectory("content-store-zip-block").toFile();
        ContentStoreDirZip store = new ContentStoreDirZip(dir, true);
        try {
            store.setBlockSizeCharacters(64);
            String content = "Content with a configured block size.";
            int id = store.store(content);
            assertEquals(content, store.retrieve(id));
        } finally {
            store.close();
            deleteRecursively(dir);
        }
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }
}
