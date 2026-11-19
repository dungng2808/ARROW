package internal.com.spotify.annoy;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ANNIndex_ArrowTest {

    @Test
    public void cosineMarginReturnsNormalizedDotProduct() {
        assertEquals(0.8f, ANNIndex.cosineMargin(
                new double[]{3.0, 4.0},
                new double[]{4.0, 3.0}), 0.00001f);
    }

    @Test
    public void cosineMarginForOrthogonalVectorsIsZero() {
        assertEquals(0.0f, ANNIndex.cosineMargin(
                new double[]{1.0, 0.0},
                new double[]{0.0, 1.0}), 0.0f);
    }

    @Test
    public void euclideanMarginIncludesBias() {
        assertEquals(11.0f, ANNIndex.euclideanMargin(
                new double[]{2.0, 3.0},
                new double[]{4.0, 1.0},
                -1.0f), 0.0f);
    }

    @Test
    public void euclideanMarginWithZeroVectorsReturnsBias() {
        assertEquals(2.5f, ANNIndex.euclideanMargin(
                new double[]{0.0, 0.0},
                new double[]{5.0, 7.0},
                2.5f), 0.0f);
    }

    @Test
    public void readsItemVectorAndFindsNearestItem() throws Exception {
        File file = createAngularLeafIndex();
        try {
            ANNIndex index = new ANNIndex(2, file.getAbsolutePath());
            try {
                assertTrue(Arrays.equals(new double[]{1.0, 0.0}, index.getItemVector(0)));
                assertEquals(Collections.singletonList(0), index.getNearest(new double[]{1.0, 0.0}, 1));
            } finally {
                index.close();
            }
        } finally {
            file.delete();
        }
    }

    @Test
    public void rejectsQueryWithWrongDimension() throws Exception {
        File file = createAngularLeafIndex();
        try {
            final ANNIndex index = new ANNIndex(2, file.getAbsolutePath());
            try {
                index.getNearest(new double[]{1.0}, 1);
            } catch (RuntimeException expected) {
                assertEquals("queryVector must be size of 2, but was 1", expected.getMessage());
                return;
            } finally {
                index.close();
            }
            throw new AssertionError("Expected a RuntimeException");
        } finally {
            file.delete();
        }
    }

    @Test(expected = IOException.class)
    public void rejectsEmptyIndexFile() throws Exception {
        File file = File.createTempFile("ann-index-empty", ".bin");
        try {
            new ANNIndex(2, file.getAbsolutePath());
        } finally {
            file.delete();
        }
    }

    private File createAngularLeafIndex() throws IOException {
        File file = File.createTempFile("ann-index-leaf", ".bin");
        ByteBuffer buffer = ByteBuffer.allocate(2 * 20).order(ByteOrder.LITTLE_ENDIAN);

        // A leaf containing two item indices.
        buffer.putInt(0, 2);
        buffer.putInt(4, 0);
        buffer.putInt(8, 1);
        buffer.putFloat(12, 0.0f);
        buffer.putFloat(16, 0.0f);

        // Item vectors stored in the two following nodes.
        buffer.putInt(20, 1);
        buffer.putFloat(32, 1.0f);
        buffer.putFloat(36, 0.0f);

        // The file must contain complete nodes; use three nodes for the leaf and its items.
        ByteBuffer complete = ByteBuffer.allocate(3 * 20).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < buffer.capacity(); i++) {
            complete.put(i, buffer.get(i));
        }
        complete.putInt(40, 1);
        complete.putFloat(52, 0.0f);
        complete.putFloat(56, 1.0f);

        FileOutputStream output = new FileOutputStream(file);
        try {
            output.write(complete.array());
        } finally {
            output.close();
        }
        return file;
    }
}
