package internal.com.spotify.annoy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class ANNIndex_ArrowTest {

    @Test
    public void cosineMarginForOrthogonalVectorsIsZero() {
        assertEquals(0.0f, ANNIndex.cosineMargin(
                new double[] {1.0, 0.0},
                new double[] {0.0, 1.0}), 0.0f);
    }

    @Test
    public void cosineMarginForIdenticalVectorsIsOne() {
        assertEquals(1.0f, ANNIndex.cosineMargin(
                new double[] {3.0, 4.0},
                new double[] {3.0, 4.0}), 0.0f);
    }

    @Test
    public void euclideanMarginIncludesBiasAndDotProduct() {
        assertEquals(12.0f, ANNIndex.euclideanMargin(
                new double[] {1.0, 2.0},
                new double[] {3.0, 4.0},
                1.0f), 0.0f);
    }

    @Test
    public void loadsAndReadsAngularItemVector() throws IOException {
        File file = createIndexFile(20);
        try {
            ANNIndex index = new ANNIndex(2, file.getAbsolutePath());
            try {
                assertArrayEquals(new double[] {1.5, -2.0}, index.getItemVector(0), 0.0);
                assertArrayEquals(new double[] {0.0, 3.25}, index.getItemVector(1), 0.0);
            } finally {
                index.close();
            }
        } finally {
            file.delete();
        }
    }

    @Test
    public void nearestReturnsItemsFromLeaf() throws IOException {
        File file = createIndexFile(20);
        try {
            ANNIndex index = new ANNIndex(2, file.getAbsolutePath());
            try {
                assertEquals(Arrays.asList(0, 1), index.getNearest(new double[] {1.0, 0.0}, 2));
            } finally {
                index.close();
            }
        } finally {
            file.delete();
        }
    }

    @Test
    public void rejectsQueryWithWrongDimension() throws IOException {
        File file = createIndexFile(20);
        try {
            final ANNIndex index = new ANNIndex(2, file.getAbsolutePath());
            try {
                index.getNearest(new double[] {1.0}, 1);
            } catch (RuntimeException expected) {
                assertEquals("queryVector must be size of 2, but was 1", expected.getMessage());
                return;
            } finally {
                index.close();
            }
            throw new AssertionError("Expected RuntimeException");
        } finally {
            file.delete();
        }
    }

    @Test
    public void rejectsEmptyIndexFile() throws IOException {
        File file = File.createTempFile("ann-index", ".bin");
        try {
            try {
                new ANNIndex(2, file.getAbsolutePath());
            } catch (IOException expected) {
                assertEquals("Index is a 0-byte file?", expected.getMessage());
                return;
            }
            throw new AssertionError("Expected IOException");
        } finally {
            file.delete();
        }
    }

    private File createIndexFile(int size) throws IOException {
        File file = File.createTempFile("ann-index", ".bin");
        FileOutputStream output = new FileOutputStream(file);
        try {
            byte[] data = new byte[size];
            // Angular dimension-2 nodes are 20 bytes: header, two child indices, and two floats.
            // One leaf node contains two item indices.
            putInt(data, 0, 2);
            putInt(data, 4, 0);
            putInt(data, 8, 1);
            putFloat(data, 12, 1.5f);
            putFloat(data, 16, -2.0f);
            putInt(data, 20, 1);
            putFloat(data, 32, 0.0f);
            putFloat(data, 36, 3.25f);
            output.write(data);
        } finally {
            output.close();
        }
        return file;
    }

    private void putInt(byte[] data, int offset, int value) {
        data[offset] = (byte) value;
        data[offset + 1] = (byte) (value >>> 8);
        data[offset + 2] = (byte) (value >>> 16);
        data[offset + 3] = (byte) (value >>> 24);
    }

    private void putFloat(byte[] data, int offset, float value) {
        putInt(data, offset, Float.floatToIntBits(value));
    }
}
