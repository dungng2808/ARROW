package internal.com.spotify.annoy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ANNIndex_ArrowTest {

    @Test
    public void cosineMarginForParallelVectors() {
        double[] u = {3.0, 4.0};
        double[] v = {6.0, 8.0};

        assertEquals(1.0f, ANNIndex.cosineMargin(u, v), 0.000001f);
    }

    @Test
    public void cosineMarginForOrthogonalVectors() {
        double[] u = {1.0, 0.0};
        double[] v = {0.0, 1.0};

        assertEquals(0.0f, ANNIndex.cosineMargin(u, v), 0.000001f);
    }

    @Test
    public void euclideanMarginIncludesBiasAndDotProduct() {
        double[] u = {1.0, 2.0};
        double[] v = {3.0, 4.0};

        assertEquals(13.5f, ANNIndex.euclideanMargin(u, v, 0.5f), 0.000001f);
    }

    @Test
    public void euclideanMarginWithZeroVectorsReturnsBias() {
        double[] u = {0.0, 0.0};
        double[] v = {0.0, 0.0};

        assertEquals(2.25f, ANNIndex.euclideanMargin(u, v, 2.25f), 0.000001f);
    }
}
