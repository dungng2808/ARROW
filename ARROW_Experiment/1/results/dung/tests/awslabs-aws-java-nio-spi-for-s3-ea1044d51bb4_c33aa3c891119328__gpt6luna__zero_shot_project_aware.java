package software.amazon.nio.spi.s3;

import org.junit.Test;

import java.nio.file.ClosedFileSystemException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.PatternSyntaxException;
import java.nio.file.UnsupportedOperationException;
import java.util.Collections;

import static org.junit.Assert.*;

public class S3FileSystem_ArrowTest {

    @Test
    public void bucketNameReturnsBucketFromConstructor() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");

        assertEquals("example-bucket", fileSystem.bucketName());
    }

    @Test
    public void providerReturnsProviderUsedByFilesystem() {
        S3FileSystemProvider provider = new S3FileSystemProvider();
        S3FileSystem fileSystem = new S3FileSystem("s3://example-bucket", provider);

        assertSame(provider, fileSystem.provider());
    }

    @Test
    public void filesystemStartsOpenAndIsNotReadOnly() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");

        assertTrue(fileSystem.isOpen());
        assertFalse(fileSystem.isReadOnly());
    }

    @Test
    public void closeMarksFilesystemClosed() throws Exception {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");

        fileSystem.close();

        assertFalse(fileSystem.isOpen());
    }

    @Test
    public void separatorIsSlash() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");

        assertEquals("/", fileSystem.getSeparator());
    }

    @Test
    public void rootDirectoriesContainsBucketRoot() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");

        Path root = fileSystem.getRootDirectories().iterator().next();

        assertEquals("/", root.toString());
    }

    @Test
    public void fileStoresIsEmpty() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");

        assertFalse(fileSystem.getFileStores().iterator().hasNext());
    }

    @Test
    public void supportedViewsContainsOnlyBasic() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");

        assertEquals(Collections.singleton("basic"), fileSystem.supportedFileAttributeViews());
    }

    @Test
    public void getPathCreatesPathInThisFilesystem() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");

        S3Path path = fileSystem.getPath("/folder/file.txt");

        assertEquals("/folder/file.txt", path.toString());
        assertSame(fileSystem, path.getFileSystem());
    }

    @Test
    public void getPathJoinsAdditionalElements() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");

        S3Path path = fileSystem.getPath("/folder", "file.txt");

        assertEquals("/folder/file.txt", path.toString());
    }

    @Test
    public void globPathMatcherMatchesMatchingPath() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");
        PathMatcher matcher = fileSystem.getPathMatcher("glob:*.txt");

        assertTrue(matcher.matches(fileSystem.getPath("notes.txt")));
    }

    @Test
    public void regexPathMatcherRejectsNonmatchingPath() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");
        PathMatcher matcher = fileSystem.getPathMatcher("regex:.*\\.txt");

        assertFalse(matcher.matches(fileSystem.getPath("notes.csv")));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void userPrincipalLookupServiceIsUnsupported() {
        new S3FileSystem("example-bucket").getUserPrincipalLookupService();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newWatchServiceIsUnsupported() {
        new S3FileSystem("example-bucket").newWatchService();
    }

    @Test
    public void filesystemsWithSameBucketAndProviderClassAreEqual() {
        S3FileSystem first = new S3FileSystem("s3://example-bucket", new S3FileSystemProvider());
        S3FileSystem second = new S3FileSystem("s3://example-bucket", new S3FileSystemProvider());

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void filesystemsWithDifferentBucketsAreNotEqual() {
        S3FileSystem first = new S3FileSystem("first-bucket");
        S3FileSystem second = new S3FileSystem("second-bucket");

        assertNotEquals(first, second);
    }

    @Test
    public void filesystemIsNotEqualToNullOrDifferentType() {
        S3FileSystem fileSystem = new S3FileSystem("example-bucket");

        assertNotEquals(fileSystem, null);
        assertNotEquals(fileSystem, "example-bucket");
    }
}
