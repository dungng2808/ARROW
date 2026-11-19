package internal.com.spotify.annoy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import org.junit.Test;

public class ANNIndex_ArrowTest {

    @Test
    public void cosineMarginReturnsNormalizedDotProduct() {
        assertEquals(0.6f, ANNIndex.cosineMargin(
                new double[] {3.0, 4.0}, new double[] {4.0, 3.0}), 0.000001f);
    }

    @Test
    public void cosineMarginForOrthogonalVectorsIsZero() {
        assertEquals(0.0f, ANNIndex.cosineMargin(
                new double[] {1.0, 0.0}, new double[] {0.0, 1.0}), 0.0f);
    }

    @Test
    public void euclideanMarginAddsBiasAndDotProduct() {
        assertEquals(12.0f, ANNIndex.euclideanMargin(
                new double[] {2.0, 3.0}, new double[] {4.0, 2.0}, 1.0f), 0.0f);
    }

    @Test
    public void euclideanMarginWithEmptyVectorsReturnsBias() {
        assertEquals(2.5f, ANNIndex.euclideanMargin(
                new double[0], new double[0], 2.5f), 0.0f);
    }

    @Test
    public void readsItemVectorAndReturnsNoNearestItemsForEmptyIndex() throws Exception {
        File file = createAngularIndex();
        try {
            ANNIndex index = new ANNIndex(1, file.getAbsolutePath());
            try {
                assertArrayEquals(new double[] {2.0}, index.getItemVector(0), 0.0);
                double[] vector = new double[1];
                index.getItemVector(0, vector);
                assertArrayEquals(new double[] {2.0}, vector);
                assertArrayEquals(new double[] {2.0}, index.getNodeVector(0), 0.0);

                List<Integer> nearest = index.getNearest(new double[] {2.0}, 1);
                assertTrue(nearest.isEmpty());
            } finally {
                index.close();
            }
        } finally {
            file.delete();
        }
    }

    @Test
    public void rejectsQueryWithWrongDimension() throws Exception {
        File file = createAngularIndex();
        try {
            ANNIndex index = new ANNIndex(1, file.getAbsolutePath());
            try {
                index.getNearest(new double[] {1.0, 2.0}, 1);
            } catch (RuntimeException expected) {
                assertEquals("queryVector must be size of 1, but was 2", expected.getMessage());
                return;
            } finally {
                index.close();
            }
            throw new AssertionError("Expected a RuntimeException");
        } finally {
            file.delete();
        }
    }

    @Test
    public void rejectsZeroByteIndexFile() throws Exception {
        File file = File.createTempFile("annoy-empty", ".index");
        try {
            try {
                new ANNIndex(1, file.getAbsolutePath());
            } catch (IOException expected) {
                assertEquals("Index is a 0-byte file?", expected.getMessage());
                return;
            }
            throw new AssertionError("Expected an IOException");
        } finally {
            file.delete();
        }
    }

    private File createAngularIndex() throws IOException {
        File file = File.createTempFile("annoy-index", ".index");
        ByteBuffer buffer = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(Float.floatToIntBits(2.0f));
        buffer.putInt(0);
        FileOutputStream output = new FileOutputStream(file);
        try {
            output.write(buffer.array());
        } finally {
            output.close();
        }
        return file;
    }
}
