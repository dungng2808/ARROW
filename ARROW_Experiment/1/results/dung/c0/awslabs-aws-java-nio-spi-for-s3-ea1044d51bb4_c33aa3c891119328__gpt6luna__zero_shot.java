package software.amazon.nio.spi.s3;

import org.junit.Test;

import java.nio.file.ClosedFileSystemException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.UnsupportedOperationException;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class S3FileSystem_ArrowTest {

    @Test
    public void bucketNameAndProviderAreAvailable() {
        S3FileSystem fileSystem = new S3FileSystem("test-bucket");

        assertEquals("test-bucket", fileSystem.bucketName());
        assertNotNull(fileSystem.provider());
    }

    @Test
    public void initialStateAndSeparator() {
        S3FileSystem fileSystem = new S3FileSystem("test-bucket");

        assertTrue(fileSystem.isOpen());
        assertFalse(fileSystem.isReadOnly());
        assertEquals("/", fileSystem.getSeparator());
    }

    @Test
    public void rootDirectoryIsTheBucketRoot() {
        S3FileSystem fileSystem = new S3FileSystem("test-bucket");

        Iterator<Path> roots = fileSystem.getRootDirectories().iterator();
        assertTrue(roots.hasNext());
        assertEquals("/", roots.next().toString());
        assertFalse(roots.hasNext());
    }

    @Test
    public void fileStoresAreEmpty() {
        S3FileSystem fileSystem = new S3FileSystem("test-bucket");

        assertFalse(fileSystem.getFileStores().iterator().hasNext());
    }

    @Test
    public void supportedAttributeViewsContainsOnlyBasic() {
        S3FileSystem fileSystem = new S3FileSystem("test-bucket");

        Set<String> views = fileSystem.supportedFileAttributeViews();
        assertEquals(1, views.size());
        assertTrue(views.contains("basic"));
    }

    @Test
    public void getPathCreatesBucketPath() {
        S3FileSystem fileSystem = new S3FileSystem("test-bucket");

        assertEquals("/folder/file.txt", fileSystem.getPath("/folder", "file.txt").toString());
    }

    @Test
    public void regexPathMatcherMatchesMatchingPath() {
        S3FileSystem fileSystem = new S3FileSystem("test-bucket");
        PathMatcher matcher = fileSystem.getPathMatcher("regex:.*\\.txt");

        assertTrue(matcher.matches(fileSystem.getPath("folder/file.txt")));
        assertFalse(matcher.matches(fileSystem.getPath("folder/file.csv")));
    }

    @Test
    public void unsupportedLookupAndWatchOperationsThrow() {
        S3FileSystem fileSystem = new S3FileSystem("test-bucket");

        try {
            fileSystem.getUserPrincipalLookupService();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            assertNotNull(expected.getMessage());
        }

        try {
            fileSystem.newWatchService();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void closeMarksFileSystemClosed() throws Exception {
        S3FileSystem fileSystem = new S3FileSystem("test-bucket");

        fileSystem.close();

        assertFalse(fileSystem.isOpen());
    }

    @Test
    public void equalFileSystemsHaveEqualHashCodes() {
        S3FileSystem first = new S3FileSystem("test-bucket");
        S3FileSystem second = new S3FileSystem("test-bucket");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void fileSystemsWithDifferentBucketsAreNotEqual() {
        S3FileSystem first = new S3FileSystem("first-bucket");
        S3FileSystem second = new S3FileSystem("second-bucket");

        assertNotEquals(first, second);
        assertNotEquals(first, null);
        assertNotEquals(first, "first-bucket");
    }
}
