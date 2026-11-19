package internal.com.spotify.annoy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;

public class ANNIndex_ArrowTest {

    @Test
    public void cosineMarginForAlignedVectorsIsOne() {
        assertEquals(1.0f, ANNIndex.cosineMargin(
                new double[] {3.0, 4.0},
                new double[] {6.0, 8.0}), 0.000001f);
    }

    @Test
    public void cosineMarginForOrthogonalVectorsIsZero() {
        assertEquals(0.0f, ANNIndex.cosineMargin(
                new double[] {1.0, 0.0},
                new double[] {0.0, 1.0}), 0.000001f);
    }

    @Test
    public void euclideanMarginIncludesBiasAndDotProduct() {
        assertEquals(8.0f, ANNIndex.euclideanMargin(
                new double[] {1.0, 2.0},
                new double[] {3.0, 4.0},
                1.0f), 0.000001f);
    }

    @Test
    public void euclideanMarginWithZeroVectorsReturnsBias() {
        assertEquals(2.5f, ANNIndex.euclideanMargin(
                new double[] {0.0, 0.0},
                new double[] {5.0, -3.0},
                2.5f), 0.000001f);
    }

    @Test
    public void getNearestReturnsEmptyForIndexWithNoRoots() throws Exception {
        File file = createIndexFile(20);
        try {
            ANNIndex index = new ANNIndex(1, file.getAbsolutePath(), IndexType.ANGULAR);
            try {
                assertEquals(0, index.getNearest(new double[] {1.0}, 3).size());
            } finally {
                index.close();
            }
        } finally {
            file.delete();
        }
    }

    @Test
    public void getNearestRejectsQueryWithWrongDimension() throws Exception {
        File file = createIndexFile(20);
        try {
            final ANNIndex index = new ANNIndex(1, file.getAbsolutePath(), IndexType.ANGULAR);
            try {
                index.getNearest(new double[] {1.0, 2.0}, 1);
            } finally {
                index.close();
            }
        } catch (RuntimeException expected) {
            assertEquals("queryVector must be size of 1, but was 2", expected.getMessage());
        } finally {
            file.delete();
        }
    }

    @Test
    public void getItemVectorReadsVectorFromIndex() throws Exception {
        File file = createIndexFile(20);
        try {
            ANNIndex index = new ANNIndex(1, file.getAbsolutePath(), IndexType.ANGULAR);
            try {
                assertArrayEquals(new double[] {0.0}, index.getItemVector(0), 0.0);
            } finally {
                index.close();
            }
        } finally {
            file.delete();
        }
    }

    private File createIndexFile(int size) throws IOException {
        File file = File.createTempFile("ann-index-test", ".bin");
        FileOutputStream output = new FileOutputStream(file);
        try {
            for (int i = 0; i < size; i++) {
                output.write(0);
            }
        } finally {
            output.close();
        }
        return file;
    }
}
