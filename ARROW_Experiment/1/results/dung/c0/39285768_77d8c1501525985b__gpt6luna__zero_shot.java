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

public class OfflineCatalogLoader_ArrowTest
{
  @Test
  public void databaseSystemIdentifierIsOffline()
  {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();

    assertEquals("offline", loader.getDatabaseSystemIdentifier());
  }

  @Test
  public void additionalConfigurationIsCreatedWhenUnset()
  {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();

    assertNotNull(loader.getAdditionalConfiguration());
  }

  @Test
  public void additionalConfigurationCanBeSetAndRetrieved()
  {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();
    final Config configuration = new Config();

    loader.setAdditionalConfiguration(configuration);

    assertSame(configuration, loader.getAdditionalConfiguration());
  }

  @Test
  public void connectionIsNullInitially()
  {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();

    assertNull(loader.getConnection());
  }

  @Test
  public void connectionCanBeSetAndRetrieved()
  {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();
    final Connection connection = null;

    loader.setConnection(connection);

    assertSame(connection, loader.getConnection());
  }

  @Test
  public void schemaCrawlerOptionsAreCreatedWhenUnset()
  {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();

    assertNotNull(loader.getSchemaCrawlerOptions());
  }

  @Test
  public void schemaCrawlerOptionsCanBeSetAndRetrieved()
  {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();
    final SchemaCrawlerOptions options = loader.getSchemaCrawlerOptions();

    loader.setSchemaCrawlerOptions(options);

    assertSame(options, loader.getSchemaCrawlerOptions());
  }

  @Test
  public void schemaRetrievalOptionsAreCreated()
  {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();

    assertNotNull(loader.getSchemaRetrievalOptions());
  }

  @Test
  public void settingSchemaRetrievalOptionsDoesNotPreventCreatingOptions()
  {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();
    final SchemaRetrievalOptions options = loader.getSchemaRetrievalOptions();

    loader.setSchemaRetrievalOptions(options);

    assertNotNull(loader.getSchemaRetrievalOptions());
  }
}
