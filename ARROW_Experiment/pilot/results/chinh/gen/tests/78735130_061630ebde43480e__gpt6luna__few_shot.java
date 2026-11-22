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
        Embedding first = new Embedding();
        Embedding second = new Embedding();

        assertEquals(0.0, first.similarity(second), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void similarityRejectsDifferentDimensions() {
        Embedding first = new Embedding(1.0, 2.0);
        Embedding second = new Embedding(1.0);

        first.similarity(second);
    }

    @Test
    public void stringConstructorParsesValues() {
        Embedding embedding = new Embedding("1.5 -2.0 3.25");

        assertEquals("Embedding [embedding=[1.5, -2.0, 3.25]]", embedding.toString());
    }

    @Test
    public void toStringFormatsValues() {
        Embedding embedding = new Embedding(0.5, 1.0);

        assertEquals("Embedding [embedding=[0.5, 1.0]]", embedding.toString());
    }
}
