package schemacrawler.tools.offline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.sql.Connection;

import org.junit.Test;

import schemacrawler.schemacrawler.Config;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;

public class OfflineCatalogLoader_ArrowTest {

  @Test
  public void databaseSystemIdentifierIsOffline() {
    OfflineCatalogLoader loader = new OfflineCatalogLoader();

    assertEquals("offline", loader.getDatabaseSystemIdentifier());
  }

  @Test
  public void additionalConfigurationDefaultsToConfig() {
    OfflineCatalogLoader loader = new OfflineCatalogLoader();

    assertNotNull(loader.getAdditionalConfiguration());
  }

  @Test
  public void additionalConfigurationCanBeSet() {
    OfflineCatalogLoader loader = new OfflineCatalogLoader();
    Config configuration = new Config();

    loader.setAdditionalConfiguration(configuration);

    assertSame(configuration, loader.getAdditionalConfiguration());
  }

  @Test
  public void connectionDefaultsToNull() {
    OfflineCatalogLoader loader = new OfflineCatalogLoader();

    assertNull(loader.getConnection());
  }

  @Test
  public void connectionCanBeSet() {
    OfflineCatalogLoader loader = new OfflineCatalogLoader();
    Connection connection = null;

    loader.setConnection(connection);

    assertSame(connection, loader.getConnection());
  }

  @Test
  public void schemaCrawlerOptionsDefaultsToOptions() {
    OfflineCatalogLoader loader = new OfflineCatalogLoader();

    assertNotNull(loader.getSchemaCrawlerOptions());
  }

  @Test
  public void schemaCrawlerOptionsCanBeSet() {
    OfflineCatalogLoader loader = new OfflineCatalogLoader();
    SchemaCrawlerOptions options = loader.getSchemaCrawlerOptions();

    loader.setSchemaCrawlerOptions(options);

    assertSame(options, loader.getSchemaCrawlerOptions());
  }

  @Test
  public void schemaRetrievalOptionsAreCreated() {
    OfflineCatalogLoader loader = new OfflineCatalogLoader();

    SchemaRetrievalOptions options = loader.getSchemaRetrievalOptions();

    assertNotNull(options);
  }

  @Test
  public void settingSchemaRetrievalOptionsDoesNotThrow() {
    OfflineCatalogLoader loader = new OfflineCatalogLoader();

    loader.setSchemaRetrievalOptions(null);

    assertNotNull(loader.getSchemaRetrievalOptions());
  }
}
