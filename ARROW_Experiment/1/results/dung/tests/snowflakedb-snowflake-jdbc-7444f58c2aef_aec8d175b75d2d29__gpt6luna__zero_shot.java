package net.snowflake.client.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Test;

public class SFClientConfigParser_ArrowTest {

  @Test
  public void loadConfigFromExplicitPath() throws Exception {
    File configFile = File.createTempFile("sf-client-config", ".json");
    try {
      Files.write(configFile.toPath(), "{}".getBytes(StandardCharsets.UTF_8));

      SFClientConfig config = SFClientConfigParser.loadSFClientConfig(configFile.getAbsolutePath());

      assertNotNull(config);
      assertEquals(configFile.getAbsolutePath(), config.getConfigFilePath());
    } finally {
      Files.deleteIfExists(configFile.toPath());
    }
  }

  @Test
  public void loadConfigReturnsNullWhenNoConfigFileIsFound() throws Exception {
    assertNull(SFClientConfigParser.loadSFClientConfig(""));
  }

  @Test
  public void loadConfigWrapsIOExceptionForInvalidJson() throws Exception {
    File configFile = File.createTempFile("sf-client-config-invalid", ".json");
    try {
      Files.write(configFile.toPath(), "{".getBytes(StandardCharsets.UTF_8));

      try {
        SFClientConfigParser.loadSFClientConfig(configFile.getAbsolutePath());
        fail("Expected IOException");
      } catch (IOException e) {
        assertEquals(
            "Error while reading config file at location: " + configFile.getAbsolutePath(),
            e.getMessage());
      }
    } finally {
      Files.deleteIfExists(configFile.toPath());
    }
  }

  @Test
  public void getConfigFilePathFromJDBCJarLocationReturnsNonNullPath() {
    assertNotNull(SFClientConfigParser.getConfigFilePathFromJDBCJarLocation());
  }
}
