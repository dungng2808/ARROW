package software.amazon.nio.spi.s3;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.net.URI;
import java.nio.channels.Channel;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Set;

public class S3FileSystem_ArrowTest {

    private S3FileSystemProvider provider;
    private S3FileSystem fileSystem;

    @Before
    public void setUp() {
        provider = new S3FileSystemProvider();
        fileSystem = new S3FileSystem("s3://test-bucket", provider);
    }

    @Test
    public void testConstructorWithUri() {
        URI uri = URI.create("s3://my-bucket");
        S3FileSystem fs = new S3FileSystem(uri, provider);
        assertEquals("my-bucket", fs.bucketName());
    }

    @Test
    public void testConstructorWithBucketName() {
        S3FileSystem fs = new S3FileSystem("my-bucket");
        assertEquals("my-bucket", fs.bucketName());
    }

    @Test
    public void testProvider() {
        assertEquals(provider, fileSystem.provider());
    }

    @Test
    public void testBucketName() {
        assertEquals("test-bucket", fileSystem.bucketName());
    }

    @Test
    public void testClose() throws Exception {
        assertFalse(fileSystem.isOpen());
        fileSystem.close();
        assertFalse(fileSystem.isOpen());
    }

    @Test
    public void testIsOpen() {
        assertFalse(fileSystem.isOpen());
    }

    @Test
    public void testIsReadOnly() {
        assertFalse(fileSystem.isReadOnly());
    }

    @Test
    public void testGetSeparator() {
        assertEquals("/", fileSystem.getSeparator());
    }

    @Test
    public void testGetRootDirectories() {
        Iterable<Path> roots = fileSystem.getRootDirectories();
        assertNotNull(roots);
        assertTrue(roots.iterator().hasNext());
        Path root = roots.iterator().next();
        assertEquals("/", root.toString());
    }

    @Test
    public void testGetFileStores() {
        Iterable<FileStore> stores = fileSystem.getFileStores();
        assertNotNull(stores);
        assertFalse(stores.iterator().hasNext());
    }

    @Test
    public void testSupportedFileAttributeViews() {
        Set<String> views = fileSystem.supportedFileAttributeViews();
        assertNotNull(views);
        assertTrue(views.contains("basic"));
        assertEquals(1, views.size());
    }

    @Test
    public void testGetPath() {
        Path path = fileSystem.getPath("/test/path");
        assertNotNull(path);
        assertEquals("/test/path", path.toString());
    }

    @Test
    public void testGetPathWithMultipleArgs() {
        Path path = fileSystem.getPath("test", "path", "file.txt");
        assertNotNull(path);
        assertEquals("/test/path/file.txt", path.toString());
    }

    @Test
    public void testGetPathMatcher() {
        PathMatcher matcher = fileSystem.getPathMatcher("glob:**.txt");
        assertNotNull(matcher);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetUserPrincipalLookupService() {
        fileSystem.getUserPrincipalLookupService();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testNewWatchService() {
        fileSystem.newWatchService();
    }

    @Test
    public void testGetOpenChannels() {
        Set<Channel> channels = fileSystem.getOpenChannels();
        assertNotNull(channels);
        assertTrue(channels.isEmpty());
    }

    @Test
    public void testRegisterOpenChannel() {
        S3SeekableByteChannel channel = new S3SeekableByteChannel(fileSystem, "test-key");
        fileSystem.registerOpenChannel(channel);
        Set<Channel> channels = fileSystem.getOpenChannels();
        assertNotNull(channels);
        assertTrue(channels.contains(channel));
    }

    @Test
    public void testEquals() {
        S3FileSystem fs1 = new S3FileSystem("s3://bucket1", provider);
        S3FileSystem fs2 = new S3FileSystem("s3://bucket1", provider);
        S3FileSystem fs3 = new S3FileSystem("s3://bucket2", provider);
        
        assertTrue(fs1.equals(fs1));
        assertTrue(fs1.equals(fs2));
        assertFalse(fs1.equals(fs3));
        assertFalse(fs1.equals(null));
        assertFalse(fs1.equals(new Object()));
    }

    @Test
    public void testHashCode() {
        S3FileSystem fs1 = new S3FileSystem("s3://bucket1", provider);
        S3FileSystem fs2 = new S3FileSystem("s3://bucket1", provider);
        assertEquals(fs1.hashCode(), fs2.hashCode());
    }
}
