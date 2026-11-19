package internal.com.spotify.annoy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class ANNIndex_ArrowTest {

    @Test
    public void cosineMarginReturnsCosineSimilarity() {
        assertEquals(1.0f, ANNIndex.cosineMargin(new double[] {3.0, 4.0}, new double[] {6.0, 8.0}), 0.00001f);
    }

    @Test
    public void cosineMarginForOrthogonalVectorsIsZero() {
        assertEquals(0.0f, ANNIndex.cosineMargin(new double[] {1.0, 0.0}, new double[] {0.0, 1.0}), 0.00001f);
    }

    @Test
    public void euclideanMarginIncludesBiasAndDotProduct() {
        assertEquals(8.0f, ANNIndex.euclideanMargin(new double[] {1.0, 2.0}, new double[] {3.0, 4.0}, 1.0f), 0.00001f);
    }

    @Test
    public void euclideanMarginWithZeroVectorsReturnsBias() {
        assertEquals(2.5f, ANNIndex.euclideanMargin(new double[] {0.0, 0.0}, new double[] {1.0, 1.0}, 2.5f), 0.00001f);
    }

    @Test
    public void loadsItemVectorsAndFindsNearestItems() throws IOException {
        File file = createAngularLeafIndex();
        try {
            ANNIndex index = new ANNIndex(1, file.getAbsolutePath());
            try {
                assertEquals(Arrays.asList(0.0), Arrays.asList(index.getItemVector(0)[0]));
                assertEquals(Arrays.asList(1.0), Arrays.asList(index.getNodeVector(16L)[0]));
                assertEquals(Arrays.asList(0), index.getNearest(new double[] {1.0}, 1));
            } finally {
                index.close();
            }
        } finally {
            file.delete();
        }
    }

    @Test
    public void nearestReturnsEmptyWhenNoResultsRequested() throws IOException {
        File file = createAngularLeafIndex();
        try {
            ANNIndex index = new ANNIndex(1, file.getAbsolutePath());
            try {
                assertTrue(index.getNearest(new double[] {1.0}, 0).isEmpty());
            } finally {
                index.close();
            }
        } finally {
            file.delete();
        }
    }

    @Test(expected = RuntimeException.class)
    public void nearestRejectsQueryWithWrongDimension() throws IOException {
        File file = createAngularLeafIndex();
        try {
            ANNIndex index = new ANNIndex(1, file.getAbsolutePath());
            try {
                index.getNearest(new double[] {1.0, 2.0}, 1);
            } finally {
                index.close();
            }
        } finally {
            file.delete();
        }
    }

    @Test(expected = IOException.class)
    public void rejectsEmptyIndexFile() throws IOException {
        File file = File.createTempFile("ann-index", ".bin");
        try {
            new ANNIndex(1, file.getAbsolutePath());
        } finally {
            file.delete();
        }
    }

    @Test(expected = RuntimeException.class)
    public void rejectsFileWithWrongDimension() throws IOException {
        File file = File.createTempFile("ann-index", ".bin");
        try {
            FileOutputStream output = new FileOutputStream(file);
            try {
                output.write(new byte[16]);
            } finally {
                output.close();
            }
            new ANNIndex(2, file.getAbsolutePath());
        } finally {
            file.delete();
        }
    }

    private File createAngularLeafIndex() throws IOException {
        File file = File.createTempFile("ann-index", ".bin");
        FileOutputStream output = new FileOutputStream(file);
        try {
            // Two 16-byte nodes: a leaf containing item indices 0 and 1.
            writeInt(output, 2);
            writeInt(output, 0);
            writeInt(output, 1);
            writeFloat(output, 0.0f);

            writeInt(output, 1);
            writeInt(output, 0);
            writeFloat(output, 0.0f);
            writeFloat(output, 1.0f);
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
