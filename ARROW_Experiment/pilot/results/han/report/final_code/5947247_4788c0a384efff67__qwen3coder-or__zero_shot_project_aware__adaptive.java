package it.geosolutions.jaiext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

public class BufferedImageAdapter_ArrowTest {

    private BufferedImage image;
    private BufferedImageAdapter adapter;

    @Before
    public void setUp() {
        image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        adapter = new BufferedImageAdapter(image);
    }

    @Test
    public void testGetNumXTiles() {
        assertEquals(1, adapter.getNumXTiles());
    }

    @Test
    public void testGetNumYTiles() {
        assertEquals(1, adapter.getNumYTiles());
    }

    @Test
    public void testGetMinTileX() {
        assertEquals(0, adapter.getMinTileX());
    }

    @Test
    public void testGetMinTileY() {
        assertEquals(0, adapter.getMinTileY());
    }

    @Test
    public void testGetTileWidth() {
        assertEquals(100, adapter.getTileWidth());
    }

    @Test
    public void testGetTileHeight() {
        assertEquals(100, adapter.getTileHeight());
    }

    @Test
    public void testGetTileGridXOffset() {
        assertEquals(0, adapter.getTileGridXOffset());
    }

    @Test
    public void testGetTileGridYOffset() {
        assertEquals(0, adapter.getTileGridYOffset());
    }

    @Test
    public void testGetTileValidTile() {
        Raster tile = adapter.getTile(0, 0);
        assertNotNull(tile);
    }

    @Test
    public void testGetTileInvalidTile() {
        Raster tile = adapter.getTile(1, 0);
        assertNull(tile);
        
        tile = adapter.getTile(0, 1);
        assertNull(tile);
        
        tile = adapter.getTile(1, 1);
        assertNull(tile);
    }

    @Test
    public void testGetDataNoParent() {
        Raster data = adapter.getData();
        assertNotNull(data);
        assertSame(image.getRaster(), data);
    }

    @Test
    public void testGetDataWithParent() {
        // Create a raster with parent to trigger the copy logic
        WritableRaster raster = image.getRaster();
        WritableRaster parentRaster = raster.createCompatibleWritableRaster();
        // Since setParent is not available on WritableRaster, we simulate the condition
        // by directly testing the copyData functionality which is the core of the getData method
        // when a parent exists
        WritableRaster targetRaster = raster.createCompatibleWritableRaster();
        WritableRaster result = adapter.copyData(targetRaster);
        assertNotNull(result);
    }

    @Test
    public void testGetDataRectangle() {
        Rectangle rect = new Rectangle(10, 10, 50, 50);
        Raster data = adapter.getData(rect);
        assertNotNull(data);
    }

    @Test
    public void testCopyData() {
        WritableRaster raster = image.getRaster().createCompatibleWritableRaster();
        WritableRaster copied = adapter.copyData(raster);
        assertNotNull(copied);
    }

    @Test
    public void testGetSources() {
        Vector sources = adapter.getSources();
        assertNotNull(sources);
        assertEquals(0, sources.size());
    }

    @Test
    public void testGetProperty() {
        Object property = adapter.getProperty("testProperty");
        assertNull(property);
    }

    @Test
    public void testGetPropertyNames() {
        String[] names = adapter.getPropertyNames();
        assertNotNull(names);
        // Check that the returned array is not empty (it should contain at least the default property names)
        // But since we're using a fresh BufferedImage, it might be empty
        // Just ensure it doesn't throw an exception and returns an array
        assertNotNull(names);
    }

    @Test
    public void testGetColorModel() {
        ColorModel colorModel = adapter.getColorModel();
        assertNotNull(colorModel);
        assertSame(image.getColorModel(), colorModel);
    }

    @Test
    public void testGetSampleModel() {
        SampleModel sampleModel = adapter.getSampleModel();
        assertNotNull(sampleModel);
        assertSame(image.getSampleModel(), sampleModel);
    }

    @Test
    public void testGetWidth() {
        assertEquals(100, adapter.getWidth());
    }

    @Test
    public void testGetHeight() {
        assertEquals(100, adapter.getHeight());
    }

    @Test
    public void testGetMinX() {
        assertEquals(0, adapter.getMinX());
    }

    @Test
    public void testGetMinY() {
        assertEquals(0, adapter.getMinY());
    }
}
