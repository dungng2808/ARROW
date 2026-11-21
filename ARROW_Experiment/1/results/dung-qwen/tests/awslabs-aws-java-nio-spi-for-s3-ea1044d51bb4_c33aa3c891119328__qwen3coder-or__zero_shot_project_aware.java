package software.amazon.nio.spi.s3;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.Channel;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class S3FileSystem_ArrowTest {

    private S3FileSystemProvider provider;
    private S3FileSystem fileSystem;

    @Before
    public void setUp() {
        provider = new S3FileSystemProvider();
        fileSystem = new S3FileSystem("test-bucket", provider);
    }

    @Test
    public void testConstructorWithUriString() {
        S3FileSystem fs = new S3FileSystem("s3://test-bucket", provider);
        assertEquals("test-bucket", fs.bucketName());
    }

    @Test
    public void testConstructorWithUri() {
        URI uri = URI.create("s3://test-bucket");
        S3FileSystem fs = new S3FileSystem(uri, provider);
        assertEquals("test-bucket", fs.bucketName());
    }

    @Test
    public void testConstructorWithBucketName() {
        S3FileSystem fs = new S3FileSystem("test-bucket");
        assertEquals("test-bucket", fs.bucketName());
    }

    @Test
    public void testProvider() {
        FileSystemProvider p = fileSystem.provider();
        assertNotNull(p);
        assertEquals(provider, p);
    }

    @Test
    public void testBucketName() {
        assertEquals("test-bucket", fileSystem.bucketName());
    }

    @Test
    public void testClose() throws IOException {
        S3SeekableByteChannel channel = Mockito.mock(S3SeekableByteChannel.class);
        when(channel.isOpen()).thenReturn(true);
        fileSystem.registerOpenChannel(channel);

        fileSystem.close();

        assertFalse(fileSystem.isOpen());
        verify(channel).close();
        assertTrue(fileSystem.getOpenChannels().isEmpty());
    }

    @Test
    public void testCloseAlreadyClosed() throws IOException {
        fileSystem.close();
        fileSystem.close(); // Should not throw exception
        assertFalse(fileSystem.isOpen());
    }

    @Test
    public void testIsOpen() {
        assertTrue(fileSystem.isOpen());
        fileSystem.close();
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
        assertNotNull(root);
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
        assertEquals(1, views.size());
        assertTrue(views.contains("basic"));
    }

    @Test
    public void testGetPath() {
        Path path = fileSystem.getPath("/test/path");
        assertNotNull(path);
        assertTrue(path instanceof S3Path);
        assertEquals("/test/path", path.toString());
    }

    @Test
    public void testGetPathMultipleArgs() {
        Path path = fileSystem.getPath("/test", "path", "file.txt");
        assertNotNull(path);
        assertTrue(path instanceof S3Path);
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
        S3SeekableByteChannel channel = Mockito.mock(S3SeekableByteChannel.class);
        fileSystem.registerOpenChannel(channel);
        Set<Channel> channels = fileSystem.getOpenChannels();
        assertNotNull(channels);
        assertEquals(1, channels.size());
        assertTrue(channels.contains(channel));
    }

    @Test
    public void testDeregisterClosedChannel() {
        S3SeekableByteChannel channel = Mockito.mock(S3SeekableByteChannel.class);
        when(channel.isOpen()).thenReturn(false);
        fileSystem.registerOpenChannel(channel);
        boolean removed = fileSystem.deregisterClosedChannel(channel);
        assertTrue(removed);
        assertTrue(fileSystem.getOpenChannels().isEmpty());
    }

    @Test
    public void testEqualsSameObject() {
        assertTrue(fileSystem.equals(fileSystem));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(fileSystem.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(fileSystem.equals(new Object()));
    }

    @Test
    public void testEqualsSameBucketAndProvider() {
        S3FileSystem otherFs = new S3FileSystem("test-bucket", provider);
        assertTrue(fileSystem.equals(otherFs));
    }

    @Test
    public void testEqualsDifferentBucket() {
        S3FileSystem otherFs = new S3FileSystem("other-bucket", provider);
        assertFalse(fileSystem.equals(otherFs));
    }

    @Test
    public void testEqualsDifferentProvider() {
        S3FileSystemProvider otherProvider = new S3FileSystemProvider();
        S3FileSystem otherFs = new S3FileSystem("test-bucket", otherProvider);
        assertFalse(fileSystem.equals(otherFs));
    }

    @Test
    public void testHashCode() {
        int hash = fileSystem.hashCode();
        assertTrue(hash != 0);
    }
}
