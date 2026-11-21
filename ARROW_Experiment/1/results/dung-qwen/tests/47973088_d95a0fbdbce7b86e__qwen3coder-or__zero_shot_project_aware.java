package uk.gov.gchq.gaffer.graph;

import org.junit.Test;
import uk.gov.gchq.gaffer.data.elementdefinition.view.View;
import uk.gov.gchq.gaffer.graph.hook.GraphHook;
import uk.gov.gchq.gaffer.graph.hook.GraphHookPath;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

public class GraphConfig_ArrowTest {

    @Test
    public void shouldCreateEmptyGraphConfig() {
        // When
        GraphConfig config = new GraphConfig();

        // Then
        assertThat(config.getGraphId(), is(nullValue()));
        assertThat(config.getView(), is(nullValue()));
        assertThat(config.getLibrary(), is(nullValue()));
        assertThat(config.getDescription(), is(nullValue()));
        assertThat(config.getHooks(), contains());
    }

    @Test
    public void shouldCreateGraphConfigWithGraphId() {
        // Given
        String graphId = "test-graph";

        // When
        GraphConfig config = new GraphConfig(graphId);

        // Then
        assertThat(config.getGraphId(), equalTo(graphId));
    }

    @Test
    public void shouldSetAndGetGraphId() {
        // Given
        GraphConfig config = new GraphConfig();
        String graphId = "test-graph";

        // When
        config.setGraphId(graphId);

        // Then
        assertThat(config.getGraphId(), equalTo(graphId));
    }

    @Test
    public void shouldSetAndGetView() {
        // Given
        GraphConfig config = new GraphConfig();
        View view = new View.Builder().build();

        // When
        config.setView(view);

        // Then
        assertThat(config.getView(), equalTo(view));
    }

    @Test
    public void shouldSetAndGetNullView() {
        // Given
        GraphConfig config = new GraphConfig();

        // When
        config.setView(null);

        // Then
        assertThat(config.getView(), is(nullValue()));
    }

    @Test
    public void shouldSetAndGetLibrary() {
        // Given
        GraphConfig config = new GraphConfig();
        GraphLibrary library = new NoGraphLibrary();

        // When
        config.setLibrary(library);

        // Then
        assertThat(config.getLibrary(), equalTo(library));
    }

    @Test
    public void shouldSetAndGetDescription() {
        // Given
        GraphConfig config = new GraphConfig();
        String description = "Test description";

        // When
        config.setDescription(description);

        // Then
        assertThat(config.getDescription(), equalTo(description));
    }

    @Test
    public void shouldSetAndGetHooks() {
        // Given
        GraphConfig config = new GraphConfig();
        List<GraphHook> hooks = new ArrayList<>();
        GraphHook hook1 = mock(GraphHook.class);
        GraphHook hook2 = mock(GraphHook.class);
        hooks.add(hook1);
        hooks.add(hook2);

        // When
        config.setHooks(hooks);

        // Then
        assertThat(config.getHooks(), contains(hook1, hook2));
    }

    @Test
    public void shouldClearHooksWhenSettingNullHooks() {
        // Given
        GraphConfig config = new GraphConfig();
        GraphHook hook1 = mock(GraphHook.class);
        config.addHook(hook1);

        // When
        config.setHooks(null);

        // Then
        assertThat(config.getHooks(), contains());
    }

    @Test
    public void shouldAddHook() {
        // Given
        GraphConfig config = new GraphConfig();
        GraphHook hook = mock(GraphHook.class);

        // When
        config.addHook(hook);

        // Then
        assertThat(config.getHooks(), contains(hook));
    }

    @Test
    public void shouldNotAddNullHook() {
        // Given
        GraphConfig config = new GraphConfig();

        // When
        config.addHook(null);

        // Then
        assertThat(config.getHooks(), contains());
    }

