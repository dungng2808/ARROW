package uk.gov.gchq.gaffer.graph;

import org.junit.Test;

import uk.gov.gchq.gaffer.data.elementdefinition.view.View;
import uk.gov.gchq.gaffer.graph.hook.GraphHook;
import uk.gov.gchq.gaffer.store.library.GraphLibrary;
import uk.gov.gchq.gaffer.store.library.NoGraphLibrary;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GraphConfig_ArrowTest {

    @Test
    public void shouldCreateDefaultGraphConfig() {
        // When
        GraphConfig config = new GraphConfig();

        // Then
        assertNull(config.getGraphId());
        assertNull(config.getView());
        assertNull(config.getLibrary());
        assertNull(config.getDescription());
        assertNotNull(config.getHooks());
        assertTrue(config.getHooks().isEmpty());
    }

    @Test
    public void shouldCreateGraphConfigWithGraphId() {
        // Given
        String graphId = "test-graph";

        // When
        GraphConfig config = new GraphConfig(graphId);

        // Then
        assertEquals(graphId, config.getGraphId());
        assertNull(config.getView());
        assertNull(config.getLibrary());
        assertNull(config.getDescription());
        assertNotNull(config.getHooks());
        assertTrue(config.getHooks().isEmpty());
    }

    @Test
    public void shouldSetAndGetGraphId() {
        // Given
        GraphConfig config = new GraphConfig();
        String graphId = "test-graph";

        // When
        config.setGraphId(graphId);

        // Then
        assertEquals(graphId, config.getGraphId());
    }

    @Test
    public void shouldSetAndGetView() {
        // Given
        GraphConfig config = new GraphConfig();
        View view = new View.Builder().build();

        // When
        config.setView(view);

        // Then
        assertEquals(view, config.getView());
    }

    @Test
    public void shouldSetAndGetNullView() {
        // Given
        GraphConfig config = new GraphConfig();
        View view = new View.Builder().build();

        // When
        config.setView(view);
        config.setView(null);

        // Then
        assertNull(config.getView());
    }

    @Test
    public void shouldSetAndGetLibrary() {
        // Given
        GraphConfig config = new GraphConfig();
        GraphLibrary library = new NoGraphLibrary();

        // When
        config.setLibrary(library);

        // Then
        assertEquals(library, config.getLibrary());
    }

    @Test
    public void shouldSetAndGetDescription() {
        // Given
        GraphConfig config = new GraphConfig();
        String description = "Test description";

        // When
        config.setDescription(description);

        // Then
        assertEquals(description, config.getDescription());
    }

    @Test
    public void shouldGetHooks() {
        // Given
        GraphConfig config = new GraphConfig();

        // When & Then
        assertNotNull(config.getHooks());
        assertTrue(config.getHooks().isEmpty());
    }

    @Test
    public void shouldSetHooks() {
        // Given
        GraphConfig config = new GraphConfig();
        List<GraphHook> hooks = new ArrayList<>();

        // When
        config.setHooks(hooks);

        // Then
        assertEquals(hooks, config.getHooks());
    }

    @Test
    public void shouldSetNullHooks() {
        // Given
        GraphConfig config = new GraphConfig();
        config.getHooks().add(new MockGraphHook());

        // When
        config.setHooks(null);

        // Then
        assertTrue(config.getHooks().isEmpty());
    }

    @Test
    public void shouldAddHook() {
        // Given
        GraphConfig config = new GraphConfig();
        GraphHook hook = new MockGraphHook();

        // When
        config.addHook(hook);

        // Then
        assertEquals(1, config.getHooks().size());
        assertEquals(hook, config.getHooks().get(0));
    }

    @Test
    public void shouldAddNullHook() {
        // Given
        GraphConfig config = new GraphConfig();

        // When
        config.addHook(null);

        // Then
        assertTrue(config.getHooks().isEmpty());
    }

    @Test
    public void shouldBuildGraphConfig() {
        // When
        GraphConfig config = new GraphConfig.Builder().build();

        // Then
        assertNotNull(config);
        assertNotNull(config.getLibrary());
        assertTrue(config.getLibrary() instanceof NoGraphLibrary);
    }

    @Test
    public void shouldBuildGraphConfigWithGraphId() {
        // Given
        String graphId = "test-graph";

        // When
        GraphConfig config = new GraphConfig.Builder().graphId(graphId).build();

        // Then
        assertEquals(graphId, config.getGraphId());
    }

    @Test
    public void shouldBuildGraphConfigWithLibrary() {
        // Given
        GraphLibrary library = new NoGraphLibrary();

        // When
        GraphConfig config = new GraphConfig.Builder().library(library).build();

        // Then
        assertEquals(library, config.getLibrary());
    }

    @Test
    public void shouldBuildGraphConfigWithDescription() {
        // Given
        String description = "Test description";

        // When
        GraphConfig config = new GraphConfig.Builder().description(description).build();

        // Then
        assertEquals(description, config.getDescription());
    }

    @Test
    public void shouldBuildGraphConfigWithView() {
        // Given
        View view = new View.Builder().build();

        // When
        GraphConfig config = new GraphConfig.Builder().view(view).build();

        // Then
        assertEquals(view, config.getView());
    }

    @Test
    public void shouldBuildGraphConfigWithHooks() {
        // Given
        GraphHook hook1 = new MockGraphHook();
        GraphHook hook2 = new MockGraphHook();

        // When
        GraphConfig config = new GraphConfig.Builder().addHooks(hook1, hook2).build();

        // Then
        assertEquals(2, config.getHooks().size());
        assertEquals(hook1, config.getHooks().get(0));
        assertEquals(hook2, config.getHooks().get(1));
    }

    @Test
    public void shouldMergeGraphConfigs() {
        // Given
        GraphConfig source = new GraphConfig();
        source.setGraphId("source-graph");
        source.setDescription("source description");

        GraphConfig target = new GraphConfig();
        target.setGraphId("target-graph");

        // When
        GraphConfig.Builder builder = new GraphConfig.Builder().merge(source);
        GraphConfig result = builder.build();

        // Then
        assertEquals("source-graph", result.getGraphId());
        assertEquals("source description", result.getDescription());
    }

    @Test
    public void shouldMergeGraphConfigWithNullValues() {
        // Given
        GraphConfig source = new GraphConfig();
        source.setGraphId(null);
        source.setDescription(null);

        GraphConfig target = new GraphConfig();
        target.setGraphId("target-graph");

        // When
        GraphConfig.Builder builder = new GraphConfig.Builder().merge(source);
        GraphConfig result = builder.build();

        // Then
        assertEquals("target-graph", result.getGraphId());
    }

    @Test
    public void shouldBuildWithJsonBytes() throws IOException {
        // Given
        String json = "{\"graphId\":\"test-graph\",\"description\":\"test\"}";
        byte[] bytes = json.getBytes();

        // When
        GraphConfig config = new GraphConfig.Builder().json(bytes).build();

        // Then
        assertEquals("test-graph", config.getGraphId());
        assertEquals("test", config.getDescription());
    }

    @Test
    public void shouldBuildWithNullJsonBytes() {
        // When
        GraphConfig config = new GraphConfig.Builder().json((byte[]) null).build();

        // Then
        assertNotNull(config);
    }

    @Test
    public void shouldBuildWithInputStream() throws IOException {
        // Given
        String json = "{\"graphId\":\"test-graph\",\"description\":\"test\"}";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes());

        // When
        GraphConfig config = new GraphConfig.Builder().json(inputStream).build();

        // Then
        assertEquals("test-graph", config.getGraphId());
        assertEquals("test", config.getDescription());
    }

    @Test
    public void shouldBuildWithNullInputStream() {
        // When
        GraphConfig config = new GraphConfig.Builder().json((InputStream) null).build();

        // Then
        assertNotNull(config);
    }

    @Test
    public void shouldBuildWithPath() throws IOException {
        // Given
        Path tempFile = Files.createTempFile("graph-config", ".json");
        String json = "{\"graphId\":\"test-graph\",\"description\":\"test\"}";
        Files.write(tempFile, json.getBytes());

        // When
        GraphConfig config = new GraphConfig.Builder().json(tempFile).build();

        // Then
        assertEquals("test-graph", config.getGraphId());
        assertEquals("test", config.getDescription());
    }

    @Test
    public void shouldBuildWithNullPath() {
        // When
        GraphConfig config = new GraphConfig.Builder().json((Path) null).build();

        // Then
        assertNotNull(config);
    }

    @Test
    public void shouldBuildWithUri() throws IOException {
        // Given
        Path tempFile = Files.createTempFile("graph-config", ".json");
        String json = "{\"graphId\":\"test-graph\",\"description\":\"test\"}";
        Files.write(tempFile, json.getBytes());
        URI uri = tempFile.toUri();

        // When
        GraphConfig config = new GraphConfig.Builder().json(uri).build();

        // Then
        assertEquals("test-graph", config.getGraphId());
        assertEquals("test", config.getDescription());
    }

    @Test
    public void shouldBuildWithNullUri() {
        // When
        GraphConfig config = new GraphConfig.Builder().json((URI) null).build();

        // Then
        assertNotNull(config);
    }

    @Test
    public void shouldAddHookFromPath() throws IOException {
        // Given
        Path tempFile = Files.createTempFile("graph-hook", ".json");
        String json = "{\"class\":\"uk.gov.gchq.gaffer.graph.GraphConfig_ArrowTest$MockGraphHook\"}";
        Files.write(tempFile, json.getBytes());

        // When
        GraphConfig config = new GraphConfig.Builder().addHook(tempFile).build();

        // Then
        assertEquals(1, config.getHooks().size());
        assertTrue(config.getHooks().get(0) instanceof MockGraphHook);
    }

    @Test
    public void shouldAddHooksFromPath() throws IOException {
        // Given
        Path tempFile = Files.createTempFile("graph-hooks", ".json");
        String json = "[{\"class\":\"uk.gov.gchq.gaffer.graph.GraphConfig_ArrowTest$MockGraphHook\"},{\"class\":\"uk.gov.gchq.gaffer.graph.GraphConfig_ArrowTest$MockGraphHook\"}]";
        Files.write(tempFile, json.getBytes());

        // When
        GraphConfig config = new GraphConfig.Builder().addHooks(tempFile).build();

        // Then
        assertEquals(2, config.getHooks().size());
        assertTrue(config.getHooks().get(0) instanceof MockGraphHook);
        assertTrue(config.getHooks().get(1) instanceof MockGraphHook);
    }

    @Test
    public void shouldToString() {
        // Given
        GraphConfig config = new GraphConfig();
        config.setGraphId("test-graph");

        // When
        String result = config.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("test-graph"));
    }

    public static class MockGraphHook implements GraphHook {
        @Override
        public void preExecute(final Graph graph, final Object... args) {
        }

        @Override
        public void postExecute(final Graph graph, final Object result, final Object... args) {
        }
    }
}
