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
    public void cosineMarginReturnsCosineSimilarity() {
        assertEquals(0.8f, ANNIndex.cosineMargin(new double[] {3.0, 4.0}, new double[] {4.0, 3.0}), 0.00001f);
    }

    @Test
    public void cosineMarginForOrthogonalVectorsIsZero() {
        assertEquals(0.0f, ANNIndex.cosineMargin(new double[] {1.0, 0.0}, new double[] {0.0, 1.0}), 0.0f);
    }

    @Test
    public void euclideanMarginIncludesBiasAndDotProduct() {
        assertEquals(14.5f, ANNIndex.euclideanMargin(new double[] {2.0, 3.0}, new double[] {4.0, 5.0},  -8.5f), 0.0f);
    }

    @Test
    public void euclideanMarginWithZeroVectorsReturnsBias() {
        assertEquals(2.5f, ANNIndex.euclideanMargin(new double[] {0.0, 0.0}, new double[] {1.0, 2.0}, 2.5f), 0.0f);
    }

    @Test
    public void loadsSingleAngularNodeAndReadsItsVector() throws Exception {
        File file = createAngularIndex(new float[] {1.25f, -2.5f}, 1);
        try {
            ANNIndex index = new ANNIndex(2, file.getAbsolutePath());
            try {
                assertArrayEquals(new double[] {1.25, -2.5}, index.getItemVector(0), 0.0);
                assertEquals(Collections.singletonList(0), index.getNearest(new double[] {1.25, -2.5}, 1));
            } finally {
                index.close();
            }
        } finally {
            file.delete();
        }
    }

    @Test
    public void getNodeVectorWritesIntoProvidedArray() throws Exception {
        File file = createAngularIndex(new float[] {3.0f, 4.0f}, 1);
        try {
            ANNIndex index = new ANNIndex(2, file.getAbsolutePath());
            try {
                double[] vector = new double[2];
                index.getNodeVector(0L, vector);
                assertArrayEquals(new double[] {3.0, 4.0}, vector, 0.0);
            } finally {
                index.close();
            }
        } finally {
            file.delete();
        }
    }

    @Test
    public void getNearestRejectsQueryWithWrongDimension() throws Exception {
        File file = createAngularIndex(new float[] {1.0f, 2.0f}, 1);
        try {
            final ANNIndex index = new ANNIndex(2, file.getAbsolutePath());
            try {
                index.getNearest(new double[] {1.0}, 1);
            } catch (RuntimeException expected) {
                assertTrue(expected.getMessage().contains("queryVector must be size of 2"));
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
    public void rejectsEmptyIndexFile() throws Exception {
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

    private File createAngularIndex(float[] vector, int descendants) throws IOException {
        File file = File.createTempFile("ann-index", ".bin");
        FileOutputStream output = new FileOutputStream(file);
        try {
            writeInt(output, descendants);
            writeInt(output, 0);
            writeInt(output, 0);
            for (float value : vector) {
                writeFloat(output, value);
            }
        } finally {
            output.close();
        }
        return file;
    }

    private void writeInt(FileOutputStream output, int value) throws IOException {
        output.write(value & 0xff);
        output.write((value >>> 8) & 0xff);
        output.write((value >>> 16) & 0xff);
        output.write((value >>> 24) & 0xff);
    }

    private void writeFloat(FileOutputStream output, float value) throws IOException {
        writeInt(output, Float.floatToIntBits(value));
    }
}
