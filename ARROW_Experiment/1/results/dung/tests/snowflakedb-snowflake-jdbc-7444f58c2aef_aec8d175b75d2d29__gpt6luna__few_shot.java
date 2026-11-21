package net.snowflake.client.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

public class SFClientConfigParser_ArrowTest {

  @Test
  public void loadConfigFromExplicitPath() throws Exception {
    Path configFile = Files.createTempFile("sf-client-config", ".json");
    try {
      Files.write(
          configFile,
          "{\"arrow\":true}".getBytes(StandardCharsets.UTF_8));
      SFClientConfig config =
          SFClientConfigParser.loadSFClientConfig(configFile.toString());
      assertNotNull(config);
      assertEquals(configFile.toString(), config.getConfigFilePath());
    } finally {
      Files.deleteIfExists(configFile);
    }
  }

  @Test
  public void loadConfigFromExplicitPathReportsInvalidJson() throws Exception {
    Path configFile = Files.createTempFile("sf-client-config-invalid", ".json");
    try {
      Files.write(configFile, "{".getBytes(StandardCharsets.UTF_8));
      try {
        SFClientConfigParser.loadSFClientConfig(configFile.toString());
        fail("Expected IOException");
      } catch (IOException expected) {
        assertEquals(
            "Error while reading config file at location: " + configFile.toString(),
            expected.getMessage());
      }
    } finally {
      Files.deleteIfExists(configFile);
    }
  }

  @Test
  public void getConfigFilePathFromJdbcJarLocationIsNotNull() {
    assertNotNull(SFClientConfigParser.getConfigFilePathFromJDBCJarLocation());
  }

  @Test
  public void loadConfigReturnsNullWhenNoConfigIsFound() throws Exception {
    assertNull(SFClientConfigParser.loadSFClientConfig(""));
  }
}
