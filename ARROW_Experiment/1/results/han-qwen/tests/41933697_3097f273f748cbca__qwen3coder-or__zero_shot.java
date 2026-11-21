package com.gazbert.bxbot.core.config.exchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gazbert.bxbot.domain.exchange.ExchangeConfig;
import com.gazbert.bxbot.domain.exchange.NetworkConfig;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class ExchangeApiConfigBuilder_ArrowTest {

  @Test
  public void testBuildConfigWithAllConfigs() {
    // Given
    ExchangeConfig exchangeConfig = mock(ExchangeConfig.class);
    when(exchangeConfig.getName()).thenReturn("Binance");
    when(exchangeConfig.getAdapter()).thenReturn("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter");

    NetworkConfig networkConfig = mock(NetworkConfig.class);
    when(networkConfig.getConnectionTimeout()).thenReturn(30);
    when(networkConfig.getNonFatalErrorCodes()).thenReturn(Arrays.asList(404, 500));
    when(networkConfig.getNonFatalErrorMessages()).thenReturn(Arrays.asList("Not Found", "Server Error"));
    when(exchangeConfig.getNetworkConfig()).thenReturn(networkConfig);

    Map<String, String> authConfig = new HashMap<>();
    authConfig.put("api-key", "test-key");
    authConfig.put("secret", "test-secret");
    when(exchangeConfig.getAuthenticationConfig()).thenReturn(authConfig);

    Map<String, String> otherConfig = new HashMap<>();
    otherConfig.put("max-retries", "3");
    otherConfig.put("retry-delay", "1000");
    when(exchangeConfig.getOtherConfig()).thenReturn(otherConfig);

    // When
    ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(exchangeConfig);

    // Then
    assertNotNull(result);
    assertEquals("Binance", result.getExchangeName());
    assertEquals("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter", result.getExchangeAdapter());

    // Check network config
    assertNotNull(result.getNetworkConfig());
    assertEquals(30, result.getNetworkConfig().getConnectionTimeout());
    assertEquals(Arrays.asList(404, 500), result.getNetworkConfig().getNonFatalErrorCodes());
    assertEquals(Arrays.asList("Not Found", "Server Error"), result.getNetworkConfig().getNonFatalErrorMessages());

    // Check authentication config
    assertNotNull(result.getAuthenticationConfig());
    assertEquals(authConfig, result.getAuthenticationConfig().getItems());

    // Check other config
    assertNotNull(result.getOtherConfig());
    assertEquals(otherConfig, result.getOtherConfig().getItems());
  }

  @Test
  public void testBuildConfigWithNullNetworkConfig() {
    // Given
    ExchangeConfig exchangeConfig = mock(ExchangeConfig.class);
    when(exchangeConfig.getName()).thenReturn("Binance");
    when(exchangeConfig.getAdapter()).thenReturn("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter");
    when(exchangeConfig.getNetworkConfig()).thenReturn(null);

    Map<String, String> authConfig = new HashMap<>();
    authConfig.put("api-key", "test-key");
    when(exchangeConfig.getAuthenticationConfig()).thenReturn(authConfig);

    Map<String, String> otherConfig = new HashMap<>();
    otherConfig.put("max-retries", "3");
    when(exchangeConfig.getOtherConfig()).thenReturn(otherConfig);

    // When
    ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(exchangeConfig);

    // Then
    assertNotNull(result);
    assertEquals("Binance", result.getExchangeName());
    assertEquals("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter", result.getExchangeAdapter());
    assertNull(result.getNetworkConfig());

    // Check authentication config
    assertNotNull(result.getAuthenticationConfig());
    assertEquals(authConfig, result.getAuthenticationConfig().getItems());

    // Check other config
    assertNotNull(result.getOtherConfig());
    assertEquals(otherConfig, result.getOtherConfig().getItems());
  }

  @Test
  public void testBuildConfigWithEmptyNetworkConfigLists() {
    // Given
    ExchangeConfig exchangeConfig = mock(ExchangeConfig.class);
    when(exchangeConfig.getName()).thenReturn("Binance");
    when(exchangeConfig.getAdapter()).thenReturn("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter");

    NetworkConfig networkConfig = mock(NetworkConfig.class);
    when(networkConfig.getConnectionTimeout()).thenReturn(30);
    when(networkConfig.getNonFatalErrorCodes()).thenReturn(Arrays.asList());
    when(networkConfig.getNonFatalErrorMessages()).thenReturn(Arrays.asList());
    when(exchangeConfig.getNetworkConfig()).thenReturn(networkConfig);

    Map<String, String> authConfig = new HashMap<>();
    authConfig.put("api-key", "test-key");
    when(exchangeConfig.getAuthenticationConfig()).thenReturn(authConfig);

    Map<String, String> otherConfig = new HashMap<>();
    otherConfig.put("max-retries", "3");
    when(exchangeConfig.getOtherConfig()).thenReturn(otherConfig);

    // When
    ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(exchangeConfig);

    // Then
    assertNotNull(result);
    assertEquals("Binance", result.getExchangeName());
    assertEquals("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter", result.getExchangeAdapter());

    // Check network config
    assertNotNull(result.getNetworkConfig());
    assertEquals(30, result.getNetworkConfig().getConnectionTimeout());
    assertNull(result.getNetworkConfig().getNonFatalErrorCodes());
    assertNull(result.getNetworkConfig().getNonFatalErrorMessages());

    // Check authentication config
    assertNotNull(result.getAuthenticationConfig());
    assertEquals(authConfig, result.getAuthenticationConfig().getItems());

    // Check other config
    assertNotNull(result.getOtherConfig());
    assertEquals(otherConfig, result.getOtherConfig().getItems());
  }

  @Test
  public void testBuildConfigWithNullAuthenticationConfig() {
    // Given
    ExchangeConfig exchangeConfig = mock(ExchangeConfig.class);
    when(exchangeConfig.getName()).thenReturn("Binance");
    when(exchangeConfig.getAdapter()).thenReturn("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter");

    NetworkConfig networkConfig = mock(NetworkConfig.class);
    when(networkConfig.getConnectionTimeout()).thenReturn(30);
    when(exchangeConfig.getNetworkConfig()).thenReturn(networkConfig);

    when(exchangeConfig.getAuthenticationConfig()).thenReturn(null);

    Map<String, String> otherConfig = new HashMap<>();
    otherConfig.put("max-retries", "3");
    when(exchangeConfig.getOtherConfig()).thenReturn(otherConfig);

    // When
    ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(exchangeConfig);

    // Then
    assertNotNull(result);
    assertEquals("Binance", result.getExchangeName());
    assertEquals("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter", result.getExchangeAdapter());

    // Check network config
    assertNotNull(result.getNetworkConfig());
    assertEquals(30, result.getNetworkConfig().getConnectionTimeout());

    // Check authentication config
    assertNull(result.getAuthenticationConfig());

    // Check other config
    assertNotNull(result.getOtherConfig());
    assertEquals(otherConfig, result.getOtherConfig().getItems());
  }

  @Test
  public void testBuildConfigWithNullOtherConfig() {
    // Given
    ExchangeConfig exchangeConfig = mock(ExchangeConfig.class);
    when(exchangeConfig.getName()).thenReturn("Binance");
    when(exchangeConfig.getAdapter()).thenReturn("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter");

    NetworkConfig networkConfig = mock(NetworkConfig.class);
    when(networkConfig.getConnectionTimeout()).thenReturn(30);
    when(exchangeConfig.getNetworkConfig()).thenReturn(networkConfig);

    Map<String, String> authConfig = new HashMap<>();
    authConfig.put("api-key", "test-key");
    when(exchangeConfig.getAuthenticationConfig()).thenReturn(authConfig);

    when(exchangeConfig.getOtherConfig()).thenReturn(null);

    // When
    ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(exchangeConfig);

    // Then
    assertNotNull(result);
    assertEquals("Binance", result.getExchangeName());
    assertEquals("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter", result.getExchangeAdapter());

    // Check network config
    assertNotNull(result.getNetworkConfig());
    assertEquals(30, result.getNetworkConfig().getConnectionTimeout());

    // Check authentication config
    assertNotNull(result.getAuthenticationConfig());
    assertEquals(authConfig, result.getAuthenticationConfig().getItems());

    // Check other config
    assertNull(result.getOtherConfig());
  }
}
