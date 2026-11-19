package schemacrawler.tools.offline;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.logging.Level;

import org.junit.jupiter.api.Test;

import schemacrawler.schema.Catalog;
import schemacrawler.schema.Reducible;
import schemacrawler.schemacrawler.Config;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import schemacrawler.tools.catalogloader.CatalogLoader;
import schemacrawler.tools.integration.serialize.JavaSerializedCatalog;
import schemacrawler.tools.offline.jdbc.OfflineConnection;
import schemacrawler.SchemaCrawlerLogger;

public class OfflineCatalogLoader_ArrowTest {

  @Test
  public void testGetDatabaseSystemIdentifier() {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();
    assertThat(loader.getDatabaseSystemIdentifier(), is("offline"));
  }

  @Test
  public void testGetAdditionalConfiguration_Null() {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();
    final Config config = loader.getAdditionalConfiguration();
    assertThat(config, is(notNullValue()));
  }

  @Test
  public void testGetAdditionalConfiguration_WithConfig() {
    final Config config = new Config();
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();
    loader.setAdditionalConfiguration(config);
    assertThat(loader.getAdditionalConfiguration(), is(config));
  }

  @Test
  public void testGetConnection() {
    final Connection connection = mock(Connection.class);
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();
    loader.setConnection(connection);
    assertThat(loader.getConnection(), is(connection));
  }

  @Test
  public void testGetSchemaCrawlerOptions_Null() {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();
    final SchemaCrawlerOptions options = loader.getSchemaCrawlerOptions();
    assertThat(options, is(notNullValue()));
  }

  @Test
  public void testGetSchemaCrawlerOptions_WithOptions() {
    final SchemaCrawlerOptions options = SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions();
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();
    loader.setSchemaCrawlerOptions(options);
    assertThat(loader.getSchemaCrawlerOptions(), is(options));
  }

  @Test
  public void testGetSchemaRetrievalOptions() {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();
    final SchemaRetrievalOptions options = loader.getSchemaRetrievalOptions();
    assertThat(options, is(notNullValue()));
  }

  @Test
  public void testSetSchemaRetrievalOptions_NoOp() {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();
    final SchemaRetrievalOptions options = SchemaRetrievalOptionsBuilder.builder()
        .withDatabaseServerType("test")
        .toOptions();
    loader.setSchemaRetrievalOptions(options);
    // No-op, just ensure no exception is thrown
  }

  @Test
  public void testLoadCatalog() throws Exception {
    final OfflineConnection connection = mock(OfflineConnection.class);
    final Path path = mock(Path.class);
    final File file = mock(File.class);
    final FileInputStream inputStream = mock(FileInputStream.class);
    final JavaSerializedCatalog serializedCatalog = mock(JavaSerializedCatalog.class);
    final Catalog catalog = mock(Catalog.class);
    
    when(connection.isWrapperFor(OfflineConnection.class)).thenReturn(true);
    when(connection.unwrap(OfflineConnection.class)).thenReturn(connection);
    when(connection.getOfflineDatabasePath()).thenReturn(path);
    when(path.toFile()).thenReturn(file);
    when(new FileInputStream(file)).thenReturn(inputStream);
    when(new JavaSerializedCatalog(inputStream)).thenReturn(serializedCatalog);
    when(serializedCatalog.getCatalog()).thenReturn(catalog);
    when(((Reducible) catalog).reduce(null, null)).thenReturn(catalog);
    
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();
    loader.setConnection(connection);
    
    final Catalog loadedCatalog = loader.loadCatalog();
    assertThat(loadedCatalog, is(catalog));
  }
}
