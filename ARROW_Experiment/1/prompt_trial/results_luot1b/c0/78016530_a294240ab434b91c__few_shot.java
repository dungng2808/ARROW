package internal.com.spotify.annoy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ANNIndex_ArrowTest {

    @Test
    public void cosineMarginForParallelVectorsIsOne() {
        assertEquals(1.0f, ANNIndex.cosineMargin(
                new double[]{3.0, 4.0},
                new double[]{6.0, 8.0}), 0.000001f);
    }

    @Test
    public void cosineMarginForOrthogonalVectorsIsZero() {
        assertEquals(0.0f, ANNIndex.cosineMargin(
                new double[]{1.0, 0.0},
                new double[]{0.0, 1.0}), 0.000001f);
    }

    @Test
    public void euclideanMarginIncludesBiasAndDotProduct() {
        assertEquals(8.5f, ANNIndex.euclideanMargin(
                new double[]{1.0, 2.0},
                new double[]{3.0, 4.0},
                0.5f), 0.000001f);
    }

    @Test
    public void euclideanMarginWithZeroVectorsReturnsBias() {
        assertEquals(2.5f, ANNIndex.euclideanMargin(
                new double[]{0.0, 0.0},
                new double[]{5.0, -3.0},
                2.5f), 0.000001f);
    }
}
