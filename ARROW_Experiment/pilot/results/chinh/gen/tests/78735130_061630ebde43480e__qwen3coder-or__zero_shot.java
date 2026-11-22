package de.julielab.jeseme.embeddings;

import org.junit.Test;
import static org.junit.Assert.*;

public class Embedding_ArrowTest {

    @Test
    public void testConstructorWithVarargs() {
        double[] values = {1.0, 2.0, 3.0};
        Embedding embedding = new Embedding(values);
        assertNotNull(embedding.embedding);
        assertArrayEquals(values, embedding.embedding, 0.0);
    }

    @Test
    public void testConstructorWithString() {
        String line = "1.0 2.0 3.0";
        Embedding embedding = new Embedding(line);
        assertNotNull(embedding.embedding);
        assertEquals(3, embedding.embedding.length);
        assertEquals(1.0, embedding.embedding[0], 0.0);
        assertEquals(2.0, embedding.embedding[1], 0.0);
        assertEquals(3.0, embedding.embedding[2], 0.0);
    }

    @Test
    public void testSimilarityMatchingDimensions() {
        Embedding e1 = new Embedding(1.0, 2.0, 3.0);
        Embedding e2 = new Embedding(4.0, 5.0, 6.0);
        double similarity = e1.similarity(e2);
        assertEquals(32.0, similarity, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSimilarityDifferentDimensions() {
        Embedding e1 = new Embedding(1.0, 2.0);
        Embedding e2 = new Embedding(3.0, 4.0, 5.0);
        e1.similarity(e2);
    }

    @Test
    public void testToString() {
        Embedding embedding = new Embedding(1.0, 2.0, 3.0);
        String result = embedding.toString();
        assertTrue(result.contains("Embedding"));
        assertTrue(result.contains("["));
        assertTrue(result.contains("]"));
        assertTrue(result.contains("1.0"));
        assertTrue(result.contains("2.0"));
        assertTrue(result.contains("3.0"));
    }
}
