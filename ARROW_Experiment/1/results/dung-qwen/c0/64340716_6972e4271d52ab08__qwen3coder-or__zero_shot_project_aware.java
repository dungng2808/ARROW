package io.katharsis.queryspec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class SortSpec_ArrowTest {

    @Test
    public void testConstructorWithValidArguments() {
        List<String> path = Arrays.asList("attribute1", "attribute2");
        SortSpec sortSpec = new SortSpec(path, Direction.ASC);
        
        assertEquals(path, sortSpec.getAttributePath());
        assertEquals(Direction.ASC, sortSpec.getDirection());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullPath() {
        new SortSpec(null, Direction.ASC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyPath() {
        new SortSpec(Arrays.asList(), Direction.ASC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullDirection() {
        new SortSpec(Arrays.asList("attribute"), null);
    }

    @Test
    public void testAscFactoryMethod() {
        List<String> path = Arrays.asList("attribute1", "attribute2");
        SortSpec sortSpec = SortSpec.asc(path);
        
        assertEquals(path, sortSpec.getAttributePath());
        assertEquals(Direction.ASC, sortSpec.getDirection());
    }

    @Test
    public void testDescFactoryMethod() {
        List<String> path = Arrays.asList("attribute1", "attribute2");
        SortSpec sortSpec = SortSpec.desc(path);
        
        assertEquals(path, sortSpec.getAttributePath());
        assertEquals(Direction.DESC, sortSpec.getDirection());
    }

    @Test
    public void testReverse() {
        List<String> path = Arrays.asList("attribute1", "attribute2");
        SortSpec sortSpec = new SortSpec(path, Direction.ASC);
        SortSpec reversed = sortSpec.reverse();
        
        assertEquals(path, reversed.getAttributePath());
        assertEquals(Direction.DESC, reversed.getDirection());
        
        SortSpec reversedAgain = reversed.reverse();
        assertEquals(path, reversedAgain.getAttributePath());
        assertEquals(Direction.ASC, reversedAgain.getDirection());
    }

    @Test
    public void testHashCode() {
        List<String> path = Arrays.asList("attribute1", "attribute2");
        SortSpec sortSpec1 = new SortSpec(path, Direction.ASC);
        SortSpec sortSpec2 = new SortSpec(path, Direction.ASC);
        SortSpec sortSpec3 = new SortSpec(path, Direction.DESC);
        
        assertEquals(sortSpec1.hashCode(), sortSpec2.hashCode());
        assertNotEquals(sortSpec1.hashCode(), sortSpec3.hashCode());
    }

    @Test
    public void testEquals() {
        List<String> path = Arrays.asList("attribute1", "attribute2");
        SortSpec sortSpec1 = new SortSpec(path, Direction.ASC);
        SortSpec sortSpec2 = new SortSpec(path, Direction.ASC);
        SortSpec sortSpec3 = new SortSpec(path, Direction.DESC);
        SortSpec sortSpec4 = new SortSpec(Arrays.asList("attribute1"), Direction.ASC);
        
        assertEquals(sortSpec1, sortSpec2);
        assertNotEquals(sortSpec1, sortSpec3);
        assertNotEquals(sortSpec1, sortSpec4);
        assertNotEquals(sortSpec1, new Object());
        assertEquals(sortSpec1, sortSpec1);
    }

    @Test
    public void testToString() {
        List<String> path = Arrays.asList("attribute1", "attribute2");
        SortSpec sortSpec = new SortSpec(path, Direction.ASC);
        String expected = "attribute1.attribute2 ASC";
        assertEquals(expected, sortSpec.toString());
        
        SortSpec sortSpecDesc = new SortSpec(path, Direction.DESC);
        String expectedDesc = "attribute1.attribute2 DESC";
        assertEquals(expectedDesc, sortSpecDesc.toString());
    }
}
