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

    private File createAngularIndex() throws IOException {
        File file = File.createTempFile("ann-index", ".bin");
        file.deleteOnExit();

        ByteBuffer data = ByteBuffer.allocate(3 * 20).order(ByteOrder.LITTLE_ENDIAN);
        // Two-item leaf containing items 0 and 1.
        data.putInt(0, 2);
        data.putInt(4, 0);
        data.putInt(8, 1);
        data.putFloat(12, 0.0f);
        data.putFloat(16, 0.0f);

        // Item 0: [1, 0]
        data.putInt(20, 1);
        data.putFloat(32, 1.0f);
        data.putFloat(36, 0.0f);

        // Item 1: [0, 1]
        data.putInt(40, 1);
        data.putFloat(52, 0.0f);
        data.putFloat(56, 1.0f);

        FileOutputStream output = new FileOutputStream(file);
        try {
            output.write(data.array());
        } finally {
            output.close();
        }
        return file;
    }

    @Test
    public void cosineMarginReturnsNormalizedDotProduct() {
        assertEquals(0.6f, ANNIndex.cosineMargin(
                new double[]{3.0, 4.0}, new double[]{4.0, 3.0}), 0.000001f);
    }

    @Test
    public void cosineMarginForOrthogonalVectorsIsZero() {
        assertEquals(0.0f, ANNIndex.cosineMargin(
                new double[]{1.0, 0.0}, new double[]{0.0, 1.0}), 0.0f);
    }

    @Test
    public void euclideanMarginAddsBiasAndDotProduct() {
        assertEquals(11.0f, ANNIndex.euclideanMargin(
                new double[]{1.0, 2.0}, new double[]{3.0, 4.0}, 2.0f), 0.0f);
    }

    @Test
    public void euclideanMarginWithEmptyVectorsReturnsBias() {
        assertEquals(2.5f, ANNIndex.euclideanMargin(
                new double[0], new double[0], 2.5f), 0.0f);
    }

    @Test
    public void itemVectorReadsVectorFromIndex() throws IOException {
        File file = createAngularIndex();
        ANNIndex index = new ANNIndex(2, file.getAbsolutePath());
        try {
            assertTrue(Arrays.equals(new double[]{1.0, 0.0}, index.getItemVector(0)));
        } finally {
            index.close();
            file.delete();
        }
    }

    @Test
    public void nodeVectorReadsVectorAtByteOffset() throws IOException {
        File file = createAngularIndex();
        ANNIndex index = new ANNIndex(2, file.getAbsolutePath());
        try {
            assertTrue(Arrays.equals(new double[]{0.0, 1.0}, index.getNodeVector(20L)));
        } finally {
            index.close();
            file.delete();
        }
    }

    @Test
    public void nearestReturnsItemsOrderedByCosineSimilarity() throws IOException {
        File file = createAngularIndex();
        ANNIndex index = new ANNIndex(2, file.getAbsolutePath());
        try {
            assertEquals(Arrays.asList(0, 1),
                    index.getNearest(new double[]{1.0, 0.0}, 2));
        } finally {
            index.close();
            file.delete();
        }
    }

    @Test
    public void nearestWithZeroResultsReturnsEmptyList() throws IOException {
        File file = createAngularIndex();
        ANNIndex index = new ANNIndex(2, file.getAbsolutePath());
        try {
            assertEquals(Collections.emptyList(),
                    index.getNearest(new double[]{1.0, 0.0}, 0));
        } finally {
            index.close();
            file.delete();
        }
    }

    @Test(expected = RuntimeException.class)
    public void nearestRejectsQueryWithWrongDimension() throws IOException {
        File file = createAngularIndex();
        ANNIndex index = new ANNIndex(2, file.getAbsolutePath());
        try {
            index.getNearest(new double[]{1.0}, 1);
        } finally {
            index.close();
            file.delete();
        }
    }
}
