package com.gazbert.bxbot.core.config.exchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
    final ExchangeConfig exchangeConfig = new ExchangeConfig();
    exchangeConfig.setName("Binance");
    exchangeConfig.setAdapter("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter");

    final NetworkConfig networkConfig = new NetworkConfig();
    networkConfig.setConnectionTimeout(30);
    networkConfig.setNonFatalErrorCodes(Arrays.asList(404, 500));
    networkConfig.setNonFatalErrorMessages(Arrays.asList("Not Found", "Internal Server Error"));
    exchangeConfig.setNetworkConfig(networkConfig);

    final Map<String, String> authenticationConfig = new HashMap<>();
    authenticationConfig.put("api-key", "test-key");
    authenticationConfig.put("secret-key", "test-secret");
    exchangeConfig.setAuthenticationConfig(authenticationConfig);

    final Map<String, String> otherConfig = new HashMap<>();
    otherConfig.put("max-orders", "100");
    exchangeConfig.setOtherConfig(otherConfig);

    // When
    final ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(exchangeConfig);

    // Then
    assertNotNull(result);
    assertEquals("Binance", result.getExchangeName());
    assertEquals("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter", result.getExchangeAdapter());

    // Network config
    assertNotNull(result.getNetworkConfig());
    assertEquals(30, result.getNetworkConfig().getConnectionTimeout());
    assertEquals(Arrays.asList(404, 500), result.getNetworkConfig().getNonFatalErrorCodes());
    assertEquals(Arrays.asList("Not Found", "Internal Server Error"),
        result.getNetworkConfig().getNonFatalErrorMessages());

    // Authentication config
    assertNotNull(result.getAuthenticationConfig());
    assertEquals(authenticationConfig, result.getAuthenticationConfig().getItems());

    // Other config
    assertNotNull(result.getOtherConfig());
    assertEquals(otherConfig, result.getOtherConfig().getItems());
  }

  @Test
  public void testBuildConfigWithNullNetworkConfig() {
    // Given
    final ExchangeConfig exchangeConfig = new ExchangeConfig();
    exchangeConfig.setName("Binance");
    exchangeConfig.setAdapter("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter");
    exchangeConfig.setNetworkConfig(null);

    // When
    final ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(exchangeConfig);

    // Then
    assertNotNull(result);
    assertEquals("Binance", result.getExchangeName());
    assertEquals("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter", result.getExchangeAdapter());
    assertNull(result.getNetworkConfig());
  }

  @Test
  public void testBuildConfigWithEmptyNetworkConfig() {
    // Given
    final ExchangeConfig exchangeConfig = new ExchangeConfig();
    exchangeConfig.setName("Binance");
    exchangeConfig.setAdapter("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter");

    final NetworkConfig networkConfig = new NetworkConfig();
    networkConfig.setConnectionTimeout(30);
    networkConfig.setNonFatalErrorCodes(null);
    networkConfig.setNonFatalErrorMessages(null);
    exchangeConfig.setNetworkConfig(networkConfig);

    // When
    final ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(exchangeConfig);

    // Then
    assertNotNull(result);
    assertEquals("Binance", result.getExchangeName());
    assertEquals("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter", result.getExchangeAdapter());

    // Network config should be created but with null/empty collections
    assertNotNull(result.getNetworkConfig());
    assertEquals(30, result.getNetworkConfig().getConnectionTimeout());
    assertNull(result.getNetworkConfig().getNonFatalErrorCodes());
    assertNull(result.getNetworkConfig().getNonFatalErrorMessages());
  }

  @Test
  public void testBuildConfigWithNullAuthenticationConfig() {
    // Given
    final ExchangeConfig exchangeConfig = new ExchangeConfig();
    exchangeConfig.setName("Binance");
    exchangeConfig.setAdapter("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter");
    exchangeConfig.setAuthenticationConfig(null);

    // When
    final ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(exchangeConfig);

    // Then
    assertNotNull(result);
    assertEquals("Binance", result.getExchangeName());
    assertEquals("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter", result.getExchangeAdapter());
    assertNull(result.getAuthenticationConfig());
  }

  @Test
  public void testBuildConfigWithNullOtherConfig() {
    // Given
    final ExchangeConfig exchangeConfig = new ExchangeConfig();
    exchangeConfig.setName("Binance");
    exchangeConfig.setAdapter("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter");
    exchangeConfig.setOtherConfig(null);

    // When
    final ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(exchangeConfig);

    // Then
    assertNotNull(result);
    assertEquals("Binance", result.getExchangeName());
    assertEquals("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter", result.getExchangeAdapter());
    assertNull(result.getOtherConfig());
  }

  @Test
  public void testBuildConfigWithEmptyCollectionsInNetworkConfig() {
    // Given
    final ExchangeConfig exchangeConfig = new ExchangeConfig();
    exchangeConfig.setName("Binance");
    exchangeConfig.setAdapter("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter");

    final NetworkConfig networkConfig = new NetworkConfig();
    networkConfig.setConnectionTimeout(30);
    networkConfig.setNonFatalErrorCodes(Arrays.asList());
    networkConfig.setNonFatalErrorMessages(Arrays.asList());
    exchangeConfig.setNetworkConfig(networkConfig);

    // When
    final ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(exchangeConfig);

    // Then
    assertNotNull(result);
    assertEquals("Binance", result.getExchangeName());
    assertEquals("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter", result.getExchangeAdapter());

    // Network config should be created but with empty collections
    assertNotNull(result.getNetworkConfig());
    assertEquals(30, result.getNetworkConfig().getConnectionTimeout());
    assertEquals(Arrays.asList(), result.getNetworkConfig().getNonFatalErrorCodes());
    assertEquals(Arrays.asList(), result.getNetworkConfig().getNonFatalErrorMessages());
  }
}