    @Test
    public void shouldThrowExceptionWhenAddingGraphHookPathWithNonExistentFile() {
        // Given
        GraphConfig config = new GraphConfig();
        GraphHookPath hookPath = new GraphHookPath();
        hookPath.setPath("/non/existent/file");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> config.addHook(hookPath));
        assertThat(exception.getMessage(), equalTo("Unable to find graph hook file: /non/existent/file"));
    }

    @Test
    public void shouldBuildGraphConfigWithDefaultNoGraphLibrary() {
        // When
        GraphConfig config = new GraphConfig.Builder().build();

        // Then
        assertThat(config.getLibrary(), instanceOf(NoGraphLibrary.class));
    }

    @Test
    public void shouldBuildGraphConfigWithCustomGraphId() {
        // When
        GraphConfig config = new GraphConfig.Builder().graphId("test-id").build();

        // Then
        assertThat(config.getGraphId(), equalTo("test-id"));
    }

    @Test
    public void shouldBuildGraphConfigWithCustomLibrary() {
        // When
        GraphLibrary library = new NoGraphLibrary();
        GraphConfig config = new GraphConfig.Builder().library(library).build();

        // Then
        assertThat(config.getLibrary(), equalTo(library));
    }

    @Test
    public void shouldBuildGraphConfigWithCustomDescription() {
        // When
        GraphConfig config = new GraphConfig.Builder().description("test-description").build();

        // Then
        assertThat(config.getDescription(), equalTo("test-description"));
    }

    @Test
    public void shouldBuildGraphConfigWithCustomView() {
        // When
        View view = new View.Builder().build();
        GraphConfig config = new GraphConfig.Builder().view(view).build();

        // Then
        assertThat(config.getView(), equalTo(view));
    }

    @Test
    public void shouldBuildGraphConfigWithCustomHooks() {
        // When
        GraphHook hook1 = mock(GraphHook.class);
        GraphHook hook2 = mock(GraphHook.class);
        GraphConfig config = new GraphConfig.Builder().addHooks(hook1, hook2).build();

        // Then
        assertThat(config.getHooks(), contains(hook1, hook2));
    }

    @Test
    public void shouldMergeGraphConfigs() {
        // Given
        GraphConfig source = new GraphConfig();
        source.setGraphId("source-id");
        source.setDescription("source-desc");
        View view = new View.Builder().build();
        source.setView(view);
        GraphLibrary library = new NoGraphLibrary();
        source.setLibrary(library);

        GraphHook hook1 = mock(GraphHook.class);
        GraphHook hook2 = mock(GraphHook.class);
        source.getHooks().add(hook1);
        source.getHooks().add(hook2);

        // When
        GraphConfig target = new GraphConfig.Builder().merge(source).build();

        // Then
        assertThat(target.getGraphId(), equalTo("source-id"));
        assertThat(target.getDescription(), equalTo("source-desc"));
        assertThat(target.getView(), equalTo(view));
        assertThat(target.getLibrary(), equalTo(library));
        assertThat(target.getHooks(), contains(hook1, hook2));
    }

    @Test
    public void shouldHandleNullInMerge() {
        // When
        GraphConfig target = new GraphConfig.Builder().merge(null).build();

        // Then
        assertThat(target.getGraphId(), is(nullValue()));
        assertThat(target.getDescription(), is(nullValue()));
        assertThat(target.getView(), is(nullValue()));
        assertThat(target.getLibrary(), instanceOf(NoGraphLibrary.class));
        assertThat(target.getHooks(), contains());
    }

    @Test
    public void shouldHandleNullValuesInMerge() {
        // Given
        GraphConfig source = new GraphConfig();
        source.setGraphId(null);
        source.setDescription(null);
        source.setView(null);
        source.setLibrary(null);

        // When
        GraphConfig target = new GraphConfig.Builder().merge(source).build();

        // Then
        assertThat(target.getGraphId(), is(nullValue()));
        assertThat(target.getDescription(), is(nullValue()));
        assertThat(target.getView(), is(nullValue()));
        assertThat(target.getLibrary(), instanceOf(NoGraphLibrary.class));
        assertThat(target.getHooks(), contains());
    }

    @Test
    public void shouldBuildFromJsonBytes() throws IOException {
        // Given
        String json = "{\"graphId\":\"test-id\",\"description\":\"test-desc\"}";
        byte[] bytes = json.getBytes();

        // When
        GraphConfig config = new GraphConfig.Builder().json(bytes).build();

        // Then
        assertThat(config.getGraphId(), equalTo("test-id"));
        assertThat(config.getDescription(), equalTo("test-desc"));
    }

    @Test
    public void shouldHandleNullJsonBytes() {
        // When
        GraphConfig config = new GraphConfig.Builder().json((byte[]) null).build();

        // Then
        assertThat(config.getGraphId(), is(nullValue()));
        assertThat(config.getDescription(), is(nullValue()));
    }

    @Test
    public void shouldBuildFromInputStream() throws IOException {
        // Given
        String json = "{\"graphId\":\"test-id\",\"description\":\"test-desc\"}";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes());

        // When
        GraphConfig config = new GraphConfig.Builder().json(inputStream).build();

        // Then
        assertThat(config.getGraphId(), equalTo("test-id"));
        assertThat(config.getDescription(), equalTo("test-desc"));
    }

    @Test
    public void shouldHandleNullInputStream() {
        // When
        GraphConfig config = new GraphConfig.Builder().json((InputStream) null).build();

        // Then
        assertThat(config.getGraphId(), is(nullValue()));
        assertThat(config.getDescription(), is(nullValue()));
    }

    @Test
    public void shouldBuildFromURI() throws IOException {
        // Given
        String json = "{\"graphId\":\"test-id\",\"description\":\"test-desc\"}";
        Path tempFile = Files.createTempFile("graph-config", ".json");
        Files.write(tempFile, json.getBytes());
        URI uri = tempFile.toUri();

        // When
        GraphConfig config = new GraphConfig.Builder().json(uri).build();

        // Then
        assertThat(config.getGraphId(), equalTo("test-id"));
        assertThat(config.getDescription(), equalTo("test-desc"));

        // Cleanup
        Files.delete(tempFile);
    }

    @Test
    public void shouldHandleNullURI() {
        // When
        GraphConfig config = new GraphConfig.Builder().json((URI) null).build();

        // Then
        assertThat(config.getGraphId(), is(nullValue()));
        assertThat(config.getDescription(), is(nullValue()));
    }

    @Test
    public void shouldBuildFromPath() throws IOException {
        // Given
        String json = "{\"graphId\":\"test-id\",\"description\":\"test-desc\"}";
        Path tempFile = Files.createTempFile("graph-config", ".json");
        Files.write(tempFile, json.getBytes());

        // When
        GraphConfig config = new GraphConfig.Builder().json(tempFile).build();

        // Then
        assertThat(config.getGraphId(), equalTo("test-id"));
        assertThat(config.getDescription(), equalTo("test-desc"));

        // Cleanup
        Files.delete(tempFile);
    }

    @Test
    public void shouldHandleNullPath() {
        // When
        GraphConfig config = new GraphConfig.Builder().json((Path) null).build();

        // Then
        assertThat(config.getGraphId(), is(nullValue()));
        assertThat(config.getDescription(), is(nullValue()));
    }

    @Test
    public void shouldAddHookFromFile() throws IOException {
        // Given
        String hookJson = "{\"class\":\"" + GraphHook.class.getName() + "\"}";
        Path tempFile = Files.createTempFile("hook", ".json");
        Files.write(tempFile, hookJson.getBytes());
        GraphHook hook = mock(GraphHook.class);

        // When
        GraphConfig config = new GraphConfig.Builder().addHook(tempFile).build();

        // Then
        assertThat(config.getHooks().size(), equalTo(1));

        // Cleanup
        Files.delete(tempFile);
    }

    @Test
    public void shouldThrowExceptionWhenAddingHookFromFileThatDoesNotExist() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new GraphConfig.Builder().addHook(Paths.get("/non/existent/file")).build());
        assertThat(exception.getMessage(), equalTo("Unable to find graph hook file: /non/existent/file"));
    }

    @Test
    public void shouldAddHooksFromFile() throws IOException {
        // Given
        String hooksJson = "[{\"class\":\"" + GraphHook.class.getName() + "\"},{\"class\":\"" + GraphHook.class.getName() + "\"}]";
        Path tempFile = Files.createTempFile("hooks", ".json");
        Files.write(tempFile, hooksJson.getBytes());

        // When
        GraphConfig config = new GraphConfig.Builder().addHooks(tempFile).build();

        // Then
        assertThat(config.getHooks().size(), equalTo(2));

        // Cleanup
        Files.delete(tempFile);
    }

    @Test
    public void shouldThrowExceptionWhenAddingHooksFromFileThatDoesNotExist() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new GraphConfig.Builder().addHooks(Paths.get("/non/existent/file")).build());
        assertThat(exception.getMessage(), equalTo("Unable to find graph hooks file: /non/existent/file"));
    }

    @Test
    public void shouldHandleNullInAddHooks() {
        // When
        GraphConfig config = new GraphConfig.Builder().addHooks((GraphHook[]) null).build();

        // Then
        assertThat(config.getHooks(), contains());
    }

    @Test
    public void shouldHandleNullInAddHook() {
        // When
        GraphConfig config = new GraphConfig.Builder().addHook((GraphHook) null).build();

        // Then
        assertThat(config.getHooks(), contains());
    }

    @Test
    public void shouldReturnToString() {
        // Given
        GraphConfig config = new GraphConfig();
        config.setGraphId("test-id");

        // When
        String result = config.toString();

        // Then
        assertThat(result, containsString("graphId=test-id"));
    }

    private static <T> T instanceOf(Class<T> clazz) {
        return null;
    }
}
