package uk.gov.gchq.gaffer.graph;

import org.junit.Test;
import uk.gov.gchq.gaffer.graph.hook.GraphHook;
import uk.gov.gchq.gaffer.store.library.NoGraphLibrary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class GraphConfig_ArrowTest {

    @Test
    public void constructorAndProperties() {
        GraphConfig config = new GraphConfig("graph-one");

        assertEquals("graph-one", config.getGraphId());
        assertNull(config.getDescription());
        assertNull(config.getView());
        assertNull(config.getLibrary());
        assertTrue(config.getHooks().isEmpty());

        config.setGraphId("graph-two");
        config.setDescription("description");

        assertEquals("graph-two", config.getGraphId());
        assertEquals("description", config.getDescription());
    }

    @Test
    public void setAndGetLibrary() {
        GraphConfig config = new GraphConfig();
        NoGraphLibrary library = new NoGraphLibrary();

        config.setLibrary(library);

        assertSame(library, config.getLibrary());
    }

    @Test
    public void setHooksAddsHooksAndNullClearsThem() {
        GraphConfig config = new GraphConfig();
        GraphHook first = null;
        GraphHook second = null;
        List<GraphHook> hooks = new ArrayList<>();
        hooks.add(first);
        hooks.add(second);

        config.setHooks(hooks);

        assertTrue(config.getHooks().isEmpty());

        config.setHooks(null);
        assertTrue(config.getHooks().isEmpty());
    }

    @Test
    public void addNullHookDoesNothing() {
        GraphConfig config = new GraphConfig();

        config.addHook(null);

        assertTrue(config.getHooks().isEmpty());
    }

    @Test
    public void builderSetsPropertiesAndDefaultLibrary() {
        GraphConfig config = new GraphConfig.Builder()
                .graphId("built-graph")
                .description("built description")
                .build();

        assertEquals("built-graph", config.getGraphId());
        assertEquals("built description", config.getDescription());
        assertNotNull(config.getLibrary());
        assertTrue(config.getLibrary() instanceof NoGraphLibrary);
    }

    @Test
    public void builderMergeCopiesNonNullPropertiesAndHooks() {
        GraphConfig source = new GraphConfig("source-graph");
        source.setDescription("source description");
        NoGraphLibrary library = new NoGraphLibrary();
        source.setLibrary(library);

        GraphConfig merged = new GraphConfig.Builder()
                .graphId("initial-graph")
                .merge(source)
                .build();

        assertEquals("source-graph", merged.getGraphId());
        assertEquals("source description", merged.getDescription());
        assertSame(library, merged.getLibrary());
        assertTrue(merged.getHooks().isEmpty());
    }

    @Test
    public void builderAddNullHooksDoesNothing() {
        GraphConfig config = new GraphConfig.Builder()
                .addHooks((GraphHook[]) null)
                .addHook((GraphHook) null)
                .build();

        assertTrue(config.getHooks().isEmpty());
    }

    @Test
    public void builderJsonNullLeavesConfigurationUnchanged() {
        GraphConfig config = new GraphConfig.Builder()
                .json((byte[]) null)
                .build();

        assertNull(config.getGraphId());
        assertNotNull(config.getLibrary());
        assertTrue(config.getHooks().isEmpty());
    }

    @Test
    public void builderJsonNullStreamLeavesConfigurationUnchanged() {
        GraphConfig config = new GraphConfig.Builder()
                .json((java.io.InputStream) null)
                .build();

        assertNull(config.getGraphId());
        assertNotNull(config.getLibrary());
    }

    @Test
    public void builderJsonNullPathLeavesConfigurationUnchanged() {
        GraphConfig config = new GraphConfig.Builder()
                .json((java.nio.file.Path) null)
                .build();

        assertNull(config.getGraphId());
        assertNotNull(config.getLibrary());
    }

    @Test
    public void builderJsonNullUriLeavesConfigurationUnchanged() {
        GraphConfig config = new GraphConfig.Builder()
                .json((java.net.URI) null)
                .build();

        assertNull(config.getGraphId());
        assertNotNull(config.getLibrary());
    }

    @Test
    public void builderViewNullLeavesViewUnset() {
        GraphConfig config = new GraphConfig.Builder()
                .view((byte[]) null)
                .build();

        assertNull(config.getView());
    }

    @Test
    public void builderViewNullPathLeavesViewUnset() {
        GraphConfig config = new GraphConfig.Builder()
                .view((java.nio.file.Path) null)
                .build();

        assertNull(config.getView());
    }

    @Test
    public void builderViewNullStreamLeavesViewUnset() {
        GraphConfig config = new GraphConfig.Builder()
                .view((java.io.InputStream) null)
                .build();

        assertNull(config.getView());
    }

    @Test
    public void builderViewNullUriLeavesViewUnset() {
        GraphConfig config = new GraphConfig.Builder()
                .view((java.net.URI) null)
                .build();

        assertNull(config.getView());
    }

    @Test
    public void builderAddHooksWithEmptyArrayDoesNothing() {
        GraphConfig config = new GraphConfig.Builder()
                .addHooks(new GraphHook[0])
                .build();

        assertTrue(config.getHooks().isEmpty());
    }

    @Test
    public void builderAddHookMissingPathThrows() {
        try {
            new GraphConfig.Builder().addHook(java.nio.file.Paths.get("missing-graph-hook-file.json"));
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Unable to find graph hook file"));
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    public void builderAddHooksMissingPathThrows() {
        try {
            new GraphConfig.Builder().addHooks(java.nio.file.Paths.get("missing-graph-hooks-file.json"));
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Unable to find graph hooks file"));
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    public void addHookPathMissingFileThrows() {
        GraphConfig config = new GraphConfig();

        try {
            config.addHook(new uk.gov.gchq.gaffer.graph.hook.GraphHookPath() {{
                setPath("missing-graph-hook-file.json");
            }});
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Unable to find graph hook file"));
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    public void builderToStringContainsGraphId() {
        GraphConfig.Builder builder = new GraphConfig.Builder().graphId("visible-graph");

        assertTrue(builder.toString().contains("visible-graph"));
    }

    @Test
    public void configToStringContainsGraphId() {
        GraphConfig config = new GraphConfig("visible-graph");

        assertTrue(config.toString().contains("visible-graph"));
    }
}
