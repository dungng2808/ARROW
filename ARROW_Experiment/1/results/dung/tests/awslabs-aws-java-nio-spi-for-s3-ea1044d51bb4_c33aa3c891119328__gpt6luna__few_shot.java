package software.amazon.nio.spi.s3;

import org.junit.Test;

import java.nio.file.ClosedFileSystemException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class S3FileSystem_ArrowTest {

    @Test
    public void bucketNameAndProvider() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");
        assertEquals("example-bucket", fileSystem.bucketName());
        assertTrue(fileSystem.provider() instanceof S3FileSystemProvider);
    }

    @Test
    public void initialStateAndBasicProperties() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");
        assertTrue(fileSystem.isOpen());
        assertFalse(fileSystem.isReadOnly());
        assertEquals("/", fileSystem.getSeparator());
        assertEquals(S3FileSystem.BASIC_FILE_ATTRIBUTE_VIEW,
                fileSystem.supportedFileAttributeViews().iterator().next());
    }

    @Test
    public void supportedViewsAreUnmodifiable() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");
        Set<String> views = fileSystem.supportedFileAttributeViews();
        assertEquals(Collections.singleton("basic"), views);
        try {
            views.add("other");
            fail("Expected the supported views to be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            assertEquals(Collections.singleton("basic"), views);
        }
    }

    @Test
    public void rootDirectoryAndPathCreation() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");
        Iterator<Path> roots = fileSystem.getRootDirectories().iterator();
        assertTrue(roots.hasNext());
        assertEquals("/", roots.next().toString());
        assertFalse(roots.hasNext());
        assertEquals("/folder/file.txt", fileSystem.getPath("/folder", "file.txt").toString());
    }

    @Test
    public void fileStoresAreEmpty() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");
        assertFalse(fileSystem.getFileStores().iterator().hasNext());
    }

    @Test
    public void pathMatcherMatchesRegex() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");
        PathMatcher matcher = fileSystem.getPathMatcher("regex:.*/file[0-9]+\\.txt");
        assertTrue(matcher.matches(fileSystem.getPath("/folder/file12.txt")));
        assertFalse(matcher.matches(fileSystem.getPath("/folder/file.txt")));
    }

    @Test
    public void equalFileSystemsWithSameBucketAndProviderClass() {
        S3FileSystem first = new S3FileSystem("example-bucket");
        S3FileSystem second = new S3FileSystem("example-bucket");
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void fileSystemsWithDifferentBucketsAreNotEqual() {
        S3FileSystem first = new S3FileSystem("first-bucket");
        S3FileSystem second = new S3FileSystem("second-bucket");
        assertFalse(first.equals(second));
    }

    @Test
    public void equalityHandlesNullAndOtherTypes() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");
        assertFalse(fileSystem.equals(null));
        assertFalse(fileSystem.equals("example-bucket"));
        assertTrue(fileSystem.equals(fileSystem));
    }

    @Test
    public void closeMarksFileSystemClosed() throws Exception {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");
        fileSystem.close();
        assertFalse(fileSystem.isOpen());
        fileSystem.close();
        assertFalse(fileSystem.isOpen());
    }

    @Test
    public void principalLookupIsUnsupported() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");
        try {
            UserPrincipalLookupService ignored = fileSystem.getUserPrincipalLookupService();
            fail("Expected unsupported operation");
        } catch (UnsupportedOperationException expected) {
            assertTrue(expected.getMessage().contains("not yet supported"));
        }
    }

    @Test
    public void watchServiceIsUnsupported() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");
        try {
            WatchService ignored = fileSystem.newWatchService();
            fail("Expected unsupported operation");
        } catch (UnsupportedOperationException expected) {
            assertTrue(expected.getMessage().contains("not yet supported"));
        }
    }
}
