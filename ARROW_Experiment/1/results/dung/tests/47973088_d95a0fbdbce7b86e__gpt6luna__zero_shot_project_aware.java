package uk.gov.gchq.gaffer.graph;

import org.junit.jupiter.api.Test;
import uk.gov.gchq.gaffer.data.elementdefinition.view.View;
import uk.gov.gchq.gaffer.graph.hook.GraphHook;
import uk.gov.gchq.gaffer.store.library.NoGraphLibrary;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

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
    public void viewAccessorsPreserveView() {
        View view = new View.Builder().build();
        GraphConfig config = new GraphConfig();

        config.setView(view);

        assertNotNull(config.getView());
        assertNotSame(view, config.getView());
    }

    @Test
    public void settingNullViewClearsView() {
        GraphConfig config = new GraphConfig();
        config.setView(new View.Builder().build());

        config.setView(null);

        assertNull(config.getView());
    }

    @Test
    public void libraryAccessors() {
        GraphConfig config = new GraphConfig();
        NoGraphLibrary library = new NoGraphLibrary();

        config.setLibrary(library);

        assertSame(library, config.getLibrary());
    }

    @Test
    public void hooksCanBeAddedAndNullHooksAreIgnored() {
        GraphConfig config = new GraphConfig();
        GraphHook hook = new GraphHook() { };

        config.addHook(hook);
        config.addHook(null);

        assertEquals(Collections.singletonList(hook), config.getHooks());
    }

    @Test
    public void setHooksAddsHooksAndNullClearsThem() {
        GraphConfig config = new GraphConfig();
        GraphHook first = new GraphHook() { };
        GraphHook second = new GraphHook() { };

        config.setHooks(Arrays.asList(first, second));
        assertEquals(Arrays.asList(first, second), config.getHooks());

        config.setHooks(null);
        assertTrue(config.getHooks().isEmpty());
    }

    @Test
    public void builderBuildsConfigWithDefaultsAndConfiguredValues() {
        View view = new View.Builder().build();
        GraphHook hook = new GraphHook() { };

        GraphConfig config = new GraphConfig.Builder()
                .graphId("graph")
                .description("description")
                .view(view)
                .addHook(hook)
                .build();

        assertEquals("graph", config.getGraphId());
        assertEquals("description", config.getDescription());
        assertNotNull(config.getView());
        assertTrue(config.getLibrary() instanceof NoGraphLibrary);
        assertEquals(Collections.singletonList(hook), config.getHooks());
    }

    @Test
    public void builderMergeKeepsExistingValuesWhenMergedValuesAreNull() {
        GraphConfig initial = new GraphConfig("initial");
        initial.setDescription("original");
        GraphConfig merged = new GraphConfig();

        GraphConfig result = new GraphConfig.Builder()
                .merge(initial)
                .merge(merged)
                .build();

        assertEquals("initial", result.getGraphId());
        assertEquals("original", result.getDescription());
    }

    @Test
    public void builderJsonWithNullBytesLeavesConfigUnchanged() {
        GraphConfig config = new GraphConfig.Builder()
                .json((byte[]) null)
                .build();

        assertNull(config.getGraphId());
        assertTrue(config.getLibrary() instanceof NoGraphLibrary);
    }

    @Test
    public void builderJsonWithValidBytesDeserialisesConfig() {
        byte[] json = "{\"graphId\":\"from-json\",\"description\":\"loaded\"}"
                .getBytes(StandardCharsets.UTF_8);

        GraphConfig config = new GraphConfig.Builder().json(json).build();

        assertEquals("from-json", config.getGraphId());
        assertEquals("loaded", config.getDescription());
    }

    @Test
    public void builderJsonWithNullPathLeavesConfigUnchanged() {
        GraphConfig config = new GraphConfig.Builder()
                .json((java.nio.file.Path) null)
                .build();

        assertNull(config.getGraphId());
        assertTrue(config.getLibrary() instanceof NoGraphLibrary);
    }

    @Test
    public void builderViewWithNullBytesClearsView() {
        GraphConfig config = new GraphConfig.Builder()
                .view((byte[]) null)
                .build();

        assertNull(config.getView());
    }

    @Test
    public void builderAddHookWithNullHookDoesNothing() {
        GraphConfig config = new GraphConfig.Builder()
                .addHook((GraphHook) null)
                .build();

        assertTrue(config.getHooks().isEmpty());
    }

    @Test
    public void builderAddHooksWithNullArrayDoesNothing() {
        GraphConfig config = new GraphConfig.Builder()
                .addHooks((GraphHook[]) null)
                .build();

        assertTrue(config.getHooks().isEmpty());
    }

    @Test
    public void builderAddHookRejectsMissingPath() {
        assertThrows(IllegalArgumentException.class,
                () -> new GraphConfig.Builder().addHook(Paths.get("missing-graph-hook-file")));
    }

    @Test
    public void builderAddHooksRejectsMissingPath() {
        assertThrows(IllegalArgumentException.class,
                () -> new GraphConfig.Builder().addHooks(Paths.get("missing-graph-hooks-file")));
    }

    @Test
    public void addHookRejectsMissingHookPath() {
        GraphConfig config = new GraphConfig();

        assertThrows(IllegalArgumentException.class,
                () -> config.addHook(new uk.gov.gchq.gaffer.graph.hook.GraphHookPath() {{
                    setPath("missing-graph-hook-file");
                }}));
    }

    @Test
    public void toStringIncludesGraphId() {
        GraphConfig config = new GraphConfig("graph");

        assertTrue(config.toString().contains("graph"));
    }

    @Test
    public void builderToStringIncludesGraphId() {
        GraphConfig.Builder builder = new GraphConfig.Builder().graphId("graph");

        assertTrue(builder.toString().contains("graph"));
    }
}
