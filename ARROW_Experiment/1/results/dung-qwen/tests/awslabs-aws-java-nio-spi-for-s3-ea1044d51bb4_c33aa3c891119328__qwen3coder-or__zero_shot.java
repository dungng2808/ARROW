package software.amazon.nio.spi.s3;

import org.junit.Test;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.Channel;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Set;
import static org.junit.Assert.*;

public class S3FileSystem_ArrowTest {

    @Test
    public void testConstructorWithUriString() {
        S3FileSystemProvider provider = new S3FileSystemProvider();
        S3FileSystem fs = new S3FileSystem("s3://test-bucket", provider);
        assertEquals("test-bucket", fs.bucketName());
        assertEquals(provider, fs.provider());
    }

    @Test
    public void testConstructorWithURI() {
        S3FileSystemProvider provider = new S3FileSystemProvider();
        URI uri = URI.create("s3://test-bucket");
        S3FileSystem fs = new S3FileSystem(uri, provider);
        assertEquals("test-bucket", fs.bucketName());
        assertEquals(provider, fs.provider());
    }

    @Test
    public void testConstructorWithBucketName() {
        S3FileSystem fs = new S3FileSystem("test-bucket");
        assertEquals("test-bucket", fs.bucketName());
    }

    @Test
    public void testProvider() {
        S3FileSystemProvider provider = new S3FileSystemProvider();
        S3FileSystem fs = new S3FileSystem("s3://test-bucket", provider);
        assertEquals(provider, fs.provider());
    }

    @Test
    public void testBucketName() {
        S3FileSystem fs = new S3FileSystem("s3://test-bucket", new S3FileSystemProvider());
        assertEquals("test-bucket", fs.bucketName());
    }

    @Test
    public void testClose() throws IOException {
        S3FileSystemProvider provider = new S3FileSystemProvider();
        S3FileSystem fs = new S3FileSystem("s3://test-bucket", provider);
        assertTrue(fs.isOpen());
        
        fs.close();
        assertFalse(fs.isOpen());
    }

    @Test
    public void testIsOpen() {
        S3FileSystemProvider provider = new S3FileSystemProvider();
        S3FileSystem fs = new S3FileSystem("s3://test-bucket", provider);
        assertTrue(fs.isOpen());
        
        fs.close();
        assertFalse(fs.isOpen());
    }

    @Test
    public void testIsReadOnly() {
        S3FileSystem fs = new S3FileSystem("s3://test-bucket", new S3FileSystemProvider());
        assertFalse(fs.isReadOnly());
    }

    @Test
    public void testGetSeparator() {
        S3FileSystem fs = new S3FileSystem("s3://test-bucket", new S3FileSystemProvider());
        assertEquals("/", fs.getSeparator());
    }

    @Test
    public void testGetRootDirectories() {
        S3FileSystem fs = new S3FileSystem("s3://test-bucket", new S3FileSystemProvider());
        Iterable<Path> roots = fs.getRootDirectories();
        assertNotNull(roots);
        
        for (Path root : roots) {
            assertEquals("/", root.toString());
        }
    }

    @Test
    public void testGetFileStores() {
        S3FileSystem fs = new S3FileSystem("s3://test-bucket", new S3FileSystemProvider());
        Iterable<FileStore> stores = fs.getFileStores();
        assertNotNull(stores);
        assertFalse(stores.iterator().hasNext());
    }

    @Test
    public void testSupportedFileAttributeViews() {
        S3FileSystem fs = new S3FileSystem("s3://test-bucket", new S3FileSystemProvider());
        Set<String> views = fs.supportedFileAttributeViews();
        assertNotNull(views);
        assertTrue(views.contains("basic"));
        assertEquals(1, views.size());
    }

    @Test
    public void testGetPath() {
        S3FileSystem fs = new S3FileSystem("s3://test-bucket", new S3FileSystemProvider());
        Path path = fs.getPath("/test/path");
        assertNotNull(path);
        assertEquals("/test/path", path.toString());
    }

    @Test
    public void testGetPathMatcher() {
        S3FileSystem fs = new S3FileSystem("s3://test-bucket", new S3FileSystemProvider());
        PathMatcher matcher = fs.getPathMatcher("glob:**.txt");
        assertNotNull(matcher);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetUserPrincipalLookupService() {
        S3FileSystem fs = new S3FileSystem("s3://test-bucket", new S3FileSystemProvider());
        fs.getUserPrincipalLookupService();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testNewWatchService() {
        S3FileSystem fs = new S3FileSystem("s3://test-bucket", new S3FileSystemProvider());
        fs.newWatchService();
    }

    @Test
    public void testGetOpenChannels() {
        S3FileSystem fs = new S3FileSystem("s3://test-bucket", new S3FileSystemProvider());
        Set<Channel> channels = fs.getOpenChannels();
        assertNotNull(channels);
        assertTrue(channels.isEmpty());
    }

    @Test
    public void testRegisterOpenChannel() {
        S3FileSystem fs = new S3FileSystem("s3://test-bucket", new S3FileSystemProvider());
        S3SeekableByteChannel channel = new S3SeekableByteChannel(fs, "test-key");
        fs.registerOpenChannel(channel);
        Set<Channel> channels = fs.getOpenChannels();
        assertTrue(channels.contains(channel));
    }

    @Test
    public void testDeregisterClosedChannel() {
        S3FileSystem fs = new S3FileSystem("s3://test-bucket", new S3FileSystemProvider());
        S3SeekableByteChannel channel = new S3SeekableByteChannel(fs, "test-key");
        fs.registerOpenChannel(channel);
        channel.close();
        boolean removed = fs.deregisterClosedChannel(channel);
        assertTrue(removed);
        Set<Channel> channels = fs.getOpenChannels();
        assertFalse(channels.contains(channel));
    }

    @Test
    public void testEquals() {
        S3FileSystemProvider provider1 = new S3FileSystemProvider();
        S3FileSystemProvider provider2 = new S3FileSystemProvider();
        S3FileSystem fs1 = new S3FileSystem("s3://test-bucket", provider1);
        S3FileSystem fs2 = new S3FileSystem("s3://test-bucket", provider1);
        S3FileSystem fs3 = new S3FileSystem("s3://test-bucket", provider2);
        S3FileSystem fs4 = new S3FileSystem("s3://other-bucket", provider1);

        assertEquals(fs1, fs2);
        assertNotEquals(fs1, fs3);
        assertNotEquals(fs1, fs4);
        assertNotEquals(fs1, null);
        assertNotEquals(fs1, new Object());
    }

    @Test
    public void testHashCode() {
        S3FileSystemProvider provider1 = new S3FileSystemProvider();
        S3FileSystemProvider provider2 = new S3FileSystemProvider();
        S3FileSystem fs1 = new S3FileSystem("s3://test-bucket", provider1);
        S3FileSystem fs2 = new S3FileSystem("s3://test-bucket", provider1);
        S3FileSystem fs3 = new S3FileSystem("s3://test-bucket", provider2);

        assertEquals(fs1.hashCode(), fs2.hashCode());
        assertNotEquals(fs1.hashCode(), fs3.hashCode());
    }
}
