package internal.com.spotify.annoy;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ANNIndex_ArrowTest {

    @Test
    public void cosineMarginForParallelVectorsIsOne() {
        assertEquals(ANNIndex.cosineMargin(new double[]{1.0, 2.0},
                new double[]{2.0, 4.0}), 1.0f, 0.000001f);
    }

    @Test
    public void cosineMarginForOrthogonalVectorsIsZero() {
        assertEquals(ANNIndex.cosineMargin(new double[]{1.0, 0.0},
                new double[]{0.0, 1.0}), 0.0f, 0.000001f);
    }

    @Test
    public void euclideanMarginIncludesBiasAndDotProduct() {
        assertEquals(ANNIndex.euclideanMargin(new double[]{1.0, 2.0},
                new double[]{3.0, 4.0}, 0.5f), 11.5f, 0.000001f);
    }

    @Test
    public void euclideanMarginWithZeroVectorsReturnsBias() {
        assertEquals(ANNIndex.euclideanMargin(new double[]{0.0, 0.0},
                new double[]{5.0, -3.0}, 2.25f), 2.25f, 0.000001f);
    }
}
