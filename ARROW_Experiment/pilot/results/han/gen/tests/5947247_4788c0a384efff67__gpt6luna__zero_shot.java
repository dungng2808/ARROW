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
        BufferedImage image = new BufferedImage(4, 3, BufferedImage.TYPE_BYTE_GRAY);
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
        assertEquals(0, adapter.getMinX());
        assertEquals(0, adapter.getMinY());
    }

    @Test
    public void returnsOnlyTheZeroIndexedTile() {
        BufferedImage image = new BufferedImage(3, 2, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);

        assertSame(image.getTile(0, 0), adapter.getTile(0, 0));
        assertNull(adapter.getTile(1, 0));
        assertNull(adapter.getTile(0, 1));
        assertNull(adapter.getTile(-1, 0));
    }

    @Test
    public void returnsImageRasterWhenItHasNoParent() {
        BufferedImage image = new BufferedImage(3, 2, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);

        assertSame(image.getRaster(), adapter.getData());
    }

    @Test
    public void returnsCopiedDataForSubimageRaster() {
        BufferedImage image = new BufferedImage(5, 4, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage subimage = image.getSubimage(1, 1, 3, 2);
        BufferedImageAdapter adapter = new BufferedImageAdapter(subimage);

        Raster data = adapter.getData();

        assertNotNull(data);
        assertEquals(3, data.getWidth());
        assertEquals(2, data.getHeight());
        assertFalse(data == subimage.getRaster());
    }

    @Test
    public void getsDataForRequestedRectangle() {
        BufferedImage image = new BufferedImage(4, 3, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        Rectangle rectangle = new Rectangle(1, 1, 2, 1);

        Raster data = adapter.getData(rectangle);

        assertEquals(rectangle, data.getBounds());
        assertEquals(2, data.getWidth());
        assertEquals(1, data.getHeight());
    }

    @Test
    public void copiesDataIntoProvidedRaster() {
        BufferedImage image = new BufferedImage(3, 2, BufferedImage.TYPE_BYTE_GRAY);
        image.getRaster().setSample(1, 1, 0, 127);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);
        WritableRaster destination = image.getRaster().createCompatibleWritableRaster();

        WritableRaster result = adapter.copyData(destination);

        assertSame(destination, result);
        assertEquals(127, result.getSample(1, 1, 0));
    }

    @Test
    public void hasNoSources() {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);

        Vector<java.awt.image.RenderedImage> sources = adapter.getSources();

        assertNotNull(sources);
        assertTrue(sources.isEmpty());
    }

    @Test
    public void exposesImageModelsAndProperties() {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImageAdapter adapter = new BufferedImageAdapter(image);

        assertSame(image.getColorModel(), adapter.getColorModel());
        assertSame(image.getSampleModel(), adapter.getSampleModel());
        assertSame(image.getPropertyNames(), adapter.getPropertyNames());
        assertEquals(image.getProperty("missing"), adapter.getProperty("missing"));
    }
}
