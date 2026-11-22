package de.julielab.jeseme.embeddings;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Embedding_ArrowTest {

    @Test
    public void similarityReturnsDotProduct() {
        Embedding first = new Embedding(1.0, 2.0, 3.0);
        Embedding second = new Embedding(4.0, 5.0, 6.0);

        assertEquals(32.0, first.similarity(second), 0.0);
    }

    @Test
    public void similarityOfEmptyEmbeddingsIsZero() {
        Embedding first = new Embedding(new double[0]);
        Embedding second = new Embedding(new double[0]);

        assertEquals(0.0, first.similarity(second), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void similarityRejectsDifferentDimensions() {
        Embedding first = new Embedding(1.0, 2.0);
        Embedding second = new Embedding(1.0);

        first.similarity(second);
    }

    @Test
    public void stringConstructorParsesValuesAndToStringFormatsThem() {
        Embedding embedding = new Embedding("1.0 2.5 -3.0");

        assertEquals("Embedding [embedding=[1.0, 2.5, -3.0]]", embedding.toString());
    }

    @Test
    public void varargsConstructorToStringFormatsValues() {
        Embedding embedding = new Embedding(0.25, -0.5);

        assertEquals("Embedding [embedding=[0.25, -0.5]]", embedding.toString());
    }
}
