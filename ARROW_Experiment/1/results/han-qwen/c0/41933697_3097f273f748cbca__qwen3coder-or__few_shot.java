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
    when(networkConfig.getConnectionTimeout()).thenReturn(10000);
    when(networkConfig.getNonFatalErrorCodes()).thenReturn(Arrays.asList(429, 503));
    when(networkConfig.getNonFatalErrorMessages()).thenReturn(Arrays.asList("Rate limit exceeded", "Service unavailable"));

    when(exchangeConfig.getNetworkConfig()).thenReturn(networkConfig);

    Map<String, String> authConfig = new HashMap<>();
    authConfig.put("api-key", "test-key");
    authConfig.put("secret-key", "test-secret");
    when(exchangeConfig.getAuthenticationConfig()).thenReturn(authConfig);

    Map<String, String> otherConfig = new HashMap<>();
    otherConfig.put("max-retries", "3");
    otherConfig.put("retry-delay", "5000");
    when(exchangeConfig.getOtherConfig()).thenReturn(otherConfig);

    // When
    ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(exchangeConfig);

    // Then
    assertNotNull(result);
    assertEquals("Binance", result.getExchangeName());
    assertEquals("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter", result.getExchangeAdapter());

    // Network config
    assertNotNull(result.getNetworkConfig());
    assertEquals(10000, result.getNetworkConfig().getConnectionTimeout());
    assertEquals(Arrays.asList(429, 503), result.getNetworkConfig().getNonFatalErrorCodes());
    assertEquals(Arrays.asList("Rate limit exceeded", "Service unavailable"), result.getNetworkConfig().getNonFatalErrorMessages());

    // Authentication config
    assertNotNull(result.getAuthenticationConfig());
    assertEquals(authConfig, result.getAuthenticationConfig().getItems());

    // Other config
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

    // When
    ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(exchangeConfig);

    // Then
    assertNotNull(result);
    assertEquals("Binance", result.getExchangeName());
    assertEquals("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter", result.getExchangeAdapter());
    assertNull(result.getNetworkConfig());
  }

  @Test
  public void testBuildConfigWithEmptyNetworkConfig() {
    // Given
    ExchangeConfig exchangeConfig = mock(ExchangeConfig.class);
    when(exchangeConfig.getName()).thenReturn("Binance");
    when(exchangeConfig.getAdapter()).thenReturn("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter");

    NetworkConfig networkConfig = mock(NetworkConfig.class);
    when(networkConfig.getConnectionTimeout()).thenReturn(10000);
    when(networkConfig.getNonFatalErrorCodes()).thenReturn(null);
    when(networkConfig.getNonFatalErrorMessages()).thenReturn(null);

    when(exchangeConfig.getNetworkConfig()).thenReturn(networkConfig);

    // When
    ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(exchangeConfig);

    // Then
    assertNotNull(result);
    assertEquals("Binance", result.getExchangeName());
    assertEquals("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter", result.getExchangeAdapter());

    // Network config should be created but with null/empty collections
    assertNotNull(result.getNetworkConfig());
    assertEquals(10000, result.getNetworkConfig().getConnectionTimeout());
    assertNull(result.getNetworkConfig().getNonFatalErrorCodes());
    assertNull(result.getNetworkConfig().getNonFatalErrorMessages());
  }

  @Test
  public void testBuildConfigWithNullAuthenticationConfig() {
    // Given
    ExchangeConfig exchangeConfig = mock(ExchangeConfig.class);
    when(exchangeConfig.getName()).thenReturn("Binance");
    when(exchangeConfig.getAdapter()).thenReturn("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter");
    when(exchangeConfig.getAuthenticationConfig()).thenReturn(null);

    // When
    ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(exchangeConfig);

    // Then
    assertNotNull(result);
    assertEquals("Binance", result.getExchangeName());
    assertEquals("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter", result.getExchangeAdapter());
    assertNull(result.getAuthenticationConfig());
  }

  @Test
  public void testBuildConfigWithNullOtherConfig() {
    // Given
    ExchangeConfig exchangeConfig = mock(ExchangeConfig.class);
    when(exchangeConfig.getName()).thenReturn("Binance");
    when(exchangeConfig.getAdapter()).thenReturn("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter");
    when(exchangeConfig.getOtherConfig()).thenReturn(null);

    // When
    ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(exchangeConfig);

    // Then
    assertNotNull(result);
    assertEquals("Binance", result.getExchangeName());
    assertEquals("com.gazbert.bxbot.exchanges.BinanceExchangeAdapter", result.getExchangeAdapter());
    assertNull(result.getOtherConfig());
  }
}
