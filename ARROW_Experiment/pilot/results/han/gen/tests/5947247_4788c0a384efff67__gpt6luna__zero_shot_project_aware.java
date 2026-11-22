package it.geosolutions.jaiext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Vector;

import org.junit.Test;

public class BufferedImageAdapter_ArrowTest {

    @Test
    public void reportsSingleTileAndImageDimensions() {
        BufferedImage image = new BufferedImage(4, 3, BufferedImage.TYPE_INT_ARGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);

        assertEquals(1, adapter.getNumXTiles());
        assertEquals(1, adapter.getNumYTiles());
        assertEquals(0, adapter.getMinTileX());
        assertEquals(0, adapter.getMinTileY());
        assertEquals(4, adapter.getTileWidth());
        assertEquals(3, adapter.getTileHeight());
        assertEquals(0, adapter.getTileGridXOffset());
        assertEquals(0, adapter.getTileGridYOffset());
        assertEquals(4, adapter.getWidth());
        assertEquals(3, adapter.getHeight());
        assertEquals(image.getMinX(), adapter.getMinX());
        assertEquals(image.getMinY(), adapter.getMinY());
    }

    @Test
    public void returnsOnlyTheZeroIndexedTile() {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);

        assertSame(image.getTile(0, 0), adapter.getTile(0, 0));
        assertNull(adapter.getTile(1, 0));
        assertNull(adapter.getTile(0, 1));
        assertNull(adapter.getTile(-1, 0));
    }

    @Test
    public void returnsRasterDataAndCopiesRequestedRectangle() {
        BufferedImage image = new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(1, 1, 0xFF123456);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);

        Raster data = adapter.getData();
        assertSame(image.getRaster(), data);
        assertEquals(0xFF123456, image.getRGB(1, 1));

        Rectangle rectangle = new Rectangle(1, 0, 2, 2);
        Raster requested = adapter.getData(rectangle);
        assertEquals(rectangle, requested.getBounds());
        assertEquals(image.getRaster().getSample(1, 1, 0), requested.getSample(1, 1, 0));
    }

    @Test
    public void copiesDataIntoProvidedRaster() {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0xFFABCDEF);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        WritableRaster destination = image.getRaster().createCompatibleWritableRaster();

        WritableRaster result = adapter.copyData(destination);

        assertSame(destination, result);
        assertEquals(image.getRaster().getSample(0, 0, 0), result.getSample(0, 0, 0));
    }

    @Test
    public void exposesImageMetadataAndNoSources() {
        BufferedImage image = new BufferedImage(2, 3, BufferedImage.TYPE_INT_ARGB);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);

        assertTrue(adapter.getSources().isEmpty());
        assertSame(image.getColorModel(), adapter.getColorModel());
        assertSame(image.getSampleModel(), adapter.getSampleModel());
        assertNull(adapter.getProperty("missing"));
        assertFalse(adapter.getPropertyNames() != null && adapter.getPropertyNames().length > 0);

        Vector<java.awt.image.RenderedImage> sources = adapter.getSources();
        sources.add(image);
        assertEquals(1, sources.size());
        assertTrue(adapter.getSources().isEmpty());
    }
}
