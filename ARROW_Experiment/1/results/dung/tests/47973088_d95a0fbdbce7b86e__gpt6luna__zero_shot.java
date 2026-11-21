package uk.gov.gchq.gaffer.graph;

import org.junit.Test;
import uk.gov.gchq.gaffer.data.elementdefinition.view.View;
import uk.gov.gchq.gaffer.store.library.NoGraphLibrary;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class GraphConfig_ArrowTest {

    @Test
    public void constructorAndGraphIdAccessors() {
        GraphConfig config = new GraphConfig("graph-1");

        assertEquals("graph-1", config.getGraphId());
        config.setGraphId("graph-2");
        assertEquals("graph-2", config.getGraphId());
    }

    @Test
    public void descriptionAccessors() {
        GraphConfig config = new GraphConfig();

        assertNull(config.getDescription());
        config.setDescription("description");
        assertEquals("description", config.getDescription());
    }

    @Test
    public void libraryAccessors() {
        GraphConfig config = new GraphConfig();
        NoGraphLibrary library = new NoGraphLibrary();

        assertNull(config.getLibrary());
        config.setLibrary(library);
        assertSame(library, config.getLibrary());
    }

    @Test
    public void viewAccessors() {
        GraphConfig config = new GraphConfig();

        assertNull(config.getView());
        View view = new View();
        config.setView(view);
        assertNotNull(config.getView());
        assertNotSame(view, config.getView());
        config.setView(null);
        assertNull(config.getView());
    }

    @Test
    public void hooksCanBeAddedAndNullHooksAreIgnored() {
        GraphConfig config = new GraphConfig();

        assertTrue(config.getHooks().isEmpty());
        config.addHook(null);
        assertTrue(config.getHooks().isEmpty());
    }

    @Test
    public void setHooksAddsHooksAndNullClearsThem() {
        GraphConfig config = new GraphConfig();
        config.setHooks(null);
        assertTrue(config.getHooks().isEmpty());
    }

    @Test
    public void builderBuildsWithDefaultLibrary() {
        GraphConfig config = new GraphConfig.Builder().build();

        assertNotNull(config.getLibrary());
        assertEquals(NoGraphLibrary.class, config.getLibrary().getClass());
    }

    @Test
    public void builderSetsGraphProperties() {
        View view = new View();
        GraphConfig config = new GraphConfig.Builder()
                .graphId("graph")
                .description("description")
                .library(new NoGraphLibrary())
                .view(view)
                .build();

        assertEquals("graph", config.getGraphId());
        assertEquals("description", config.getDescription());
        assertNotNull(config.getLibrary());
        assertNotNull(config.getView());
    }

    @Test
    public void builderMergeCopiesNonNullProperties() {
        GraphConfig source = new GraphConfig("source");
        source.setDescription("merged");
        source.setLibrary(new NoGraphLibrary());

        GraphConfig result = new GraphConfig.Builder()
                .graphId("initial")
                .merge(source)
                .build();

        assertEquals("source", result.getGraphId());
        assertEquals("merged", result.getDescription());
        assertEquals(NoGraphLibrary.class, result.getLibrary().getClass());
    }

    @Test
    public void builderAcceptsNullJsonBytesAndNullMerge() {
        GraphConfig config = new GraphConfig.Builder()
                .json((byte[]) null)
                .merge(null)
                .build();

        assertNull(config.getGraphId());
        assertNotNull(config.getLibrary());
    }

    @Test
    public void builderAcceptsNullViewBytes() {
        GraphConfig config = new GraphConfig.Builder()
                .view((byte[]) null)
                .build();

        assertNull(config.getView());
    }

    @Test
    public void builderAcceptsNullHookArray() {
        GraphConfig config = new GraphConfig.Builder()
                .addHooks((uk.gov.gchq.gaffer.graph.hook.GraphHook[]) null)
                .build();

        assertTrue(config.getHooks().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addHookPathRejectsMissingFile() {
        new GraphConfig.Builder().addHook(java.nio.file.Paths.get("missing-graph-hook-file.json"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addHooksPathRejectsMissingFile() {
        new GraphConfig.Builder().addHooks(java.nio.file.Paths.get("missing-graph-hooks-file.json"));
    }

    @Test
    public void toStringIncludesGraphId() {
        GraphConfig config = new GraphConfig("graph");

        assertTrue(config.toString().contains("graph"));
    }
}
