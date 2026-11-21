package uk.gov.gchq.gaffer.graph;

import org.junit.Test;

import uk.gov.gchq.gaffer.data.elementdefinition.view.View;
import uk.gov.gchq.gaffer.graph.hook.GraphHook;
import uk.gov.gchq.gaffer.store.library.GraphLibrary;
import uk.gov.gchq.gaffer.store.library.NoGraphLibrary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GraphConfig_ArrowTest {

    @Test
    public void shouldCreateGraphConfigWithGraphId() {
        // Given
        String graphId = "testGraph";

        // When
        GraphConfig config = new GraphConfig(graphId);

        // Then
        assertEquals(graphId, config.getGraphId());
    }

    @Test
    public void shouldSetAndGetGraphId() {
        // Given
        GraphConfig config = new GraphConfig();
        String graphId = "testGraph";

        // When
        config.setGraphId(graphId);

        // Then
        assertEquals(graphId, config.getGraphId());
    }

    @Test
    public void shouldSetAndGetView() {
        // Given
        GraphConfig config = new GraphConfig();
        View view = mock(View.class);

        // When
        config.setView(view);

        // Then
        assertSame(view, config.getView());
    }

    @Test
    public void shouldSetAndGetNullView() {
        // Given
        GraphConfig config = new GraphConfig();

        // When
        config.setView(null);

        // Then
        assertNull(config.getView());
    }

    @Test
    public void shouldSetAndGetLibrary() {
        // Given
        GraphConfig config = new GraphConfig();
        GraphLibrary library = mock(GraphLibrary.class);

        // When
        config.setLibrary(library);

        // Then
        assertSame(library, config.getLibrary());
    }

    @Test
    public void shouldSetAndGetDescription() {
        // Given
        GraphConfig config = new GraphConfig();
        String description = "test description";

        // When
        config.setDescription(description);

        // Then
        assertEquals(description, config.getDescription());
    }

    @Test
    public void shouldGetEmptyHooksListByDefault() {
        // Given
        GraphConfig config = new GraphConfig();

        // When
        List<GraphHook> hooks = config.getHooks();

        // Then
        assertEquals(0, hooks.size());
    }

    @Test
    public void shouldSetHooks() {
        // Given
        GraphConfig config = new GraphConfig();
        GraphHook hook1 = mock(GraphHook.class);
        GraphHook hook2 = mock(GraphHook.class);
        List<GraphHook> hooks = java.util.Arrays.asList(hook1, hook2);

        // When
        config.setHooks(hooks);

        // Then
        assertEquals(2, config.getHooks().size());
        assertSame(hook1, config.getHooks().get(0));
        assertSame(hook2, config.getHooks().get(1));
    }

    @Test
    public void shouldClearHooksWhenSettingNull() {
        // Given
        GraphConfig config = new GraphConfig();
        GraphHook hook1 = mock(GraphHook.class);
        GraphHook hook2 = mock(GraphHook.class);
        config.getHooks().add(hook1);
        config.getHooks().add(hook2);

        // When
        config.setHooks(null);

        // Then
        assertEquals(0, config.getHooks().size());
    }

    @Test
    public void shouldAddHook() {
        // Given
        GraphConfig config = new GraphConfig();
        GraphHook hook = mock(GraphHook.class);

        // When
        config.addHook(hook);

        // Then
        assertEquals(1, config.getHooks().size());
        assertSame(hook, config.getHooks().get(0));
    }

    @Test
    public void shouldNotAddNullHook() {
        // Given
        GraphConfig config = new GraphConfig();

        // When
        config.addHook(null);

        // Then
        assertEquals(0, config.getHooks().size());
    }

    @Test
    public void shouldBuildGraphConfigWithBuilder() {
        // Given
        String graphId = "testGraph";
        GraphLibrary library = mock(GraphLibrary.class);
        String description = "test description";

        // When
        GraphConfig config = new GraphConfig.Builder()
                .graphId(graphId)
                .library(library)
                .description(description)
                .build();

        // Then
        assertEquals(graphId, config.getGraphId());
        assertSame(library, config.getLibrary());
        assertEquals(description, config.getDescription());
    }

    @Test
    public void shouldBuildGraphConfigWithNoLibraryAndDefaultToNoGraphLibrary() {
        // When
        GraphConfig config = new GraphConfig.Builder().build();

        // Then
        assertEquals(NoGraphLibrary.class, config.getLibrary().getClass());
    }

    @Test
    public void shouldMergeConfigs() {
        // Given
        GraphConfig sourceConfig = new GraphConfig();
        sourceConfig.setGraphId("sourceGraph");
        sourceConfig.setDescription("source description");

        GraphConfig targetConfig = new GraphConfig();
        targetConfig.setGraphId("targetGraph");

        // When
        GraphConfig.Builder builder = new GraphConfig.Builder();
        builder.merge(sourceConfig);
        GraphConfig mergedConfig = builder.build();

        // Then
        assertEquals("sourceGraph", mergedConfig.getGraphId());
        assertEquals("source description", mergedConfig.getDescription());
    }

    @Test
    public void shouldHandleNullInMerge() {
        // Given
        GraphConfig sourceConfig = new GraphConfig();
        sourceConfig.setGraphId("sourceGraph");

        // When
        GraphConfig.Builder builder = new GraphConfig.Builder();
        builder.merge(sourceConfig);
        GraphConfig mergedConfig = builder.build();

        // Then
        assertEquals("sourceGraph", mergedConfig.getGraphId());
    }

    @Test
    public void shouldSetViewFromPath() throws IOException {
        // Given
        Path tempFile = Files.createTempFile("view", ".json");
        String viewJson = "{\"edges\":{}}";
        Files.write(tempFile, viewJson.getBytes());

        // When
        GraphConfig config = new GraphConfig.Builder().view(tempFile).build();

        // Then
        assertNotNull(config.getView());
    }

    @Test
    public void shouldSetViewFromInputStream() throws IOException {
        // Given
        String viewJson = "{\"edges\":{}}";
        InputStream inputStream = new ByteArrayInputStream(viewJson.getBytes());

        // When
        GraphConfig config = new GraphConfig.Builder().view(inputStream).build();

        // Then
        assertNotNull(config.getView());
    }

    @Test
    public void shouldSetViewFromURI() throws IOException {
        // Given
        URI uri = URI.create("http://example.com/view.json");
        InputStream inputStream = new ByteArrayInputStream("{\"edges\":{}}".getBytes());
        when(StreamUtil.openStream(uri)).thenReturn(inputStream);

        // When
        GraphConfig config = new GraphConfig.Builder().view(uri).build();

        // Then
        assertNotNull(config.getView());
    }

    @Test
    public void shouldSetViewFromBytes() {
        // Given
        byte[] viewBytes = "{\"edges\":{}}".getBytes();

        // When
        GraphConfig config = new GraphConfig.Builder().view(viewBytes).build();

        // Then
        assertNotNull(config.getView());
    }
}
