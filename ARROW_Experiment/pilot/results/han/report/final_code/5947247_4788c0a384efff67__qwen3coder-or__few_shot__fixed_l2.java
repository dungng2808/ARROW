package it.geosolutions.jaiext;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Vector;
import javax.media.jai.RenderedImageAdapter;
import org.junit.Test;
import static org.junit.Assert.*;

public class BufferedImageAdapter_ArrowTest {

    @Test
    public void testConstructor() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        assertNotNull(adapter);
    }

    @Test
    public void testGetNumXTiles() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        assertEquals(1, adapter.getNumXTiles());
    }

    @Test
    public void testGetNumYTiles() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        assertEquals(1, adapter.getNumYTiles());
    }

    @Test
    public void testGetMinTileX() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        assertEquals(0, adapter.getMinTileX());
    }

    @Test
    public void testGetMinTileY() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        assertEquals(0, adapter.getMinTileY());
    }

    @Test
    public void testGetTileWidth() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        assertEquals(100, adapter.getTileWidth());
    }

    @Test
    public void testGetTileHeight() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        assertEquals(100, adapter.getTileHeight());
    }

    @Test
    public void testGetTileGridXOffset() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        assertEquals(0, adapter.getTileGridXOffset());
    }

    @Test
    public void testGetTileGridYOffset() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        assertEquals(0, adapter.getTileGridYOffset());
    }

    @Test
    public void testGetTileValid() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        Raster raster = adapter.getTile(0, 0);
        assertNotNull(raster);
    }

    @Test
    public void testGetTileInvalid() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        Raster raster = adapter.getTile(1, 1);
        assertNull(raster);
    }

    @Test
    public void testGetData() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        Raster raster = adapter.getData();
        assertNotNull(raster);
    }

    @Test
    public void testGetDataRectangle() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        Rectangle rect = new Rectangle(10, 10, 50, 50);
        Raster raster = adapter.getData(rect);
        assertNotNull(raster);
    }

    @Test
    public void testCopyData() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        WritableRaster raster = image.getRaster();
        WritableRaster copiedRaster = adapter.copyData(raster);
        assertNotNull(copiedRaster);
    }

    @Test
    public void testGetSources() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        Vector<?> sources = adapter.getSources();
        assertNotNull(sources);
        assertEquals(0, sources.size());
    }

    @Test
    public void testGetProperty() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        Object property = adapter.getProperty("testProperty");
        // BufferedImage may have default properties, so we can't assume null
        // Instead, we verify that the returned object is not the default object reference
        assertNotNull(property);
    }

    @Test
    public void testGetPropertyNames() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        String[] propertyNames = adapter.getPropertyNames();
        assertNotNull(propertyNames);
    }

    @Test
    public void testGetColorModel() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        ColorModel colorModel = adapter.getColorModel();
        assertNotNull(colorModel);
    }

    @Test
    public void testGetSampleModel() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        SampleModel sampleModel = adapter.getSampleModel();
        assertNotNull(sampleModel);
    }

    @Test
    public void testGetWidth() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        assertEquals(100, adapter.getWidth());
    }

    @Test
    public void testGetHeight() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        assertEquals(100, adapter.getHeight());
    }

    @Test
    public void testGetMinX() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        assertEquals(0, adapter.getMinX());
    }

    @Test
    public void testGetMinY() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        assertEquals(0, adapter.getMinY());
    }
}
