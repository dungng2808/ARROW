package schemacrawler.tools.offline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.sql.Connection;

import org.junit.jupiter.api.Test;

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
  public void connectionIsInitiallyNull()
  {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();

    assertNull(loader.getConnection());
  }

  @Test
  public void connectionCanBeSetAndRetrieved()
  {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();
    final Connection connection = org.mockito.Mockito.mock(Connection.class);

    loader.setConnection(connection);

    assertSame(connection, loader.getConnection());
  }

  @Test
  public void additionalConfigurationDefaultsToAnEmptyConfig()
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
  public void schemaCrawlerOptionsDefaultToOptions()
  {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();

    assertNotNull(loader.getSchemaCrawlerOptions());
  }

  @Test
  public void schemaCrawlerOptionsCanBeSetAndRetrieved()
  {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();
    final SchemaCrawlerOptions options =
      org.mockito.Mockito.mock(SchemaCrawlerOptions.class);

    loader.setSchemaCrawlerOptions(options);

    assertSame(options, loader.getSchemaCrawlerOptions());
  }

  @Test
  public void schemaRetrievalOptionsAreCreated()
  {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();

    final SchemaRetrievalOptions options = loader.getSchemaRetrievalOptions();

    assertNotNull(options);
  }

  @Test
  public void settingSchemaRetrievalOptionsDoesNotChangeReturnedOptions()
  {
    final OfflineCatalogLoader loader = new OfflineCatalogLoader();
    final SchemaRetrievalOptions supplied =
      org.mockito.Mockito.mock(SchemaRetrievalOptions.class);

    loader.setSchemaRetrievalOptions(supplied);

    assertNotNull(loader.getSchemaRetrievalOptions());
  }
}
