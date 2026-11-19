package internal.com.spotify.annoy;

import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ANNIndex_ArrowTest {

    @Test
    public void cosineMarginReturnsCosineSimilarity() {
        assertEquals(ANNIndex.cosineMargin(new double[]{1.0, 0.0},
                new double[]{0.0, 1.0}), 0.0f, 0.0f);
        assertEquals(ANNIndex.cosineMargin(new double[]{3.0, 4.0},
                new double[]{3.0, 4.0}), 1.0f, 0.0f);
    }

    @Test
    public void euclideanMarginAddsBiasAndDotProduct() {
        assertEquals(ANNIndex.euclideanMargin(new double[]{1.0, 2.0},
                new double[]{3.0, 4.0}, 5.0f), 16.0f, 0.0f);
    }

    @Test
    public void loadsLeafIndexAndReturnsItsItemVectorAndNearestItem() throws Exception {
        File file = createAngularLeafIndex();
        try {
            ANNIndex index = new ANNIndex(2, file.getAbsolutePath());
            try {
                assertEquals(index.getItemVector(0), new double[]{1.0, 0.0});
                assertEquals(index.getItemVector(1), new double[]{0.0, 1.0});
                assertEquals(index.getNearest(new double[]{1.0, 0.0}, 2),
                        Arrays.asList(0, 1));
            } finally {
                index.close();
            }
        } finally {
            file.delete();
        }
    }

    @Test
    public void returnsEmptyNearestListWhenNoResultsRequested() throws Exception {
        File file = createAngularLeafIndex();
        try {
            ANNIndex index = new ANNIndex(2, file.getAbsolutePath());
            try {
                List<Integer> result = index.getNearest(new double[]{1.0, 0.0}, 0);
                assertTrue(result.isEmpty());
            } finally {
                index.close();
            }
        } finally {
            file.delete();
        }
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void rejectsQueryWithWrongDimension() throws Exception {
        File file = createAngularLeafIndex();
        try {
            ANNIndex index = new ANNIndex(2, file.getAbsolutePath());
            try {
                index.getNearest(new double[]{1.0}, 1);
            } finally {
                index.close();
            }
        } finally {
            file.delete();
        }
    }

    @Test(expectedExceptions = IOException.class)
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
        ByteBuffer buffer = ByteBuffer.allocate(20 * 3).order(ByteOrder.LITTLE_ENDIAN);

        // Leaf node containing two item offsets.
        buffer.putInt(0, 2);
        buffer.putInt(4, 0);
        buffer.putInt(8, 1);
        buffer.putFloat(12, 1.0f);
        buffer.putFloat(16, 0.0f);

        // Item vectors occupy the first two nodes.
        buffer.putInt(20, 1);
        buffer.putFloat(32, 1.0f);
        buffer.putFloat(36, 0.0f);

        buffer.putInt(40, 1);
        buffer.putFloat(52, 0.0f);
        buffer.putFloat(56, 1.0f);

        FileOutputStream output = new FileOutputStream(file);
        try {
            output.write(buffer.array());
        } finally {
            output.close();
        }
        return file;
    }
}
