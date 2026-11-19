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
                new double[] {0.0, 1.0}), 0.0f);
    }

    @Test
    public void euclideanMarginIncludesBiasAndDotProduct() {
        assertEquals(12.0f, ANNIndex.euclideanMargin(
                new double[] {2.0, 3.0},
                new double[] {4.0, 5.0},
                -11.0f), 0.0f);
    }

    @Test
    public void euclideanMarginWithZeroVectorsReturnsBias() {
        assertEquals(2.5f, ANNIndex.euclideanMargin(
                new double[] {0.0, 0.0},
                new double[] {7.0, -3.0},
                2.5f), 0.0f);
    }

    @Test
    public void readsItemVectorFromAngularIndex() throws IOException {
        File file = createAngularIndex();
        try {
            ANNIndex index = new ANNIndex(1, file.getAbsolutePath());
            try {
                assertArrayEquals(new double[] {2.5}, index.getItemVector(0), 0.0);
            } finally {
                index.close();
            }
        } finally {
            file.delete();
        }
    }

    @Test
    public void nearestQueryReturnsLeafItem() throws IOException {
        File file = createAngularIndex();
        try {
            ANNIndex index = new ANNIndex(1, file.getAbsolutePath());
            try {
                assertEquals(java.util.Collections.singletonList(0),
                        index.getNearest(new double[] {1.0}, 1));
            } finally {
                index.close();
            }
        } finally {
            file.delete();
        }
    }

    @Test(expected = RuntimeException.class)
    public void nearestQueryRejectsWrongDimension() throws IOException {
        File file = createAngularIndex();
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
        File file = File.createTempFile("ann-index-empty", ".bin");
        try {
            new ANNIndex(1, file.getAbsolutePath());
        } finally {
            file.delete();
        }
    }

    private File createAngularIndex() throws IOException {
        File file = File.createTempFile("ann-index", ".bin");
        FileOutputStream output = new FileOutputStream(file);
        try {
            // Angular dimension-1 nodes are 16 bytes: 12-byte header and one float.
            // A single leaf node with one descendant has item index 0.
            writeInt(output, 1);
            writeInt(output, 0);
            writeInt(output, 0);
            writeFloat(output, 2.5f);
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
