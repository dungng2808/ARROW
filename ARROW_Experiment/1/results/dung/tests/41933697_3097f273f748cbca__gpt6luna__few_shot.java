package com.gazbert.bxbot.core.config.exchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.gazbert.bxbot.domain.exchange.ExchangeConfig;
import com.gazbert.bxbot.domain.exchange.NetworkConfig;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class ExchangeApiConfigBuilder_ArrowTest {

  @Test
  public void buildsExchangeConfigWithoutOptionalConfigurations() {
    ExchangeConfig exchangeConfig = new ExchangeConfig();
    exchangeConfig.setName("Example Exchange");
    exchangeConfig.setAdapter("ExampleAdapter");

    ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(exchangeConfig);

    assertEquals("Example Exchange", result.getExchangeName());
    assertEquals("ExampleAdapter", result.getExchangeAdapter());
    assertNull(result.getNetworkConfig());
    assertNull(result.getAuthenticationConfig());
    assertNull(result.getOtherConfig());
  }

  @Test
  public void buildsNetworkConfigurationWithNonFatalErrors() {
    ExchangeConfig exchangeConfig = new ExchangeConfig();
    exchangeConfig.setName("Example Exchange");
    exchangeConfig.setAdapter("ExampleAdapter");

    NetworkConfig networkConfig = new NetworkConfig();
    networkConfig.setConnectionTimeout(2500);
    networkConfig.setNonFatalErrorCodes(Arrays.asList(429, 503));
    networkConfig.setNonFatalErrorMessages(Arrays.asList("busy", "unavailable"));
    exchangeConfig.setNetworkConfig(networkConfig);

    ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(exchangeConfig);

    assertNotNull(result.getNetworkConfig());
    assertEquals(2500, result.getNetworkConfig().getConnectionTimeout());
    assertEquals(Arrays.asList(429, 503), result.getNetworkConfig().getNonFatalErrorCodes());
    assertEquals(
        Arrays.asList("busy", "unavailable"),
        result.getNetworkConfig().getNonFatalErrorMessages());
  }

  @Test
  public void buildsAuthenticationAndOtherConfigurations() {
    ExchangeConfig exchangeConfig = new ExchangeConfig();
    exchangeConfig.setName("Example Exchange");
    exchangeConfig.setAdapter("ExampleAdapter");

    Map<String, String> authentication = new HashMap<>();
    authentication.put("apiKey", "key-value");
    exchangeConfig.setAuthenticationConfig(authentication);

    Map<String, String> other = new HashMap<>();
    other.put("mode", "test");
    exchangeConfig.setOtherConfig(other);

    ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(exchangeConfig);

    assertNotNull(result.getAuthenticationConfig());
    assertEquals(authentication, result.getAuthenticationConfig().getItems());
    assertNotNull(result.getOtherConfig());
    assertEquals(other, result.getOtherConfig().getItems());
  }
}
